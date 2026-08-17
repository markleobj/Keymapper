package io.github.sds100.keymapper.system.accessibility;

import android.content.Intent;
import android.os.UserManager;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import dagger.hilt.android.AndroidEntryPoint;
import io.github.sds100.keymapper.base.system.accessibility.BaseAccessibilityService;
import io.github.sds100.keymapper.base.system.accessibility.BaseAccessibilityServiceController;
import javax.inject.Inject;
import timber.log.Timber;

@dagger.hilt.android.AndroidEntryPoint()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000L\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\n\u0010\u0013\u001a\u0004\u0018\u00010\u0014H\u0016J\b\u0010\u0015\u001a\u00020\fH\u0002J\u0012\u0010\u0016\u001a\u00020\u00172\b\u0010\u0018\u001a\u0004\u0018\u00010\u0019H\u0016J\b\u0010\u001a\u001a\u00020\u0017H\u0016J\b\u0010\u001b\u001a\u00020\u0017H\u0016J\u0012\u0010\u001c\u001a\u00020\f2\b\u0010\u0018\u001a\u0004\u0018\u00010\u001dH\u0014J\b\u0010\u001e\u001a\u00020\u0017H\u0014J\u0012\u0010\u001f\u001a\u00020\f2\b\u0010 \u001a\u0004\u0018\u00010!H\u0016R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001e\u0010\u0005\u001a\u00020\u00068\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0007\u0010\b\"\u0004\b\t\u0010\nR\u000e\u0010\u000b\u001a\u00020\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001d\u0010\r\u001a\u0004\u0018\u00010\u000e8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0011\u0010\u0012\u001a\u0004\b\u000f\u0010\u0010\u00a8\u0006\""}, d2 = {"Lio/github/sds100/keymapper/system/accessibility/MyAccessibilityService;", "Lio/github/sds100/keymapper/base/system/accessibility/BaseAccessibilityService;", "()V", "controller", "Lio/github/sds100/keymapper/system/accessibility/AccessibilityServiceController;", "controllerFactory", "Lio/github/sds100/keymapper/system/accessibility/AccessibilityServiceController$Factory;", "getControllerFactory", "()Lio/github/sds100/keymapper/system/accessibility/AccessibilityServiceController$Factory;", "setControllerFactory", "(Lio/github/sds100/keymapper/system/accessibility/AccessibilityServiceController$Factory;)V", "loggedLockedInitDelay", "", "userManager", "Landroid/os/UserManager;", "getUserManager", "()Landroid/os/UserManager;", "userManager$delegate", "Lkotlin/Lazy;", "getController", "Lio/github/sds100/keymapper/base/system/accessibility/BaseAccessibilityServiceController;", "initializeControllerIfUserUnlocked", "onAccessibilityEvent", "", "event", "Landroid/view/accessibility/AccessibilityEvent;", "onDestroy", "onInterrupt", "onKeyEvent", "Landroid/view/KeyEvent;", "onServiceConnected", "onUnbind", "intent", "Landroid/content/Intent;", "app_debug"})
public final class MyAccessibilityService extends io.github.sds100.keymapper.base.system.accessibility.BaseAccessibilityService {
    @javax.inject.Inject()
    public io.github.sds100.keymapper.system.accessibility.AccessibilityServiceController.Factory controllerFactory;
    @org.jetbrains.annotations.Nullable()
    private io.github.sds100.keymapper.system.accessibility.AccessibilityServiceController controller;
    private boolean loggedLockedInitDelay = false;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy userManager$delegate = null;
    
    public MyAccessibilityService() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final io.github.sds100.keymapper.system.accessibility.AccessibilityServiceController.Factory getControllerFactory() {
        return null;
    }
    
    public final void setControllerFactory(@org.jetbrains.annotations.NotNull()
    io.github.sds100.keymapper.system.accessibility.AccessibilityServiceController.Factory p0) {
    }
    
    private final android.os.UserManager getUserManager() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public io.github.sds100.keymapper.base.system.accessibility.BaseAccessibilityServiceController getController() {
        return null;
    }
    
    @java.lang.Override()
    protected void onServiceConnected() {
    }
    
    @java.lang.Override()
    public void onAccessibilityEvent(@org.jetbrains.annotations.Nullable()
    android.view.accessibility.AccessibilityEvent event) {
    }
    
    @java.lang.Override()
    protected boolean onKeyEvent(@org.jetbrains.annotations.Nullable()
    android.view.KeyEvent event) {
        return false;
    }
    
    private final boolean initializeControllerIfUserUnlocked() {
        return false;
    }
    
    @java.lang.Override()
    public boolean onUnbind(@org.jetbrains.annotations.Nullable()
    android.content.Intent intent) {
        return false;
    }
    
    @java.lang.Override()
    public void onInterrupt() {
    }
    
    @java.lang.Override()
    public void onDestroy() {
    }
}