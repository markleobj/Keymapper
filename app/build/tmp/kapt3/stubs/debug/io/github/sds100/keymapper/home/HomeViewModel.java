package io.github.sds100.keymapper.home;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.github.sds100.keymapper.base.actions.keyevent.FixKeyEventActionDelegate;
import io.github.sds100.keymapper.base.backup.BackupRestoreMappingsUseCase;
import io.github.sds100.keymapper.base.home.BaseHomeViewModel;
import io.github.sds100.keymapper.base.home.ListKeyMapsUseCase;
import io.github.sds100.keymapper.base.home.ShowHomeScreenAlertsUseCase;
import io.github.sds100.keymapper.base.keymaps.PauseKeyMapsUseCase;
import io.github.sds100.keymapper.base.onboarding.OnboardingUseCase;
import io.github.sds100.keymapper.base.onboarding.SetupAccessibilityServiceDelegate;
import io.github.sds100.keymapper.base.sorting.SortKeyMapsUseCase;
import io.github.sds100.keymapper.base.system.inputmethod.ShowInputMethodPickerUseCase;
import io.github.sds100.keymapper.base.utils.navigation.NavigationProvider;
import io.github.sds100.keymapper.base.utils.ui.DialogProvider;
import io.github.sds100.keymapper.base.utils.ui.ResourceProvider;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000T\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u0007\u0018\u00002\u00020\u0001Bg\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\u0006\u0010\n\u001a\u00020\u000b\u0012\u0006\u0010\f\u001a\u00020\r\u0012\u0006\u0010\u000e\u001a\u00020\u000f\u0012\u0006\u0010\u0010\u001a\u00020\u0011\u0012\u0006\u0010\u0012\u001a\u00020\u0013\u0012\u0006\u0010\u0014\u001a\u00020\u0015\u0012\u0006\u0010\u0016\u001a\u00020\u0017\u0012\u0006\u0010\u0018\u001a\u00020\u0019\u00a2\u0006\u0002\u0010\u001aR\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0011\u0010\u0012\u001a\u00020\u0013\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u001cR\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0011X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000fX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001d"}, d2 = {"Lio/github/sds100/keymapper/home/HomeViewModel;", "Lio/github/sds100/keymapper/base/home/BaseHomeViewModel;", "listKeyMaps", "Lio/github/sds100/keymapper/base/home/ListKeyMapsUseCase;", "pauseKeyMaps", "Lio/github/sds100/keymapper/base/keymaps/PauseKeyMapsUseCase;", "backupRestore", "Lio/github/sds100/keymapper/base/backup/BackupRestoreMappingsUseCase;", "showAlertsUseCase", "Lio/github/sds100/keymapper/base/home/ShowHomeScreenAlertsUseCase;", "onboarding", "Lio/github/sds100/keymapper/base/onboarding/OnboardingUseCase;", "resourceProvider", "Lio/github/sds100/keymapper/base/utils/ui/ResourceProvider;", "sortKeyMaps", "Lio/github/sds100/keymapper/base/sorting/SortKeyMapsUseCase;", "showInputMethodPickerUseCase", "Lio/github/sds100/keymapper/base/system/inputmethod/ShowInputMethodPickerUseCase;", "setupAccessibilityServiceDelegate", "Lio/github/sds100/keymapper/base/onboarding/SetupAccessibilityServiceDelegate;", "fixKeyEventActionDelegate", "Lio/github/sds100/keymapper/base/actions/keyevent/FixKeyEventActionDelegate;", "navigationProvider", "Lio/github/sds100/keymapper/base/utils/navigation/NavigationProvider;", "dialogProvider", "Lio/github/sds100/keymapper/base/utils/ui/DialogProvider;", "(Lio/github/sds100/keymapper/base/home/ListKeyMapsUseCase;Lio/github/sds100/keymapper/base/keymaps/PauseKeyMapsUseCase;Lio/github/sds100/keymapper/base/backup/BackupRestoreMappingsUseCase;Lio/github/sds100/keymapper/base/home/ShowHomeScreenAlertsUseCase;Lio/github/sds100/keymapper/base/onboarding/OnboardingUseCase;Lio/github/sds100/keymapper/base/utils/ui/ResourceProvider;Lio/github/sds100/keymapper/base/sorting/SortKeyMapsUseCase;Lio/github/sds100/keymapper/base/system/inputmethod/ShowInputMethodPickerUseCase;Lio/github/sds100/keymapper/base/onboarding/SetupAccessibilityServiceDelegate;Lio/github/sds100/keymapper/base/actions/keyevent/FixKeyEventActionDelegate;Lio/github/sds100/keymapper/base/utils/navigation/NavigationProvider;Lio/github/sds100/keymapper/base/utils/ui/DialogProvider;)V", "getSetupAccessibilityServiceDelegate", "()Lio/github/sds100/keymapper/base/onboarding/SetupAccessibilityServiceDelegate;", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class HomeViewModel extends io.github.sds100.keymapper.base.home.BaseHomeViewModel {
    @org.jetbrains.annotations.NotNull()
    private final io.github.sds100.keymapper.base.home.ListKeyMapsUseCase listKeyMaps = null;
    @org.jetbrains.annotations.NotNull()
    private final io.github.sds100.keymapper.base.keymaps.PauseKeyMapsUseCase pauseKeyMaps = null;
    @org.jetbrains.annotations.NotNull()
    private final io.github.sds100.keymapper.base.backup.BackupRestoreMappingsUseCase backupRestore = null;
    @org.jetbrains.annotations.NotNull()
    private final io.github.sds100.keymapper.base.home.ShowHomeScreenAlertsUseCase showAlertsUseCase = null;
    @org.jetbrains.annotations.NotNull()
    private final io.github.sds100.keymapper.base.onboarding.OnboardingUseCase onboarding = null;
    @org.jetbrains.annotations.NotNull()
    private final io.github.sds100.keymapper.base.sorting.SortKeyMapsUseCase sortKeyMaps = null;
    @org.jetbrains.annotations.NotNull()
    private final io.github.sds100.keymapper.base.system.inputmethod.ShowInputMethodPickerUseCase showInputMethodPickerUseCase = null;
    @org.jetbrains.annotations.NotNull()
    private final io.github.sds100.keymapper.base.onboarding.SetupAccessibilityServiceDelegate setupAccessibilityServiceDelegate = null;
    
    @javax.inject.Inject()
    public HomeViewModel(@org.jetbrains.annotations.NotNull()
    io.github.sds100.keymapper.base.home.ListKeyMapsUseCase listKeyMaps, @org.jetbrains.annotations.NotNull()
    io.github.sds100.keymapper.base.keymaps.PauseKeyMapsUseCase pauseKeyMaps, @org.jetbrains.annotations.NotNull()
    io.github.sds100.keymapper.base.backup.BackupRestoreMappingsUseCase backupRestore, @org.jetbrains.annotations.NotNull()
    io.github.sds100.keymapper.base.home.ShowHomeScreenAlertsUseCase showAlertsUseCase, @org.jetbrains.annotations.NotNull()
    io.github.sds100.keymapper.base.onboarding.OnboardingUseCase onboarding, @org.jetbrains.annotations.NotNull()
    io.github.sds100.keymapper.base.utils.ui.ResourceProvider resourceProvider, @org.jetbrains.annotations.NotNull()
    io.github.sds100.keymapper.base.sorting.SortKeyMapsUseCase sortKeyMaps, @org.jetbrains.annotations.NotNull()
    io.github.sds100.keymapper.base.system.inputmethod.ShowInputMethodPickerUseCase showInputMethodPickerUseCase, @org.jetbrains.annotations.NotNull()
    io.github.sds100.keymapper.base.onboarding.SetupAccessibilityServiceDelegate setupAccessibilityServiceDelegate, @org.jetbrains.annotations.NotNull()
    io.github.sds100.keymapper.base.actions.keyevent.FixKeyEventActionDelegate fixKeyEventActionDelegate, @org.jetbrains.annotations.NotNull()
    io.github.sds100.keymapper.base.utils.navigation.NavigationProvider navigationProvider, @org.jetbrains.annotations.NotNull()
    io.github.sds100.keymapper.base.utils.ui.DialogProvider dialogProvider) {
        super(null, null, null, null, null, null, null, null, null, null, null, null);
    }
    
    @org.jetbrains.annotations.NotNull()
    public final io.github.sds100.keymapper.base.onboarding.SetupAccessibilityServiceDelegate getSetupAccessibilityServiceDelegate() {
        return null;
    }
}