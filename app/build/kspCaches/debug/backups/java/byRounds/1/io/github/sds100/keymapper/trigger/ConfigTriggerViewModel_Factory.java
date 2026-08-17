package io.github.sds100.keymapper.trigger;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import io.github.sds100.keymapper.base.keymaps.DisplayKeyMapUseCase;
import io.github.sds100.keymapper.base.keymaps.FingerprintGesturesSupportedUseCase;
import io.github.sds100.keymapper.base.onboarding.OnboardingTipDelegate;
import io.github.sds100.keymapper.base.onboarding.OnboardingUseCase;
import io.github.sds100.keymapper.base.onboarding.SetupAccessibilityServiceDelegate;
import io.github.sds100.keymapper.base.shortcuts.CreateKeyMapShortcutUseCase;
import io.github.sds100.keymapper.base.trigger.ConfigTriggerUseCase;
import io.github.sds100.keymapper.base.trigger.RecordTriggerController;
import io.github.sds100.keymapper.base.trigger.TriggerSetupDelegate;
import io.github.sds100.keymapper.base.utils.navigation.NavigationProvider;
import io.github.sds100.keymapper.base.utils.ui.DialogProvider;
import io.github.sds100.keymapper.base.utils.ui.ResourceProvider;
import io.github.sds100.keymapper.sysbridge.manager.SystemBridgeConnectionManager;
import javax.annotation.processing.Generated;

@ScopeMetadata
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
public final class ConfigTriggerViewModel_Factory implements Factory<ConfigTriggerViewModel> {
  private final Provider<OnboardingUseCase> onboardingProvider;

  private final Provider<ConfigTriggerUseCase> configProvider;

  private final Provider<RecordTriggerController> recordTriggerProvider;

  private final Provider<CreateKeyMapShortcutUseCase> createKeyMapShortcutProvider;

  private final Provider<DisplayKeyMapUseCase> displayKeyMapProvider;

  private final Provider<FingerprintGesturesSupportedUseCase> fingerprintGesturesSupportedProvider;

  private final Provider<SystemBridgeConnectionManager> systemBridgeConnectionManagerProvider;

  private final Provider<SetupAccessibilityServiceDelegate> setupAccessibilityServiceDelegateProvider;

  private final Provider<OnboardingTipDelegate> onboardingTipDelegateProvider;

  private final Provider<TriggerSetupDelegate> triggerSetupDelegateProvider;

  private final Provider<ResourceProvider> resourceProvider;

  private final Provider<NavigationProvider> navigationProvider;

  private final Provider<DialogProvider> dialogProvider;

  public ConfigTriggerViewModel_Factory(Provider<OnboardingUseCase> onboardingProvider,
      Provider<ConfigTriggerUseCase> configProvider,
      Provider<RecordTriggerController> recordTriggerProvider,
      Provider<CreateKeyMapShortcutUseCase> createKeyMapShortcutProvider,
      Provider<DisplayKeyMapUseCase> displayKeyMapProvider,
      Provider<FingerprintGesturesSupportedUseCase> fingerprintGesturesSupportedProvider,
      Provider<SystemBridgeConnectionManager> systemBridgeConnectionManagerProvider,
      Provider<SetupAccessibilityServiceDelegate> setupAccessibilityServiceDelegateProvider,
      Provider<OnboardingTipDelegate> onboardingTipDelegateProvider,
      Provider<TriggerSetupDelegate> triggerSetupDelegateProvider,
      Provider<ResourceProvider> resourceProvider, Provider<NavigationProvider> navigationProvider,
      Provider<DialogProvider> dialogProvider) {
    this.onboardingProvider = onboardingProvider;
    this.configProvider = configProvider;
    this.recordTriggerProvider = recordTriggerProvider;
    this.createKeyMapShortcutProvider = createKeyMapShortcutProvider;
    this.displayKeyMapProvider = displayKeyMapProvider;
    this.fingerprintGesturesSupportedProvider = fingerprintGesturesSupportedProvider;
    this.systemBridgeConnectionManagerProvider = systemBridgeConnectionManagerProvider;
    this.setupAccessibilityServiceDelegateProvider = setupAccessibilityServiceDelegateProvider;
    this.onboardingTipDelegateProvider = onboardingTipDelegateProvider;
    this.triggerSetupDelegateProvider = triggerSetupDelegateProvider;
    this.resourceProvider = resourceProvider;
    this.navigationProvider = navigationProvider;
    this.dialogProvider = dialogProvider;
  }

  @Override
  public ConfigTriggerViewModel get() {
    return newInstance(onboardingProvider.get(), configProvider.get(), recordTriggerProvider.get(), createKeyMapShortcutProvider.get(), displayKeyMapProvider.get(), fingerprintGesturesSupportedProvider.get(), systemBridgeConnectionManagerProvider.get(), setupAccessibilityServiceDelegateProvider.get(), onboardingTipDelegateProvider.get(), triggerSetupDelegateProvider.get(), resourceProvider.get(), navigationProvider.get(), dialogProvider.get());
  }

  public static ConfigTriggerViewModel_Factory create(
      Provider<OnboardingUseCase> onboardingProvider, Provider<ConfigTriggerUseCase> configProvider,
      Provider<RecordTriggerController> recordTriggerProvider,
      Provider<CreateKeyMapShortcutUseCase> createKeyMapShortcutProvider,
      Provider<DisplayKeyMapUseCase> displayKeyMapProvider,
      Provider<FingerprintGesturesSupportedUseCase> fingerprintGesturesSupportedProvider,
      Provider<SystemBridgeConnectionManager> systemBridgeConnectionManagerProvider,
      Provider<SetupAccessibilityServiceDelegate> setupAccessibilityServiceDelegateProvider,
      Provider<OnboardingTipDelegate> onboardingTipDelegateProvider,
      Provider<TriggerSetupDelegate> triggerSetupDelegateProvider,
      Provider<ResourceProvider> resourceProvider, Provider<NavigationProvider> navigationProvider,
      Provider<DialogProvider> dialogProvider) {
    return new ConfigTriggerViewModel_Factory(onboardingProvider, configProvider, recordTriggerProvider, createKeyMapShortcutProvider, displayKeyMapProvider, fingerprintGesturesSupportedProvider, systemBridgeConnectionManagerProvider, setupAccessibilityServiceDelegateProvider, onboardingTipDelegateProvider, triggerSetupDelegateProvider, resourceProvider, navigationProvider, dialogProvider);
  }

  public static ConfigTriggerViewModel newInstance(OnboardingUseCase onboarding,
      ConfigTriggerUseCase config, RecordTriggerController recordTrigger,
      CreateKeyMapShortcutUseCase createKeyMapShortcut, DisplayKeyMapUseCase displayKeyMap,
      FingerprintGesturesSupportedUseCase fingerprintGesturesSupported,
      SystemBridgeConnectionManager systemBridgeConnectionManager,
      SetupAccessibilityServiceDelegate setupAccessibilityServiceDelegate,
      OnboardingTipDelegate onboardingTipDelegate, TriggerSetupDelegate triggerSetupDelegate,
      ResourceProvider resourceProvider, NavigationProvider navigationProvider,
      DialogProvider dialogProvider) {
    return new ConfigTriggerViewModel(onboarding, config, recordTrigger, createKeyMapShortcut, displayKeyMap, fingerprintGesturesSupported, systemBridgeConnectionManager, setupAccessibilityServiceDelegate, onboardingTipDelegate, triggerSetupDelegate, resourceProvider, navigationProvider, dialogProvider);
  }
}
