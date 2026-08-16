package com.keymapper.app.ui

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class CoordinatePickerActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "CoordPickerActivity"
        const val EXTRA_PICKED_X = "picked_x"
        const val EXTRA_PICKED_Y = "picked_y"
        private const val OVERLAY_REQUEST_CODE = 300
    }

    private var resultReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawableResource(android.R.color.transparent)

        val bg = View(this)
        bg.setBackgroundColor(Color.TRANSPARENT)
        setContentView(bg)

        if (!checkOverlayPermission()) {
            requestOverlayPermission()
            return
        }

        startPicking()
    }

    private fun checkOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else true
    }

    private fun requestOverlayPermission() {
        Toast.makeText(this, "需要悬浮窗权限才能在其他 app 上取坐标", Toast.LENGTH_LONG).show()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                android.net.Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, OVERLAY_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OVERLAY_REQUEST_CODE) {
            if (checkOverlayPermission()) {
                startPicking()
            } else {
                Toast.makeText(this, "没有权限，取消拾取", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }
    }

    private fun startPicking() {
        registerResultReceiver()

        CoordinatePickerOverlay(this).show()
        Toast.makeText(this, "✅ 已退后台，请在目标 app 上点击", Toast.LENGTH_LONG).show()

        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        moveTaskToBack(true)
    }

    private fun registerResultReceiver() {
        resultReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: android.content.Intent?) {
                Log.i(TAG, "received broadcast: ${intent?.action}")
                when (intent?.action) {
                    CoordinatePickerOverlay.ACTION_PICK_RESULT -> {
                        val nx = intent.getFloatExtra(CoordinatePickerOverlay.EXTRA_X, 0f)
                        val ny = intent.getFloatExtra(CoordinatePickerOverlay.EXTRA_Y, 0f)
                        Log.i(TAG, "pick result: ($nx, $ny)")
                        val result = Intent().apply {
                            putExtra(EXTRA_PICKED_X, nx)
                            putExtra(EXTRA_PICKED_Y, ny)
                        }
                        setResult(Activity.RESULT_OK, result)
                        finish()
                    }
                    CoordinatePickerOverlay.ACTION_PICK_CANCEL -> {
                        Log.i(TAG, "pick cancelled")
                        setResult(Activity.RESULT_CANCELED)
                        finish()
                    }
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(CoordinatePickerOverlay.ACTION_PICK_RESULT)
            addAction(CoordinatePickerOverlay.ACTION_PICK_CANCEL)
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(resultReceiver!!, filter)
        ContextCompat.registerReceiver(this, resultReceiver!!, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    override fun onDestroy() {
        super.onDestroy()
        resultReceiver?.let {
            runCatching { LocalBroadcastManager.getInstance(this).unregisterReceiver(it) }
            runCatching { unregisterReceiver(it) }
        }
        CoordinatePickerOverlay.dismiss()
    }

    override fun onBackPressed() {
        CoordinatePickerOverlay.dismiss()
        setResult(Activity.RESULT_CANCELED)
        super.onBackPressed()
    }
}
