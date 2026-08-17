package io.github.sds100.keymapper.home;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import io.github.sds100.keymapper.base.actions.keyevent.FixKeyEventActionDelegate;
import io.github.sds100.keymapper.base.backup.BackupRestoreMappingsUseCase;
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
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<ListKeyMapsUseCase> listKeyMapsProvider;

  private final Provider<PauseKeyMapsUseCase> pauseKeyMapsProvider;

  private final Provider<BackupRestoreMappingsUseCase> backupRestoreProvider;

  private final Provider<ShowHomeScreenAlertsUseCase> showAlertsUseCaseProvider;

  private final Provider<OnboardingUseCase> onboardingProvider;

  private final Provider<ResourceProvider> resourceProvider;

  private final Provider<SortKeyMapsUseCase> sortKeyMapsProvider;

  private final Provider<ShowInputMethodPickerUseCase> showInputMethodPickerUseCaseProvider;

  private final Provider<SetupAccessibilityServiceDelegate> setupAccessibilityServiceDelegateProvider;

  private final Provider<FixKeyEventActionDelegate> fixKeyEventActionDelegateProvider;

  private final Provider<NavigationProvider> navigationProvider;

  private final Provider<DialogProvider> dialogProvider;

  public HomeViewModel_Factory(Provider<ListKeyMapsUseCase> listKeyMapsProvider,
      Provider<PauseKeyMapsUseCase> pauseKeyMapsProvider,
      Provider<BackupRestoreMappingsUseCase> backupRestoreProvider,
      Provider<ShowHomeScreenAlertsUseCase> showAlertsUseCaseProvider,
      Provider<OnboardingUseCase> onboardingProvider, Provider<ResourceProvider> resourceProvider,
      Provider<SortKeyMapsUseCase> sortKeyMapsProvider,
      Provider<ShowInputMethodPickerUseCase> showInputMethodPickerUseCaseProvider,
      Provider<SetupAccessibilityServiceDelegate> setupAccessibilityServiceDelegateProvider,
      Provider<FixKeyEventActionDelegate> fixKeyEventActionDelegateProvider,
      Provider<NavigationProvider> navigationProvider, Provider<DialogProvider> dialogProvider) {
    this.listKeyMapsProvider = listKeyMapsProvider;
    this.pauseKeyMapsProvider = pauseKeyMapsProvider;
    this.backupRestoreProvider = backupRestoreProvider;
    this.showAlertsUseCaseProvider = showAlertsUseCaseProvider;
    this.onboardingProvider = onboardingProvider;
    this.resourceProvider = resourceProvider;
    this.sortKeyMapsProvider = sortKeyMapsProvider;
    this.showInputMethodPickerUseCaseProvider = showInputMethodPickerUseCaseProvider;
    this.setupAccessibilityServiceDelegateProvider = setupAccessibilityServiceDelegateProvider;
    this.fixKeyEventActionDelegateProvider = fixKeyEventActionDelegateProvider;
    this.navigationProvider = navigationProvider;
    this.dialogProvider = dialogProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(listKeyMapsProvider.get(), pauseKeyMapsProvider.get(), backupRestoreProvider.get(), showAlertsUseCaseProvider.get(), onboardingProvider.get(), resourceProvider.get(), sortKeyMapsProvider.get(), showInputMethodPickerUseCaseProvider.get(), setupAccessibilityServiceDelegateProvider.get(), fixKeyEventActionDelegateProvider.get(), navigationProvider.get(), dialogProvider.get());
  }

  public static HomeViewModel_Factory create(Provider<ListKeyMapsUseCase> listKeyMapsProvider,
      Provider<PauseKeyMapsUseCase> pauseKeyMapsProvider,
      Provider<BackupRestoreMappingsUseCase> backupRestoreProvider,
      Provider<ShowHomeScreenAlertsUseCase> showAlertsUseCaseProvider,
      Provider<OnboardingUseCase> onboardingProvider, Provider<ResourceProvider> resourceProvider,
      Provider<SortKeyMapsUseCase> sortKeyMapsProvider,
      Provider<ShowInputMethodPickerUseCase> showInputMethodPickerUseCaseProvider,
      Provider<SetupAccessibilityServiceDelegate> setupAccessibilityServiceDelegateProvider,
      Provider<FixKeyEventActionDelegate> fixKeyEventActionDelegateProvider,
      Provider<NavigationProvider> navigationProvider, Provider<DialogProvider> dialogProvider) {
    return new HomeViewModel_Factory(listKeyMapsProvider, pauseKeyMapsProvider, backupRestoreProvider, showAlertsUseCaseProvider, onboardingProvider, resourceProvider, sortKeyMapsProvider, showInputMethodPickerUseCaseProvider, setupAccessibilityServiceDelegateProvider, fixKeyEventActionDelegateProvider, navigationProvider, dialogProvider);
  }

  public static HomeViewModel newInstance(ListKeyMapsUseCase listKeyMaps,
      PauseKeyMapsUseCase pauseKeyMaps, BackupRestoreMappingsUseCase backupRestore,
      ShowHomeScreenAlertsUseCase showAlertsUseCase, OnboardingUseCase onboarding,
      ResourceProvider resourceProvider, SortKeyMapsUseCase sortKeyMaps,
      ShowInputMethodPickerUseCase showInputMethodPickerUseCase,
      SetupAccessibilityServiceDelegate setupAccessibilityServiceDelegate,
      FixKeyEventActionDelegate fixKeyEventActionDelegate, NavigationProvider navigationProvider,
      DialogProvider dialogProvider) {
    return new HomeViewModel(listKeyMaps, pauseKeyMaps, backupRestore, showAlertsUseCase, onboarding, resourceProvider, sortKeyMaps, showInputMethodPickerUseCase, setupAccessibilityServiceDelegate, fixKeyEventActionDelegate, navigationProvider, dialogProvider);
  }
}
