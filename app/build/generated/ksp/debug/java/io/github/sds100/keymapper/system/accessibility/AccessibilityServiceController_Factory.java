package io.github.sds100.keymapper.system.accessibility;

import dagger.internal.DaggerGenerated;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import io.github.sds100.keymapper.base.actions.PerformActionsUseCaseImpl;
import io.github.sds100.keymapper.base.constraints.DetectConstraintsUseCaseImpl;
import io.github.sds100.keymapper.base.detection.DetectKeyMapsUseCaseImpl;
import io.github.sds100.keymapper.base.expertmode.SystemBridgeSetupAssistantController;
import io.github.sds100.keymapper.base.input.InputEventHub;
import io.github.sds100.keymapper.base.keymaps.FingerprintGesturesSupportedUseCase;
import io.github.sds100.keymapper.base.keymaps.PauseKeyMapsUseCase;
import io.github.sds100.keymapper.base.system.accessibility.AccessibilityNodeRecorder;
import io.github.sds100.keymapper.base.system.inputmethod.AutoSwitchImeController;
import io.github.sds100.keymapper.base.trigger.RecordTriggerController;
import io.github.sds100.keymapper.data.repositories.PreferenceRepository;
import io.github.sds100.keymapper.system.inputmethod.KeyEventRelayServiceWrapper;
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
public final class AccessibilityServiceController_Factory {
  private final Provider<AccessibilityNodeRecorder.Factory> accessibilityNodeRecorderFactoryProvider;

  private final Provider<PerformActionsUseCaseImpl.Factory> performActionsUseCaseFactoryProvider;

  private final Provider<DetectKeyMapsUseCaseImpl.Factory> detectKeyMapsUseCaseFactoryProvider;

  private final Provider<DetectConstraintsUseCaseImpl.Factory> detectConstraintsUseCaseFactoryProvider;

  private final Provider<FingerprintGesturesSupportedUseCase> fingerprintGesturesSupportedProvider;

  private final Provider<PauseKeyMapsUseCase> pauseKeyMapsUseCaseProvider;

  private final Provider<PreferenceRepository> settingsRepositoryProvider;

  private final Provider<KeyEventRelayServiceWrapper> keyEventRelayServiceWrapperProvider;

  private final Provider<InputEventHub> inputEventHubProvider;

  private final Provider<RecordTriggerController> recordTriggerControllerProvider;

  private final Provider<SystemBridgeSetupAssistantController.Factory> setupAssistantControllerFactoryProvider;

  private final Provider<AutoSwitchImeController.Factory> autoSwitchImeControllerFactoryProvider;

  public AccessibilityServiceController_Factory(
      Provider<AccessibilityNodeRecorder.Factory> accessibilityNodeRecorderFactoryProvider,
      Provider<PerformActionsUseCaseImpl.Factory> performActionsUseCaseFactoryProvider,
      Provider<DetectKeyMapsUseCaseImpl.Factory> detectKeyMapsUseCaseFactoryProvider,
      Provider<DetectConstraintsUseCaseImpl.Factory> detectConstraintsUseCaseFactoryProvider,
      Provider<FingerprintGesturesSupportedUseCase> fingerprintGesturesSupportedProvider,
      Provider<PauseKeyMapsUseCase> pauseKeyMapsUseCaseProvider,
      Provider<PreferenceRepository> settingsRepositoryProvider,
      Provider<KeyEventRelayServiceWrapper> keyEventRelayServiceWrapperProvider,
      Provider<InputEventHub> inputEventHubProvider,
      Provider<RecordTriggerController> recordTriggerControllerProvider,
      Provider<SystemBridgeSetupAssistantController.Factory> setupAssistantControllerFactoryProvider,
      Provider<AutoSwitchImeController.Factory> autoSwitchImeControllerFactoryProvider) {
    this.accessibilityNodeRecorderFactoryProvider = accessibilityNodeRecorderFactoryProvider;
    this.performActionsUseCaseFactoryProvider = performActionsUseCaseFactoryProvider;
    this.detectKeyMapsUseCaseFactoryProvider = detectKeyMapsUseCaseFactoryProvider;
    this.detectConstraintsUseCaseFactoryProvider = detectConstraintsUseCaseFactoryProvider;
    this.fingerprintGesturesSupportedProvider = fingerprintGesturesSupportedProvider;
    this.pauseKeyMapsUseCaseProvider = pauseKeyMapsUseCaseProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
    this.keyEventRelayServiceWrapperProvider = keyEventRelayServiceWrapperProvider;
    this.inputEventHubProvider = inputEventHubProvider;
    this.recordTriggerControllerProvider = recordTriggerControllerProvider;
    this.setupAssistantControllerFactoryProvider = setupAssistantControllerFactoryProvider;
    this.autoSwitchImeControllerFactoryProvider = autoSwitchImeControllerFactoryProvider;
  }

