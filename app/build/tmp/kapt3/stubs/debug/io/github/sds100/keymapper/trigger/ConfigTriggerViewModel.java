package io.github.sds100.keymapper.trigger;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.github.sds100.keymapper.base.keymaps.DisplayKeyMapUseCase;
import io.github.sds100.keymapper.base.keymaps.FingerprintGesturesSupportedUseCase;
import io.github.sds100.keymapper.base.onboarding.OnboardingTipDelegate;
import io.github.sds100.keymapper.base.onboarding.OnboardingUseCase;
import io.github.sds100.keymapper.base.onboarding.SetupAccessibilityServiceDelegate;
import io.github.sds100.keymapper.base.shortcuts.CreateKeyMapShortcutUseCase;
import io.github.sds100.keymapper.base.trigger.BaseConfigTriggerViewModel;
import io.github.sds100.keymapper.base.trigger.ConfigTriggerUseCase;
import io.github.sds100.keymapper.base.trigger.RecordTriggerController;
import io.github.sds100.keymapper.base.trigger.TriggerSetupDelegate;
import io.github.sds100.keymapper.base.trigger.TriggerSetupShortcut;
import io.github.sds100.keymapper.base.utils.navigation.NavigationProvider;
import io.github.sds100.keymapper.base.utils.ui.DialogProvider;
import io.github.sds100.keymapper.base.utils.ui.ResourceProvider;
import io.github.sds100.keymapper.sysbridge.manager.SystemBridgeConnectionManager;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000n\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\b\u0007\u0018\u00002\u00020\u0001Bo\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\u0006\u0010\n\u001a\u00020\u000b\u0012\u0006\u0010\f\u001a\u00020\r\u0012\u0006\u0010\u000e\u001a\u00020\u000f\u0012\u0006\u0010\u0010\u001a\u00020\u0011\u0012\u0006\u0010\u0012\u001a\u00020\u0013\u0012\u0006\u0010\u0014\u001a\u00020\u0015\u0012\u0006\u0010\u0016\u001a\u00020\u0017\u0012\u0006\u0010\u0018\u001a\u00020\u0019\u0012\u0006\u0010\u001a\u001a\u00020\u001b\u00a2\u0006\u0002\u0010\u001cJ\b\u0010\u001d\u001a\u00020\u001eH\u0016J\b\u0010\u001f\u001a\u00020\u001eH\u0016J\u0018\u0010 \u001a\u00020\u001e2\u0006\u0010!\u001a\u00020\"2\u0006\u0010#\u001a\u00020$H\u0016R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000fX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006%"}, d2 = {"Lio/github/sds100/keymapper/trigger/ConfigTriggerViewModel;", "Lio/github/sds100/keymapper/base/trigger/BaseConfigTriggerViewModel;", "onboarding", "Lio/github/sds100/keymapper/base/onboarding/OnboardingUseCase;", "config", "Lio/github/sds100/keymapper/base/trigger/ConfigTriggerUseCase;", "recordTrigger", "Lio/github/sds100/keymapper/base/trigger/RecordTriggerController;", "createKeyMapShortcut", "Lio/github/sds100/keymapper/base/shortcuts/CreateKeyMapShortcutUseCase;", "displayKeyMap", "Lio/github/sds100/keymapper/base/keymaps/DisplayKeyMapUseCase;", "fingerprintGesturesSupported", "Lio/github/sds100/keymapper/base/keymaps/FingerprintGesturesSupportedUseCase;", "systemBridgeConnectionManager", "Lio/github/sds100/keymapper/sysbridge/manager/SystemBridgeConnectionManager;", "setupAccessibilityServiceDelegate", "Lio/github/sds100/keymapper/base/onboarding/SetupAccessibilityServiceDelegate;", "onboardingTipDelegate", "Lio/github/sds100/keymapper/base/onboarding/OnboardingTipDelegate;", "triggerSetupDelegate", "Lio/github/sds100/keymapper/base/trigger/TriggerSetupDelegate;", "resourceProvider", "Lio/github/sds100/keymapper/base/utils/ui/ResourceProvider;", "navigationProvider", "Lio/github/sds100/keymapper/base/utils/navigation/NavigationProvider;", "dialogProvider", "Lio/github/sds100/keymapper/base/utils/ui/DialogProvider;", "(Lio/github/sds100/keymapper/base/onboarding/OnboardingUseCase;Lio/github/sds100/keymapper/base/trigger/ConfigTriggerUseCase;Lio/github/sds100/keymapper/base/trigger/RecordTriggerController;Lio/github/sds100/keymapper/base/shortcuts/CreateKeyMapShortcutUseCase;Lio/github/sds100/keymapper/base/keymaps/DisplayKeyMapUseCase;Lio/github/sds100/keymapper/base/keymaps/FingerprintGesturesSupportedUseCase;Lio/github/sds100/keymapper/sysbridge/manager/SystemBridgeConnectionManager;Lio/github/sds100/keymapper/base/onboarding/SetupAccessibilityServiceDelegate;Lio/github/sds100/keymapper/base/onboarding/OnboardingTipDelegate;Lio/github/sds100/keymapper/base/trigger/TriggerSetupDelegate;Lio/github/sds100/keymapper/base/utils/ui/ResourceProvider;Lio/github/sds100/keymapper/base/utils/navigation/NavigationProvider;Lio/github/sds100/keymapper/base/utils/ui/DialogProvider;)V", "onEditFloatingButtonClick", "", "onEditFloatingLayoutClick", "showTriggerSetup", "shortcut", "Lio/github/sds100/keymapper/base/trigger/TriggerSetupShortcut;", "forceExpertMode", "", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class ConfigTriggerViewModel extends io.github.sds100.keymapper.base.trigger.BaseConfigTriggerViewModel {
    @org.jetbrains.annotations.NotNull()
    private final io.github.sds100.keymapper.base.onboarding.OnboardingUseCase onboarding = null;
    @org.jetbrains.annotations.NotNull()
    private final io.github.sds100.keymapper.base.trigger.ConfigTriggerUseCase config = null;
    @org.jetbrains.annotations.NotNull()
    private final io.github.sds100.keymapper.base.trigger.RecordTriggerController recordTrigger = null;
    @org.jetbrains.annotations.NotNull()
    private final io.github.sds100.keymapper.base.shortcuts.CreateKeyMapShortcutUseCase createKeyMapShortcut = null;
    @org.jetbrains.annotations.NotNull()
    private final io.github.sds100.keymapper.base.keymaps.DisplayKeyMapUseCase displayKeyMap = null;
    @org.jetbrains.annotations.NotNull()
    private final io.github.sds100.keymapper.base.keymaps.FingerprintGesturesSupportedUseCase fingerprintGesturesSupported = null;
    @org.jetbrains.annotations.NotNull()
    private final io.github.sds100.keymapper.sysbridge.manager.SystemBridgeConnectionManager systemBridgeConnectionManager = null;
    
    @javax.inject.Inject()
    public ConfigTriggerViewModel(@org.jetbrains.annotations.NotNull()
    io.github.sds100.keymapper.base.onboarding.OnboardingUseCase onboarding, @org.jetbrains.annotations.NotNull()
    io.github.sds100.keymapper.base.trigger.ConfigTriggerUseCase config, @org.jetbrains.annotations.NotNull()
    io.github.sds100.keymapper.base.trigger.RecordTriggerController recordTrigger, @org.jetbrains.annotations.NotNull()
    io.github.sds100.keymapper.base.shortcuts.CreateKeyMapShortcutUseCase createKeyMapShortcut, @org.jetbrains.annotations.NotNull()
    io.github.sds100.keymapper.base.keymaps.DisplayKeyMapUseCase displayKeyMap, @org.jetbrains.annotations.NotNull()
    io.github.sds100.keymapper.base.keymaps.FingerprintGesturesSupportedUseCase fingerprintGesturesSupported, @org.jetbrains.annotations.NotNull()
    io.github.sds100.keymapper.sysbridge.manager.SystemBridgeConnectionManager systemBridgeConnectionManager, @org.jetbrains.annotations.NotNull()
    io.github.sds100.keymapper.base.onboarding.SetupAccessibilityServiceDelegate setupAccessibilityServiceDelegate, @org.jetbrains.annotations.NotNull()
    io.github.sds100.keymapper.base.onboarding.OnboardingTipDelegate onboardingTipDelegate, @org.jetbrains.annotations.NotNull()
    io.github.sds100.keymapper.base.trigger.TriggerSetupDelegate triggerSetupDelegate, @org.jetbrains.annotations.NotNull()
    io.github.sds100.keymapper.base.utils.ui.ResourceProvider resourceProvider, @org.jetbrains.annotations.NotNull()
    io.github.sds100.keymapper.base.utils.navigation.NavigationProvider navigationProvider, @org.jetbrains.annotations.NotNull()
    io.github.sds100.keymapper.base.utils.ui.DialogProvider dialogProvider) {
        super(null, null, null, null, null, null, null, null, null, null, null, null, null);
    }
    
    @java.lang.Override()
    public void onEditFloatingButtonClick() {
    }
    
    @java.lang.Override()
    public void onEditFloatingLayoutClick() {
    }
    
    @java.lang.Override()
    public void showTriggerSetup(@org.jetbrains.annotations.NotNull()
    io.github.sds100.keymapper.base.trigger.TriggerSetupShortcut shortcut, boolean forceExpertMode) {
    }
}