package io.github.sds100.keymapper.system.accessibility;

import androidx.annotation.CallSuper;
import dagger.hilt.internal.GeneratedComponentManagerHolder;
import dagger.hilt.internal.UnsafeCasts;
import io.github.sds100.keymapper.base.system.accessibility.BaseAccessibilityService;
import java.lang.Override;
import javax.annotation.processing.Generated;

/**
 * A generated base class to be extended by the @dagger.hilt.android.AndroidEntryPoint annotated class. If using the Gradle plugin, this is swapped as the base class via bytecode transformation.
 */
@Generated("dagger.hilt.android.processor.internal.androidentrypoint.ServiceGenerator")
public abstract class Hilt_MyAccessibilityService extends BaseAccessibilityService {
  private boolean injected = false;

  @CallSuper
  @Override
  public void onCreate() {
    inject();
    super.onCreate();
  }

  protected void inject() {
    if (!injected) {
      injected = true;
      ((MyAccessibilityService_GeneratedInjector) UnsafeCasts.<GeneratedComponentManagerHolder>unsafeCast(this).generatedComponent()).injectMyAccessibilityService(UnsafeCasts.<MyAccessibilityService>unsafeCast(this));
    }
  }
}
