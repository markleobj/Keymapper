package io.github.sds100.keymapper;

import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import io.github.sds100.keymapper.base.onboarding.SetupAccessibilityServiceDelegateImpl;
import io.github.sds100.keymapper.base.utils.navigation.NavigationProviderImpl;
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
public final class MainFragment_MembersInjector implements MembersInjector<MainFragment> {
  private final Provider<NavigationProviderImpl> navigationProvider;

  private final Provider<SetupAccessibilityServiceDelegateImpl> setupAccessibilityServiceDelegateProvider;

  public MainFragment_MembersInjector(Provider<NavigationProviderImpl> navigationProvider,
      Provider<SetupAccessibilityServiceDelegateImpl> setupAccessibilityServiceDelegateProvider) {
    this.navigationProvider = navigationProvider;
    this.setupAccessibilityServiceDelegateProvider = setupAccessibilityServiceDelegateProvider;
  }

  public static MembersInjector<MainFragment> create(
      Provider<NavigationProviderImpl> navigationProvider,
      Provider<SetupAccessibilityServiceDelegateImpl> setupAccessibilityServiceDelegateProvider) {
    return new MainFragment_MembersInjector(navigationProvider, setupAccessibilityServiceDelegateProvider);
  }

  @Override
  public void injectMembers(MainFragment instance) {
    injectNavigationProvider(instance, navigationProvider.get());
    injectSetupAccessibilityServiceDelegate(instance, setupAccessibilityServiceDelegateProvider.get());
  }

  @InjectedFieldSignature("io.github.sds100.keymapper.MainFragment.navigationProvider")
  public static void injectNavigationProvider(MainFragment instance,
      NavigationProviderImpl navigationProvider) {
    instance.navigationProvider = navigationProvider;
  }

  @InjectedFieldSignature("io.github.sds100.keymapper.MainFragment.setupAccessibilityServiceDelegate")
  public static void injectSetupAccessibilityServiceDelegate(MainFragment instance,
      SetupAccessibilityServiceDelegateImpl setupAccessibilityServiceDelegate) {
    instance.setupAccessibilityServiceDelegate = setupAccessibilityServiceDelegate;
  }
}
