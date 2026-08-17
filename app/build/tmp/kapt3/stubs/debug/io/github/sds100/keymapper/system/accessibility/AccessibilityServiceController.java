package io.github.sds100.keymapper.system.accessibility;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import io.github.sds100.keymapper.base.actions.PerformActionsUseCaseImpl;
import io.github.sds100.keymapper.base.constraints.DetectConstraintsUseCaseImpl;
import io.github.sds100.keymapper.base.detection.DetectKeyMapsUseCaseImpl;
import io.github.sds100.keymapper.base.expertmode.SystemBridgeSetupAssistantController;
import io.github.sds100.keymapper.base.input.InputEventHub;
import io.github.sds100.keymapper.base.keymaps.FingerprintGesturesSupportedUseCase;
import io.github.sds100.keymapper.base.keymaps.PauseKeyMapsUseCase;
import io.github.sds100.keymapper.base.system.accessibility.AccessibilityNodeRecorder;
import io.github.sds100.keymapper.base.system.accessibility.BaseAccessibilityServiceController;
import io.github.sds100.keymapper.base.system.inputmethod.AutoSwitchImeController;
import io.github.sds100.keymapper.base.trigger.RecordTriggerController;
import io.github.sds100.keymapper.data.repositories.PreferenceRepository;
import io.github.sds100.keymapper.system.inputmethod.KeyEventRelayServiceWrapper;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000Z\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0007\u0018\u00002\u00020\u0001:\u0001\u001dBq\b\u0007\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\u0006\u0010\n\u001a\u00020\u000b\u0012\u0006\u0010\f\u001a\u00020\r\u0012\u0006\u0010\u000e\u001a\u00020\u000f\u0012\u0006\u0010\u0010\u001a\u00020\u0011\u0012\u0006\u0010\u0012\u001a\u00020\u0013\u0012\u0006\u0010\u0014\u001a\u00020\u0015\u0012\u0006\u0010\u0016\u001a\u00020\u0017\u0012\u0006\u0010\u0018\u001a\u00020\u0019\u0012\u0006\u0010\u001a\u001a\u00020\u001b\u00a2\u0006\u0002\u0010\u001cR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001e"}, d2 = {"Lio/github/sds100/keymapper/system/accessibility/AccessibilityServiceController;", "Lio/github/sds100/keymapper/base/system/accessibility/BaseAccessibilityServiceController;", "service", "Lio/github/sds100/keymapper/system/accessibility/MyAccessibilityService;", "accessibilityNodeRecorderFactory", "Lio/github/sds100/keymapper/base/system/accessibility/AccessibilityNodeRecorder$Factory;", "performActionsUseCaseFactory", "Lio/github/sds100/keymapper/base/actions/PerformActionsUseCaseImpl$Factory;", "detectKeyMapsUseCaseFactory", "Lio/github/sds100/keymapper/base/detection/DetectKeyMapsUseCaseImpl$Factory;", "detectConstraintsUseCaseFactory", "Lio/github/sds100/keymapper/base/constraints/DetectConstraintsUseCaseImpl$Factory;", "fingerprintGesturesSupported", "Lio/github/sds100/keymapper/base/keymaps/FingerprintGesturesSupportedUseCase;", "pauseKeyMapsUseCase", "Lio/github/sds100/keymapper/base/keymaps/PauseKeyMapsUseCase;", "settingsRepository", "Lio/github/sds100/keymapper/data/repositories/PreferenceRepository;", "keyEventRelayServiceWrapper", "Lio/github/sds100/keymapper/system/inputmethod/KeyEventRelayServiceWrapper;", "inputEventHub", "Lio/github/sds100/keymapper/base/input/InputEventHub;", "recordTriggerController", "Lio/github/sds100/keymapper/base/trigger/RecordTriggerController;", "setupAssistantControllerFactory", "Lio/github/sds100/keymapper/base/expertmode/SystemBridgeSetupAssistantController$Factory;", "autoSwitchImeControllerFactory", "Lio/github/sds100/keymapper/base/system/inputmethod/AutoSwitchImeController$Factory;", "(Lio/github/sds100/keymapper/system/accessibility/MyAccessibilityService;Lio/github/sds100/keymapper/base/system/accessibility/AccessibilityNodeRecorder$Factory;Lio/github/sds100/keymapper/base/actions/PerformActionsUseCaseImpl$Factory;Lio/github/sds100/keymapper/base/detection/DetectKeyMapsUseCaseImpl$Factory;Lio/github/sds100/keymapper/base/constraints/DetectConstraintsUseCaseImpl$Factory;Lio/github/sds100/keymapper/base/keymaps/FingerprintGesturesSupportedUseCase;Lio/github/sds100/keymapper/base/keymaps/PauseKeyMapsUseCase;Lio/github/sds100/keymapper/data/repositories/PreferenceRepository;Lio/github/sds100/keymapper/system/inputmethod/KeyEventRelayServiceWrapper;Lio/github/sds100/keymapper/base/input/InputEventHub;Lio/github/sds100/keymapper/base/trigger/RecordTriggerController;Lio/github/sds100/keymapper/base/expertmode/SystemBridgeSetupAssistantController$Factory;Lio/github/sds100/keymapper/base/system/inputmethod/AutoSwitchImeController$Factory;)V", "Factory", "app_debug"})
public final class AccessibilityServiceController extends io.github.sds100.keymapper.base.system.accessibility.BaseAccessibilityServiceController {
    @org.jetbrains.annotations.NotNull()
    private final io.github.sds100.keymapper.system.accessibility.MyAccessibilityService service = null;
    
