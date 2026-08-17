package io.github.sds100.keymapper.system.accessibility;

import dagger.internal.DaggerGenerated;
import dagger.internal.InstanceFactory;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class AccessibilityServiceController_Factory_Impl implements AccessibilityServiceController.Factory {
  private final AccessibilityServiceController_Factory delegateFactory;

  AccessibilityServiceController_Factory_Impl(
      AccessibilityServiceController_Factory delegateFactory) {
    this.delegateFactory = delegateFactory;
  }

  @Override
  public AccessibilityServiceController create(MyAccessibilityService service) {
    return delegateFactory.get(service);
  }

  public static Provider<AccessibilityServiceController.Factory> create(
      AccessibilityServiceController_Factory delegateFactory) {
    return InstanceFactory.create(new AccessibilityServiceController_Factory_Impl(delegateFactory));
  }

  public static dagger.internal.Provider<AccessibilityServiceController.Factory> createFactoryProvider(
      AccessibilityServiceController_Factory delegateFactory) {
    return InstanceFactory.create(new AccessibilityServiceController_Factory_Impl(delegateFactory));
  }
}
