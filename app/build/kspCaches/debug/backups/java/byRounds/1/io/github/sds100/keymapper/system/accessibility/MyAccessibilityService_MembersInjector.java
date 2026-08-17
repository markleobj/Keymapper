package io.github.sds100.keymapper.system.accessibility;

import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import io.github.sds100.keymapper.base.system.accessibility.AccessibilityServiceAdapterImpl;
import io.github.sds100.keymapper.base.system.accessibility.BaseAccessibilityService_MembersInjector;
import io.github.sds100.keymapper.system.inputmethod.InputMethodAdapter;
import javax.annotation.processing.Generated;

@QualifierMetadata
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
public final class MyAccessibilityService_MembersInjector implements MembersInjector<MyAccessibilityService> {
  private final Provider<AccessibilityServiceAdapterImpl> accessibilityServiceAdapterLazyProvider;

  private final Provider<InputMethodAdapter> inputMethodAdapterLazyProvider;

  private final Provider<AccessibilityServiceController.Factory> controllerFactoryProvider;

  public MyAccessibilityService_MembersInjector(
      Provider<AccessibilityServiceAdapterImpl> accessibilityServiceAdapterLazyProvider,
      Provider<InputMethodAdapter> inputMethodAdapterLazyProvider,
      Provider<AccessibilityServiceController.Factory> controllerFactoryProvider) {
    this.accessibilityServiceAdapterLazyProvider = accessibilityServiceAdapterLazyProvider;
    this.inputMethodAdapterLazyProvider = inputMethodAdapterLazyProvider;
    this.controllerFactoryProvider = controllerFactoryProvider;
  }

  public static MembersInjector<MyAccessibilityService> create(
      Provider<AccessibilityServiceAdapterImpl> accessibilityServiceAdapterLazyProvider,
      Provider<InputMethodAdapter> inputMethodAdapterLazyProvider,
      Provider<AccessibilityServiceController.Factory> controllerFactoryProvider) {
    return new MyAccessibilityService_MembersInjector(accessibilityServiceAdapterLazyProvider, inputMethodAdapterLazyProvider, controllerFactoryProvider);
  }

  @Override
  public void injectMembers(MyAccessibilityService instance) {
    BaseAccessibilityService_MembersInjector.injectAccessibilityServiceAdapterLazy(instance, DoubleCheck.lazy(accessibilityServiceAdapterLazyProvider));
    BaseAccessibilityService_MembersInjector.injectInputMethodAdapterLazy(instance, DoubleCheck.lazy(inputMethodAdapterLazyProvider));
    injectControllerFactory(instance, controllerFactoryProvider.get());
  }

  @InjectedFieldSignature("io.github.sds100.keymapper.system.accessibility.MyAccessibilityService.controllerFactory")
  public static void injectControllerFactory(MyAccessibilityService instance,
      AccessibilityServiceController.Factory controllerFactory) {
    instance.controllerFactory = controllerFactory;
  }
}