  public AccessibilityServiceController get(MyAccessibilityService service) {
    return newInstance(service, accessibilityNodeRecorderFactoryProvider.get(), performActionsUseCaseFactoryProvider.get(), detectKeyMapsUseCaseFactoryProvider.get(), detectConstraintsUseCaseFactoryProvider.get(), fingerprintGesturesSupportedProvider.get(), pauseKeyMapsUseCaseProvider.get(), settingsRepositoryProvider.get(), keyEventRelayServiceWrapperProvider.get(), inputEventHubProvider.get(), recordTriggerControllerProvider.get(), setupAssistantControllerFactoryProvider.get(), autoSwitchImeControllerFactoryProvider.get());
  }

  public static AccessibilityServiceController_Factory create(
      Provider<AccessibilityNodeRecorder.Factory> accessibilityNodeRecorderFactoryProvider,
      Provider<PerformActionsUseCaseImpl.Factory> performActionsUseCaseFactoryProvider,
      Provider<DetectKeyMapsUseCaseImpl.Factory> detectKeyMapsUseCaseFactoryProvider,
      Provider<DetectConstraintsUseCaseImpl.Factory> detectConstraintsUseCaseFactoryProvider,
      Provider<FingerprintGesturesSupportedUseCase> fingerprintGesturesSupportedProvider,
      Provider<PauseKeyMapsUseCase> pauseKeyMapsUseCaseProvider,
      Provider<PreferenceRepository> settingsRepositoryProvider,
      Provider<KeyEventRelayServiceWrapper> keyEventRelayServiceWrapperProvider,
      Provider<InputEventHub> inputEventHubProvider,
      Provider<RecordTriggerController> recordTriggerControllerProvider,
      Provider<SystemBridgeSetupAssistantController.Factory> setupAssistantControllerFactoryProvider,
      Provider<AutoSwitchImeController.Factory> autoSwitchImeControllerFactoryProvider) {
    return new AccessibilityServiceController_Factory(accessibilityNodeRecorderFactoryProvider, performActionsUseCaseFactoryProvider, detectKeyMapsUseCaseFactoryProvider, detectConstraintsUseCaseFactoryProvider, fingerprintGesturesSupportedProvider, pauseKeyMapsUseCaseProvider, settingsRepositoryProvider, keyEventRelayServiceWrapperProvider, inputEventHubProvider, recordTriggerControllerProvider, setupAssistantControllerFactoryProvider, autoSwitchImeControllerFactoryProvider);
  }

  public static AccessibilityServiceController newInstance(MyAccessibilityService service,
      AccessibilityNodeRecorder.Factory accessibilityNodeRecorderFactory,
      PerformActionsUseCaseImpl.Factory performActionsUseCaseFactory,
      DetectKeyMapsUseCaseImpl.Factory detectKeyMapsUseCaseFactory,
      DetectConstraintsUseCaseImpl.Factory detectConstraintsUseCaseFactory,
      FingerprintGesturesSupportedUseCase fingerprintGesturesSupported,
      PauseKeyMapsUseCase pauseKeyMapsUseCase, PreferenceRepository settingsRepository,
      KeyEventRelayServiceWrapper keyEventRelayServiceWrapper, InputEventHub inputEventHub,
      RecordTriggerController recordTriggerController,
      SystemBridgeSetupAssistantController.Factory setupAssistantControllerFactory,
      AutoSwitchImeController.Factory autoSwitchImeControllerFactory) {
    return new AccessibilityServiceController(service, accessibilityNodeRecorderFactory, performActionsUseCaseFactory, detectKeyMapsUseCaseFactory, detectConstraintsUseCaseFactory, fingerprintGesturesSupported, pauseKeyMapsUseCase, settingsRepository, keyEventRelayServiceWrapper, inputEventHub, recordTriggerController, setupAssistantControllerFactory, autoSwitchImeControllerFactory);
  }
}