    @dagger.assisted.AssistedInject()
    public AccessibilityServiceController(@dagger.assisted.Assisted()
    @org.jetbrains.annotations.NotNull()
    io.github.sds100.keymapper.system.accessibility.MyAccessibilityService service, @org.jetbrains.annotations.NotNull()
    io.github.sds100.keymapper.base.system.accessibility.AccessibilityNodeRecorder.Factory accessibilityNodeRecorderFactory, @org.jetbrains.annotations.NotNull()
    io.github.sds100.keymapper.base.actions.PerformActionsUseCaseImpl.Factory performActionsUseCaseFactory, @org.jetbrains.annotations.NotNull()
    io.github.sds100.keymapper.base.detection.DetectKeyMapsUseCaseImpl.Factory detectKeyMapsUseCaseFactory, @org.jetbrains.annotations.NotNull()
    io.github.sds100.keymapper.base.constraints.DetectConstraintsUseCaseImpl.Factory detectConstraintsUseCaseFactory, @org.jetbrains.annotations.NotNull()
    io.github.sds100.keymapper.base.keymaps.FingerprintGesturesSupportedUseCase fingerprintGesturesSupported, @org.jetbrains.annotations.NotNull()
    io.github.sds100.keymapper.base.keymaps.PauseKeyMapsUseCase pauseKeyMapsUseCase, @org.jetbrains.annotations.NotNull()
    io.github.sds100.keymapper.data.repositories.PreferenceRepository settingsRepository, @org.jetbrains.annotations.NotNull()
    io.github.sds100.keymapper.system.inputmethod.KeyEventRelayServiceWrapper keyEventRelayServiceWrapper, @org.jetbrains.annotations.NotNull()
    io.github.sds100.keymapper.base.input.InputEventHub inputEventHub, @org.jetbrains.annotations.NotNull()
    io.github.sds100.keymapper.base.trigger.RecordTriggerController recordTriggerController, @org.jetbrains.annotations.NotNull()
    io.github.sds100.keymapper.base.expertmode.SystemBridgeSetupAssistantController.Factory setupAssistantControllerFactory, @org.jetbrains.annotations.NotNull()
    io.github.sds100.keymapper.base.system.inputmethod.AutoSwitchImeController.Factory autoSwitchImeControllerFactory) {
        super(null, null, null, null, null, null, null, null, null, null, null, null, null);
    }
    
    @dagger.assisted.AssistedFactory()
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0016\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\bg\u0018\u00002\u00020\u0001J\u0010\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H&\u00a8\u0006\u0006"}, d2 = {"Lio/github/sds100/keymapper/system/accessibility/AccessibilityServiceController$Factory;", "", "create", "Lio/github/sds100/keymapper/system/accessibility/AccessibilityServiceController;", "service", "Lio/github/sds100/keymapper/system/accessibility/MyAccessibilityService;", "app_debug"})
    public static abstract interface Factory {
        
        @org.jetbrains.annotations.NotNull()
        public abstract io.github.sds100.keymapper.system.accessibility.AccessibilityServiceController create(@org.jetbrains.annotations.NotNull()
        io.github.sds100.keymapper.system.accessibility.MyAccessibilityService service);
    }
}