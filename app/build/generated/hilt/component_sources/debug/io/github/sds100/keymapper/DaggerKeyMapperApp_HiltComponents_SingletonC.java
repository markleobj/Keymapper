package io.github.sds100.keymapper;

import android.app.Activity;
import android.app.Service;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.LazyClassKeyMap;
import dagger.internal.MapBuilder;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.SingleCheck;
import io.github.sds100.keymapper.api.EnableKeyMapsBroadcastReceiver;
import io.github.sds100.keymapper.api.EnableKeyMapsBroadcastReceiver_MembersInjector;
import io.github.sds100.keymapper.api.KeyMapShortcutActivityIntentBuilderImpl;
import io.github.sds100.keymapper.api.LaunchKeyMapShortcutActivity;
import io.github.sds100.keymapper.api.LaunchKeyMapShortcutActivity_MembersInjector;
import io.github.sds100.keymapper.api.PauseMappingsBroadcastReceiver;
import io.github.sds100.keymapper.api.PauseMappingsBroadcastReceiver_MembersInjector;
import io.github.sds100.keymapper.api.TriggerKeyMapsBroadcastReceiver;
import io.github.sds100.keymapper.api.TriggerKeyMapsBroadcastReceiver_MembersInjector;
import io.github.sds100.keymapper.base.ActivityViewModel;
import io.github.sds100.keymapper.base.ActivityViewModel_HiltModules;
import io.github.sds100.keymapper.base.ActivityViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import io.github.sds100.keymapper.base.ActivityViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import io.github.sds100.keymapper.base.BaseKeyMapperApp_MembersInjector;
import io.github.sds100.keymapper.base.BaseMainActivity_MembersInjector;
import io.github.sds100.keymapper.base.ViewModelModule;
import io.github.sds100.keymapper.base.ViewModelModule_ProvideViewModelScopeFactory;
import io.github.sds100.keymapper.base.about.AboutFragment;
import io.github.sds100.keymapper.base.about.AboutFragment_MembersInjector;
import io.github.sds100.keymapper.base.actions.ChooseActionViewModel;
import io.github.sds100.keymapper.base.actions.ChooseActionViewModel_HiltModules;
import io.github.sds100.keymapper.base.actions.ChooseActionViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import io.github.sds100.keymapper.base.actions.ChooseActionViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import io.github.sds100.keymapper.base.actions.ChooseSettingViewModel;
import io.github.sds100.keymapper.base.actions.ChooseSettingViewModel_HiltModules;
import io.github.sds100.keymapper.base.actions.ChooseSettingViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import io.github.sds100.keymapper.base.actions.ChooseSettingViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import io.github.sds100.keymapper.base.actions.ConfigActionsUseCaseImpl;
import io.github.sds100.keymapper.base.actions.ConfigActionsViewModel;
import io.github.sds100.keymapper.base.actions.ConfigActionsViewModel_HiltModules;
import io.github.sds100.keymapper.base.actions.ConfigActionsViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import io.github.sds100.keymapper.base.actions.ConfigActionsViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import io.github.sds100.keymapper.base.actions.ConfigShellCommandViewModel;
import io.github.sds100.keymapper.base.actions.ConfigShellCommandViewModel_HiltModules;
import io.github.sds100.keymapper.base.actions.ConfigShellCommandViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import io.github.sds100.keymapper.base.actions.ConfigShellCommandViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import io.github.sds100.keymapper.base.actions.CreateActionUseCase;
import io.github.sds100.keymapper.base.actions.CreateActionUseCaseImpl;
import io.github.sds100.keymapper.base.actions.ExecuteShellCommandUseCase;
import io.github.sds100.keymapper.base.actions.GetActionErrorUseCaseImpl;
import io.github.sds100.keymapper.base.actions.PerformActionsUseCaseImpl;
import io.github.sds100.keymapper.base.actions.TestActionUseCase;
import io.github.sds100.keymapper.base.actions.TestActionUseCaseImpl;
import io.github.sds100.keymapper.base.actions.keyevent.ChooseKeyCodeFragment;
import io.github.sds100.keymapper.base.actions.keyevent.ChooseKeyCodeViewModel;
import io.github.sds100.keymapper.base.actions.keyevent.ChooseKeyCodeViewModel_HiltModules;
import io.github.sds100.keymapper.base.actions.keyevent.ChooseKeyCodeViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import io.github.sds100.keymapper.base.actions.keyevent.ChooseKeyCodeViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import io.github.sds100.keymapper.base.actions.keyevent.ConfigKeyEventActionFragment;
import io.github.sds100.keymapper.base.actions.keyevent.ConfigKeyEventActionViewModel;
import io.github.sds100.keymapper.base.actions.keyevent.ConfigKeyEventActionViewModel_HiltModules;
import io.github.sds100.keymapper.base.actions.keyevent.ConfigKeyEventActionViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import io.github.sds100.keymapper.base.actions.keyevent.ConfigKeyEventActionViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import io.github.sds100.keymapper.base.actions.keyevent.ConfigKeyEventUseCase;
import io.github.sds100.keymapper.base.actions.keyevent.ConfigKeyEventUseCaseImpl;
import io.github.sds100.keymapper.base.actions.keyevent.FixKeyEventActionDelegateImpl;
import io.github.sds100.keymapper.base.actions.pinchscreen.PinchPickDisplayCoordinateFragment;
import io.github.sds100.keymapper.base.actions.pinchscreen.PinchPickDisplayCoordinateViewModel;
import io.github.sds100.keymapper.base.actions.pinchscreen.PinchPickDisplayCoordinateViewModel_HiltModules;
import io.github.sds100.keymapper.base.actions.pinchscreen.PinchPickDisplayCoordinateViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import io.github.sds100.keymapper.base.actions.pinchscreen.PinchPickDisplayCoordinateViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import io.github.sds100.keymapper.base.actions.sound.ChooseSoundFileFragment;
import io.github.sds100.keymapper.base.actions.sound.ChooseSoundFileUseCase;
import io.github.sds100.keymapper.base.actions.sound.ChooseSoundFileUseCaseImpl;
import io.github.sds100.keymapper.base.actions.sound.ChooseSoundFileViewModel;
import io.github.sds100.keymapper.base.actions.sound.ChooseSoundFileViewModel_HiltModules;
import io.github.sds100.keymapper.base.actions.sound.ChooseSoundFileViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import io.github.sds100.keymapper.base.actions.sound.ChooseSoundFileViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import io.github.sds100.keymapper.base.actions.sound.SoundsManagerImpl;
import io.github.sds100.keymapper.base.actions.swipescreen.SwipePickDisplayCoordinateFragment;
import io.github.sds100.keymapper.base.actions.swipescreen.SwipePickDisplayCoordinateViewModel;
import io.github.sds100.keymapper.base.actions.swipescreen.SwipePickDisplayCoordinateViewModel_HiltModules;
import io.github.sds100.keymapper.base.actions.swipescreen.SwipePickDisplayCoordinateViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import io.github.sds100.keymapper.base.actions.swipescreen.SwipePickDisplayCoordinateViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import io.github.sds100.keymapper.base.actions.tapscreen.PickDisplayCoordinateFragment;
import io.github.sds100.keymapper.base.actions.tapscreen.PickDisplayCoordinateViewModel;
import io.github.sds100.keymapper.base.actions.tapscreen.PickDisplayCoordinateViewModel_HiltModules;
import io.github.sds100.keymapper.base.actions.tapscreen.PickDisplayCoordinateViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import io.github.sds100.keymapper.base.actions.tapscreen.PickDisplayCoordinateViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import io.github.sds100.keymapper.base.actions.uielement.InteractUiElementController;
import io.github.sds100.keymapper.base.actions.uielement.InteractUiElementViewModel;
import io.github.sds100.keymapper.base.actions.uielement.InteractUiElementViewModel_HiltModules;
import io.github.sds100.keymapper.base.actions.uielement.InteractUiElementViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import io.github.sds100.keymapper.base.actions.uielement.InteractUiElementViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import io.github.sds100.keymapper.base.backup.BackupManagerImpl;
import io.github.sds100.keymapper.base.backup.BackupRestoreMappingsUseCase;
import io.github.sds100.keymapper.base.backup.BackupRestoreMappingsUseCaseImpl;
import io.github.sds100.keymapper.base.backup.RestoreKeyMapsActivity;
import io.github.sds100.keymapper.base.backup.RestoreKeyMapsActivity_MembersInjector;
import io.github.sds100.keymapper.base.backup.RestoreKeyMapsViewModel;
import io.github.sds100.keymapper.base.backup.RestoreKeyMapsViewModel_HiltModules;
import io.github.sds100.keymapper.base.backup.RestoreKeyMapsViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import io.github.sds100.keymapper.base.backup.RestoreKeyMapsViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import io.github.sds100.keymapper.base.constraints.ChooseConstraintViewModel;
import io.github.sds100.keymapper.base.constraints.ChooseConstraintViewModel_HiltModules;
import io.github.sds100.keymapper.base.constraints.ChooseConstraintViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import io.github.sds100.keymapper.base.constraints.ChooseConstraintViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import io.github.sds100.keymapper.base.constraints.ConfigConstraintsUseCaseImpl;
import io.github.sds100.keymapper.base.constraints.ConfigConstraintsViewModel;
import io.github.sds100.keymapper.base.constraints.ConfigConstraintsViewModel_HiltModules;
import io.github.sds100.keymapper.base.constraints.ConfigConstraintsViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import io.github.sds100.keymapper.base.constraints.ConfigConstraintsViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import io.github.sds100.keymapper.base.constraints.CreateConstraintUseCase;
import io.github.sds100.keymapper.base.constraints.CreateConstraintUseCaseImpl;
import io.github.sds100.keymapper.base.constraints.DetectConstraintsUseCaseImpl;
import io.github.sds100.keymapper.base.constraints.GetConstraintErrorUseCaseImpl;
import io.github.sds100.keymapper.base.debug.GetEventRecorderImpl;
import io.github.sds100.keymapper.base.debug.GetEventViewModel;
import io.github.sds100.keymapper.base.debug.GetEventViewModel_HiltModules;
import io.github.sds100.keymapper.base.debug.GetEventViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import io.github.sds100.keymapper.base.debug.GetEventViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import io.github.sds100.keymapper.base.detection.DetectKeyMapsUseCaseImpl;
import io.github.sds100.keymapper.base.expertmode.ExpertModeSetupDelegateImpl;
import io.github.sds100.keymapper.base.expertmode.ExpertModeSetupViewModel;
import io.github.sds100.keymapper.base.expertmode.ExpertModeSetupViewModel_HiltModules;
import io.github.sds100.keymapper.base.expertmode.ExpertModeSetupViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import io.github.sds100.keymapper.base.expertmode.ExpertModeSetupViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import io.github.sds100.keymapper.base.expertmode.ExpertModeViewModel;
import io.github.sds100.keymapper.base.expertmode.ExpertModeViewModel_HiltModules;
import io.github.sds100.keymapper.base.expertmode.ExpertModeViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import io.github.sds100.keymapper.base.expertmode.ExpertModeViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import io.github.sds100.keymapper.base.expertmode.SystemBridgeAutoStarter;
import io.github.sds100.keymapper.base.expertmode.SystemBridgeConfigSync;
import io.github.sds100.keymapper.base.expertmode.SystemBridgeSetupAssistantController;
import io.github.sds100.keymapper.base.expertmode.SystemBridgeSetupUseCaseImpl;
import io.github.sds100.keymapper.base.home.ListKeyMapsUseCase;
import io.github.sds100.keymapper.base.home.ListKeyMapsUseCaseImpl;
import io.github.sds100.keymapper.base.home.ShowHomeScreenAlertsUseCase;
import io.github.sds100.keymapper.base.home.ShowHomeScreenAlertsUseCaseImpl;
import io.github.sds100.keymapper.base.input.EvdevDevicesDelegate;
import io.github.sds100.keymapper.base.input.InputEventHubImpl;
import io.github.sds100.keymapper.base.keymaps.ConfigKeyMapStateImpl;
import io.github.sds100.keymapper.base.keymaps.ConfigKeyMapViewModel;
import io.github.sds100.keymapper.base.keymaps.ConfigKeyMapViewModel_HiltModules;
import io.github.sds100.keymapper.base.keymaps.ConfigKeyMapViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import io.github.sds100.keymapper.base.keymaps.ConfigKeyMapViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import io.github.sds100.keymapper.base.keymaps.DisplayKeyMapUseCaseImpl;
import io.github.sds100.keymapper.base.keymaps.EnableKeyMapsUseCaseImpl;
import io.github.sds100.keymapper.base.keymaps.FingerprintGesturesSupportedUseCaseImpl;
import io.github.sds100.keymapper.base.keymaps.GetDefaultKeyMapOptionsUseCaseImpl;
import io.github.sds100.keymapper.base.keymaps.PauseKeyMapsUseCaseImpl;
import io.github.sds100.keymapper.base.logging.DisplayLogUseCase;
import io.github.sds100.keymapper.base.logging.DisplayLogUseCaseImpl;
import io.github.sds100.keymapper.base.logging.KeyMapperLoggingTree;
import io.github.sds100.keymapper.base.logging.LogViewModel;
import io.github.sds100.keymapper.base.logging.LogViewModel_HiltModules;
import io.github.sds100.keymapper.base.logging.LogViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import io.github.sds100.keymapper.base.logging.LogViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import io.github.sds100.keymapper.base.logging.ShareLogcatUseCaseImpl;
import io.github.sds100.keymapper.base.logging.SystemBridgeLogger;
import io.github.sds100.keymapper.base.onboarding.OnboardingTipDelegateImpl;
import io.github.sds100.keymapper.base.onboarding.OnboardingUseCaseImpl;
import io.github.sds100.keymapper.base.onboarding.SetupAccessibilityServiceDelegateImpl;
import io.github.sds100.keymapper.base.purchasing.PurchasingManager;
import io.github.sds100.keymapper.base.settings.ConfigSettingsUseCase;
import io.github.sds100.keymapper.base.settings.ConfigSettingsUseCaseImpl;
import io.github.sds100.keymapper.base.settings.SettingsViewModel;
import io.github.sds100.keymapper.base.settings.SettingsViewModel_HiltModules;
import io.github.sds100.keymapper.base.settings.SettingsViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import io.github.sds100.keymapper.base.settings.SettingsViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import io.github.sds100.keymapper.base.shortcuts.CreateKeyMapShortcutActivity;
import io.github.sds100.keymapper.base.shortcuts.CreateKeyMapShortcutActivity_MembersInjector;
import io.github.sds100.keymapper.base.shortcuts.CreateKeyMapShortcutUseCaseImpl;
import io.github.sds100.keymapper.base.shortcuts.CreateKeyMapShortcutViewModel;
import io.github.sds100.keymapper.base.shortcuts.CreateKeyMapShortcutViewModel_HiltModules;
import io.github.sds100.keymapper.base.shortcuts.CreateKeyMapShortcutViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import io.github.sds100.keymapper.base.shortcuts.CreateKeyMapShortcutViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import io.github.sds100.keymapper.base.sorting.SortKeyMapsUseCase;
import io.github.sds100.keymapper.base.sorting.SortKeyMapsUseCaseImpl;
import io.github.sds100.keymapper.base.system.accessibility.AccessibilityNodeRecorder;
import io.github.sds100.keymapper.base.system.accessibility.AccessibilityServiceAdapterImpl;
import io.github.sds100.keymapper.base.system.accessibility.BaseAccessibilityService;
import io.github.sds100.keymapper.base.system.accessibility.BaseAccessibilityService_MembersInjector;
import io.github.sds100.keymapper.base.system.accessibility.ControlAccessibilityServiceUseCaseImpl;
import io.github.sds100.keymapper.base.system.accessibility.IAccessibilityService;
import io.github.sds100.keymapper.base.system.apps.ChooseActivityFragment;
import io.github.sds100.keymapper.base.system.apps.ChooseActivityViewModel;
import io.github.sds100.keymapper.base.system.apps.ChooseActivityViewModel_HiltModules;
import io.github.sds100.keymapper.base.system.apps.ChooseActivityViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import io.github.sds100.keymapper.base.system.apps.ChooseActivityViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import io.github.sds100.keymapper.base.system.apps.ChooseAppFragment;
import io.github.sds100.keymapper.base.system.apps.ChooseAppShortcutFragment;
import io.github.sds100.keymapper.base.system.apps.ChooseAppShortcutViewModel;
import io.github.sds100.keymapper.base.system.apps.ChooseAppShortcutViewModel_HiltModules;
import io.github.sds100.keymapper.base.system.apps.ChooseAppShortcutViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import io.github.sds100.keymapper.base.system.apps.ChooseAppShortcutViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import io.github.sds100.keymapper.base.system.apps.ChooseAppViewModel;
import io.github.sds100.keymapper.base.system.apps.ChooseAppViewModel_HiltModules;
import io.github.sds100.keymapper.base.system.apps.ChooseAppViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import io.github.sds100.keymapper.base.system.apps.ChooseAppViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import io.github.sds100.keymapper.base.system.apps.DisplayAppShortcutsUseCase;
import io.github.sds100.keymapper.base.system.apps.DisplayAppShortcutsUseCaseImpl;
import io.github.sds100.keymapper.base.system.apps.DisplayAppsUseCase;
import io.github.sds100.keymapper.base.system.apps.DisplayAppsUseCaseImpl;
import io.github.sds100.keymapper.base.system.bluetooth.ChooseBluetoothDeviceFragment;
import io.github.sds100.keymapper.base.system.bluetooth.ChooseBluetoothDeviceUseCase;
import io.github.sds100.keymapper.base.system.bluetooth.ChooseBluetoothDeviceUseCaseImpl;
import io.github.sds100.keymapper.base.system.bluetooth.ChooseBluetoothDeviceViewModel;
import io.github.sds100.keymapper.base.system.bluetooth.ChooseBluetoothDeviceViewModel_HiltModules;
import io.github.sds100.keymapper.base.system.bluetooth.ChooseBluetoothDeviceViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import io.github.sds100.keymapper.base.system.bluetooth.ChooseBluetoothDeviceViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import io.github.sds100.keymapper.base.system.inputmethod.AutoSwitchImeController;
import io.github.sds100.keymapper.base.system.inputmethod.ImeInputEventInjectorImpl;
import io.github.sds100.keymapper.base.system.inputmethod.ShowHideInputMethodUseCase;
import io.github.sds100.keymapper.base.system.inputmethod.ShowHideInputMethodUseCaseImpl;
import io.github.sds100.keymapper.base.system.inputmethod.ShowInputMethodPickerUseCase;
import io.github.sds100.keymapper.base.system.inputmethod.ShowInputMethodPickerUseCaseImpl;
import io.github.sds100.keymapper.base.system.inputmethod.SwitchImeAsyncImpl;
import io.github.sds100.keymapper.base.system.inputmethod.ToggleCompatibleImeUseCaseImpl;
import io.github.sds100.keymapper.base.system.intents.ConfigIntentFragment;
import io.github.sds100.keymapper.base.system.intents.ConfigIntentViewModel;
import io.github.sds100.keymapper.base.system.intents.ConfigIntentViewModel_HiltModules;
import io.github.sds100.keymapper.base.system.intents.ConfigIntentViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import io.github.sds100.keymapper.base.system.intents.ConfigIntentViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import io.github.sds100.keymapper.base.system.notifications.AndroidNotificationAdapter;
import io.github.sds100.keymapper.base.system.notifications.ManageNotificationsUseCase;
import io.github.sds100.keymapper.base.system.notifications.ManageNotificationsUseCaseImpl;
import io.github.sds100.keymapper.base.system.notifications.NotificationController;
import io.github.sds100.keymapper.base.system.permissions.AutoGrantPermissionController;
import io.github.sds100.keymapper.base.trigger.ConfigTriggerUseCaseImpl;
import io.github.sds100.keymapper.base.trigger.RecordTriggerControllerImpl;
import io.github.sds100.keymapper.base.trigger.SetupInputMethodUseCaseImpl;
import io.github.sds100.keymapper.base.trigger.TriggerSetupDelegateImpl;
import io.github.sds100.keymapper.base.utils.navigation.NavigationProviderImpl;
import io.github.sds100.keymapper.base.utils.ui.DialogProviderImpl;
import io.github.sds100.keymapper.base.utils.ui.ResourceProviderImpl;
import io.github.sds100.keymapper.common.BuildConfigProvider;
import io.github.sds100.keymapper.common.KeyMapperClassProvider;
import io.github.sds100.keymapper.common.utils.ClockImpl;
import io.github.sds100.keymapper.common.utils.DefaultUuidGenerator;
import io.github.sds100.keymapper.common.utils.DispatcherProvider;
import io.github.sds100.keymapper.common.utils.UuidGenerator;
import io.github.sds100.keymapper.data.db.AppDatabase;
import io.github.sds100.keymapper.data.db.AppDatabaseModule;
import io.github.sds100.keymapper.data.db.AppDatabaseModule_ProvideAccessibilityNodeDaoFactory;
import io.github.sds100.keymapper.data.db.AppDatabaseModule_ProvideAppDatabaseFactory;
import io.github.sds100.keymapper.data.db.AppDatabaseModule_ProvideFingerprintMapDaoFactory;
import io.github.sds100.keymapper.data.db.AppDatabaseModule_ProvideFloatingButtonDaoFactory;
import io.github.sds100.keymapper.data.db.AppDatabaseModule_ProvideFloatingLayoutDaoFactory;
import io.github.sds100.keymapper.data.db.AppDatabaseModule_ProvideGroupDaoFactory;
import io.github.sds100.keymapper.data.db.AppDatabaseModule_ProvideKeyMapDaoFactory;
import io.github.sds100.keymapper.data.db.AppDatabaseModule_ProvideLogEntryDaoFactory;
import io.github.sds100.keymapper.data.db.dao.AccessibilityNodeDao;
import io.github.sds100.keymapper.data.db.dao.FingerprintMapDao;
import io.github.sds100.keymapper.data.db.dao.FloatingButtonDao;
import io.github.sds100.keymapper.data.db.dao.FloatingLayoutDao;
import io.github.sds100.keymapper.data.db.dao.GroupDao;
import io.github.sds100.keymapper.data.db.dao.KeyMapDao;
import io.github.sds100.keymapper.data.db.dao.LogEntryDao;
import io.github.sds100.keymapper.data.repositories.LogRepository;
import io.github.sds100.keymapper.data.repositories.PreferenceRepositoryImpl;
import io.github.sds100.keymapper.data.repositories.RoomAccessibilityNodeRepository;
import io.github.sds100.keymapper.data.repositories.RoomFloatingButtonRepository;
import io.github.sds100.keymapper.data.repositories.RoomFloatingLayoutRepository;
import io.github.sds100.keymapper.data.repositories.RoomGroupRepository;
import io.github.sds100.keymapper.data.repositories.RoomKeyMapRepository;
import io.github.sds100.keymapper.data.repositories.RoomLogRepository;
import io.github.sds100.keymapper.home.HomeViewModel;
import io.github.sds100.keymapper.home.HomeViewModel_HiltModules;
import io.github.sds100.keymapper.home.HomeViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import io.github.sds100.keymapper.home.HomeViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import io.github.sds100.keymapper.sysbridge.adb.AdbManagerImpl;
import io.github.sds100.keymapper.sysbridge.manager.SystemBridgeConnectionManagerImpl;
import io.github.sds100.keymapper.sysbridge.service.SystemBridgeSetupControllerImpl;
import io.github.sds100.keymapper.sysbridge.starter.SystemBridgeStarter;
import io.github.sds100.keymapper.system.AndroidSystemFeatureAdapter;
import io.github.sds100.keymapper.system.accessibility.AccessibilityServiceController;
import io.github.sds100.keymapper.system.accessibility.MyAccessibilityService;
import io.github.sds100.keymapper.system.accessibility.MyAccessibilityService_MembersInjector;
import io.github.sds100.keymapper.system.accessibility.ObserveEnabledAccessibilityServicesJob;
import io.github.sds100.keymapper.system.accessibility.ObserveEnabledAccessibilityServicesJob_MembersInjector;
import io.github.sds100.keymapper.system.airplanemode.AndroidAirplaneModeAdapter;
import io.github.sds100.keymapper.system.apps.AndroidAppShortcutAdapter;
import io.github.sds100.keymapper.system.apps.AndroidPackageManagerAdapter;
import io.github.sds100.keymapper.system.bluetooth.AndroidBluetoothAdapter;
import io.github.sds100.keymapper.system.bluetooth.BluetoothBroadcastReceiver;
import io.github.sds100.keymapper.system.bluetooth.BluetoothBroadcastReceiver_MembersInjector;
import io.github.sds100.keymapper.system.camera.AndroidCameraAdapter;
import io.github.sds100.keymapper.system.clipboard.AndroidClipboardAdapter;
import io.github.sds100.keymapper.system.devices.AndroidDevicesAdapter;
import io.github.sds100.keymapper.system.display.AndroidDisplayAdapter;
import io.github.sds100.keymapper.system.files.AndroidFileAdapter;
import io.github.sds100.keymapper.system.foldable.AndroidFoldableAdapter;
import io.github.sds100.keymapper.system.inputmethod.AndroidInputMethodAdapter;
import io.github.sds100.keymapper.system.inputmethod.InputMethodAdapter;
import io.github.sds100.keymapper.system.inputmethod.KeyEventRelayServiceWrapperImpl;
import io.github.sds100.keymapper.system.inputmethod.KeyMapperImeService;
import io.github.sds100.keymapper.system.inputmethod.KeyMapperImeService_MembersInjector;
import io.github.sds100.keymapper.system.inputmethod.ObserveInputMethodsJob;
import io.github.sds100.keymapper.system.inputmethod.ObserveInputMethodsJob_MembersInjector;
import io.github.sds100.keymapper.system.intents.IntentAdapterImpl;
import io.github.sds100.keymapper.system.lock.AndroidLockScreenAdapter;
import io.github.sds100.keymapper.system.lock.LockScreenAdapter;
import io.github.sds100.keymapper.system.media.AndroidMediaAdapter;
import io.github.sds100.keymapper.system.network.AndroidNetworkAdapter;
import io.github.sds100.keymapper.system.nfc.AndroidNfcAdapter;
import io.github.sds100.keymapper.system.notifications.NotificationReceiver;
import io.github.sds100.keymapper.system.notifications.NotificationReceiverAdapterImpl;
import io.github.sds100.keymapper.system.notifications.NotificationReceiver_MembersInjector;
import io.github.sds100.keymapper.system.notifications.ObserveNotificationListenersJob;
import io.github.sds100.keymapper.system.notifications.ObserveNotificationListenersJob_MembersInjector;
import io.github.sds100.keymapper.system.permissions.AndroidPermissionAdapter;
import io.github.sds100.keymapper.system.phone.AndroidPhoneAdapter;
import io.github.sds100.keymapper.system.popup.AndroidToastAdapter;
import io.github.sds100.keymapper.system.power.AndroidPowerAdapter;
import io.github.sds100.keymapper.system.ringtones.AndroidRingtoneAdapter;
import io.github.sds100.keymapper.system.root.SuAdapterImpl;
import io.github.sds100.keymapper.system.settings.AndroidSettingsAdapter;
import io.github.sds100.keymapper.system.shell.StandardShellAdapter;
import io.github.sds100.keymapper.system.shizuku.ShizukuAdapterImpl;
import io.github.sds100.keymapper.system.url.AndroidOpenUrlAdapter;
import io.github.sds100.keymapper.system.vibrator.AndroidVibratorAdapter;
import io.github.sds100.keymapper.system.volume.AndroidVolumeAdapter;
import io.github.sds100.keymapper.tiles.ToggleKeyMapperKeyboardTile;
import io.github.sds100.keymapper.tiles.ToggleKeyMapperKeyboardTile_MembersInjector;
import io.github.sds100.keymapper.tiles.ToggleMappingsTile;
import io.github.sds100.keymapper.tiles.ToggleMappingsTile_MembersInjector;
import io.github.sds100.keymapper.trigger.ConfigTriggerViewModel;
import io.github.sds100.keymapper.trigger.ConfigTriggerViewModel_HiltModules;
import io.github.sds100.keymapper.trigger.ConfigTriggerViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import io.github.sds100.keymapper.trigger.ConfigTriggerViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;
import kotlinx.coroutines.CoroutineScope;

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
public final class DaggerKeyMapperApp_HiltComponents_SingletonC {
  private DaggerKeyMapperApp_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private AppDatabaseModule appDatabaseModule;

    private AppHiltModule appHiltModule;

    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder appDatabaseModule(AppDatabaseModule appDatabaseModule) {
      this.appDatabaseModule = Preconditions.checkNotNull(appDatabaseModule);
      return this;
    }

    public Builder appHiltModule(AppHiltModule appHiltModule) {
      this.appHiltModule = Preconditions.checkNotNull(appHiltModule);
      return this;
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public KeyMapperApp_HiltComponents.SingletonC build() {
      if (appDatabaseModule == null) {
        this.appDatabaseModule = new AppDatabaseModule();
      }
      if (appHiltModule == null) {
        this.appHiltModule = new AppHiltModule();
      }
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(appDatabaseModule, appHiltModule, applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements KeyMapperApp_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public KeyMapperApp_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements KeyMapperApp_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public KeyMapperApp_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements KeyMapperApp_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public KeyMapperApp_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements KeyMapperApp_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public KeyMapperApp_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements KeyMapperApp_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public KeyMapperApp_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements KeyMapperApp_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public KeyMapperApp_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, new ViewModelModule(), savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements KeyMapperApp_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public KeyMapperApp_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends KeyMapperApp_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends KeyMapperApp_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    FragmentCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }

    @Override
    public void injectMainFragment(MainFragment arg0) {
      injectMainFragment2(arg0);
    }

    @Override
    public void injectAboutFragment(AboutFragment arg0) {
      injectAboutFragment2(arg0);
    }

    @Override
    public void injectChooseKeyCodeFragment(ChooseKeyCodeFragment arg0) {
    }

    @Override
    public void injectConfigKeyEventActionFragment(ConfigKeyEventActionFragment arg0) {
    }

    @Override
    public void injectPinchPickDisplayCoordinateFragment(PinchPickDisplayCoordinateFragment arg0) {
    }

    @Override
    public void injectChooseSoundFileFragment(ChooseSoundFileFragment arg0) {
    }

    @Override
    public void injectSwipePickDisplayCoordinateFragment(SwipePickDisplayCoordinateFragment arg0) {
    }

    @Override
    public void injectPickDisplayCoordinateFragment(PickDisplayCoordinateFragment arg0) {
    }

    @Override
    public void injectChooseActivityFragment(ChooseActivityFragment arg0) {
    }

    @Override
    public void injectChooseAppFragment(ChooseAppFragment arg0) {
    }

    @Override
    public void injectChooseAppShortcutFragment(ChooseAppShortcutFragment arg0) {
    }

    @Override
    public void injectChooseBluetoothDeviceFragment(ChooseBluetoothDeviceFragment arg0) {
    }

    @Override
    public void injectConfigIntentFragment(ConfigIntentFragment arg0) {
    }

    @CanIgnoreReturnValue
    private MainFragment injectMainFragment2(MainFragment instance) {
      MainFragment_MembersInjector.injectNavigationProvider(instance, singletonCImpl.navigationProviderImplProvider.get());
      MainFragment_MembersInjector.injectSetupAccessibilityServiceDelegate(instance, singletonCImpl.setupAccessibilityServiceDelegateImplProvider.get());
      return instance;
    }

    @CanIgnoreReturnValue
    private AboutFragment injectAboutFragment2(AboutFragment instance2) {
      AboutFragment_MembersInjector.injectBuildConfigProvider(instance2, singletonCImpl.provideBuildConfigProvider.get());
      AboutFragment_MembersInjector.injectPurchasingManager(instance2, singletonCImpl.providePurchasingManagerProvider.get());
      AboutFragment_MembersInjector.injectClipboardAdapter(instance2, singletonCImpl.androidClipboardAdapterProvider.get());
      return instance2;
    }
  }

  private static final class ViewCImpl extends KeyMapperApp_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends KeyMapperApp_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    ActivityCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return LazyClassKeyMap.<Boolean>of(MapBuilder.<String, Boolean>newMapBuilder(29).put(ActivityViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ActivityViewModel_HiltModules.KeyModule.provide()).put(ChooseActionViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ChooseActionViewModel_HiltModules.KeyModule.provide()).put(ChooseActivityViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ChooseActivityViewModel_HiltModules.KeyModule.provide()).put(ChooseAppShortcutViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ChooseAppShortcutViewModel_HiltModules.KeyModule.provide()).put(ChooseAppViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ChooseAppViewModel_HiltModules.KeyModule.provide()).put(ChooseBluetoothDeviceViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ChooseBluetoothDeviceViewModel_HiltModules.KeyModule.provide()).put(ChooseConstraintViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ChooseConstraintViewModel_HiltModules.KeyModule.provide()).put(ChooseKeyCodeViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ChooseKeyCodeViewModel_HiltModules.KeyModule.provide()).put(ChooseSettingViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ChooseSettingViewModel_HiltModules.KeyModule.provide()).put(ChooseSoundFileViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ChooseSoundFileViewModel_HiltModules.KeyModule.provide()).put(ConfigActionsViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ConfigActionsViewModel_HiltModules.KeyModule.provide()).put(ConfigConstraintsViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ConfigConstraintsViewModel_HiltModules.KeyModule.provide()).put(ConfigIntentViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ConfigIntentViewModel_HiltModules.KeyModule.provide()).put(ConfigKeyEventActionViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ConfigKeyEventActionViewModel_HiltModules.KeyModule.provide()).put(ConfigKeyMapViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ConfigKeyMapViewModel_HiltModules.KeyModule.provide()).put(ConfigShellCommandViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ConfigShellCommandViewModel_HiltModules.KeyModule.provide()).put(ConfigTriggerViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ConfigTriggerViewModel_HiltModules.KeyModule.provide()).put(CreateKeyMapShortcutViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, CreateKeyMapShortcutViewModel_HiltModules.KeyModule.provide()).put(ExpertModeSetupViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ExpertModeSetupViewModel_HiltModules.KeyModule.provide()).put(ExpertModeViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ExpertModeViewModel_HiltModules.KeyModule.provide()).put(GetEventViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, GetEventViewModel_HiltModules.KeyModule.provide()).put(HomeViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, HomeViewModel_HiltModules.KeyModule.provide()).put(InteractUiElementViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, InteractUiElementViewModel_HiltModules.KeyModule.provide()).put(LogViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, LogViewModel_HiltModules.KeyModule.provide()).put(PickDisplayCoordinateViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, PickDisplayCoordinateViewModel_HiltModules.KeyModule.provide()).put(PinchPickDisplayCoordinateViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, PinchPickDisplayCoordinateViewModel_HiltModules.KeyModule.provide()).put(RestoreKeyMapsViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, RestoreKeyMapsViewModel_HiltModules.KeyModule.provide()).put(SettingsViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, SettingsViewModel_HiltModules.KeyModule.provide()).put(SwipePickDisplayCoordinateViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, SwipePickDisplayCoordinateViewModel_HiltModules.KeyModule.provide()).build());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public void injectMainActivity(MainActivity arg0) {
      injectMainActivity2(arg0);
    }

    @Override
    public void injectLaunchKeyMapShortcutActivity(LaunchKeyMapShortcutActivity arg0) {
      injectLaunchKeyMapShortcutActivity2(arg0);
    }

    @Override
    public void injectRestoreKeyMapsActivity(RestoreKeyMapsActivity arg0) {
      injectRestoreKeyMapsActivity2(arg0);
    }

    @Override
    public void injectCreateKeyMapShortcutActivity(CreateKeyMapShortcutActivity arg0) {
      injectCreateKeyMapShortcutActivity2(arg0);
    }

    @CanIgnoreReturnValue
    private MainActivity injectMainActivity2(MainActivity instance) {
      BaseMainActivity_MembersInjector.injectPermissionAdapter(instance, singletonCImpl.androidPermissionAdapterProvider.get());
      BaseMainActivity_MembersInjector.injectServiceAdapter(instance, singletonCImpl.accessibilityServiceAdapterImplProvider.get());
      BaseMainActivity_MembersInjector.injectResourceProvider(instance, singletonCImpl.resourceProviderImplProvider.get());
      BaseMainActivity_MembersInjector.injectOnboardingUseCase(instance, singletonCImpl.onboardingUseCaseImplProvider.get());
      BaseMainActivity_MembersInjector.injectNotificationReceiverAdapter(instance, singletonCImpl.notificationReceiverAdapterImplProvider.get());
      BaseMainActivity_MembersInjector.injectShizukuAdapter(instance, singletonCImpl.shizukuAdapterImplProvider.get());
      BaseMainActivity_MembersInjector.injectBuildConfigProvider(instance, singletonCImpl.provideBuildConfigProvider.get());
      BaseMainActivity_MembersInjector.injectSystemBridgeSetupController(instance, singletonCImpl.systemBridgeSetupControllerImplProvider.get());
      BaseMainActivity_MembersInjector.injectSuAdapter(instance, singletonCImpl.suAdapterImplProvider.get());
      BaseMainActivity_MembersInjector.injectDevicesAdapter(instance, singletonCImpl.androidDevicesAdapterProvider.get());
      BaseMainActivity_MembersInjector.injectNetworkAdapter(instance, singletonCImpl.androidNetworkAdapterProvider.get());
      BaseMainActivity_MembersInjector.injectInputEventHub(instance, singletonCImpl.inputEventHubImplProvider.get());
      BaseMainActivity_MembersInjector.injectNavigationProvider(instance, singletonCImpl.navigationProviderImplProvider.get());
      BaseMainActivity_MembersInjector.injectConfigKeyMapState(instance, singletonCImpl.configKeyMapStateImplProvider.get());
      MainActivity_MembersInjector.injectDialogProvider(instance, singletonCImpl.dialogProviderImplProvider.get());
      return instance;
    }

    @CanIgnoreReturnValue
    private LaunchKeyMapShortcutActivity injectLaunchKeyMapShortcutActivity2(
        LaunchKeyMapShortcutActivity instance2) {
      LaunchKeyMapShortcutActivity_MembersInjector.injectAccessibilityServiceAdapter(instance2, singletonCImpl.accessibilityServiceAdapterImplProvider.get());
      return instance2;
    }

    @CanIgnoreReturnValue
    private RestoreKeyMapsActivity injectRestoreKeyMapsActivity2(RestoreKeyMapsActivity instance3) {
      RestoreKeyMapsActivity_MembersInjector.injectClassProvider(instance3, singletonCImpl.provideClassProvider.get());
      return instance3;
    }

    @CanIgnoreReturnValue
    private CreateKeyMapShortcutActivity injectCreateKeyMapShortcutActivity2(
        CreateKeyMapShortcutActivity instance4) {
      CreateKeyMapShortcutActivity_MembersInjector.injectPermissionAdapter(instance4, singletonCImpl.androidPermissionAdapterProvider.get());
      CreateKeyMapShortcutActivity_MembersInjector.injectServiceAdapter(instance4, singletonCImpl.accessibilityServiceAdapterImplProvider.get());
      CreateKeyMapShortcutActivity_MembersInjector.injectResourceProvider(instance4, singletonCImpl.resourceProviderImplProvider.get());
      CreateKeyMapShortcutActivity_MembersInjector.injectOnboardingUseCase(instance4, singletonCImpl.onboardingUseCaseImplProvider.get());
      CreateKeyMapShortcutActivity_MembersInjector.injectRecordTriggerController(instance4, singletonCImpl.recordTriggerControllerImplProvider.get());
      CreateKeyMapShortcutActivity_MembersInjector.injectNotificationReceiverAdapter(instance4, singletonCImpl.notificationReceiverAdapterImplProvider.get());
      CreateKeyMapShortcutActivity_MembersInjector.injectShizukuAdapter(instance4, singletonCImpl.shizukuAdapterImplProvider.get());
      CreateKeyMapShortcutActivity_MembersInjector.injectBuildConfigProvider(instance4, singletonCImpl.provideBuildConfigProvider.get());
      CreateKeyMapShortcutActivity_MembersInjector.injectNavigationProvider(instance4, singletonCImpl.navigationProviderImplProvider.get());
      return instance4;
    }
  }

  private static final class ViewModelCImpl extends KeyMapperApp_HiltComponents.ViewModelC {
    private final ViewModelModule viewModelModule;

    private final ViewModelLifecycle viewModelLifecycle;

    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    Provider<ActivityViewModel> activityViewModelProvider;

    Provider<CreateActionUseCaseImpl> createActionUseCaseImplProvider;

    Provider<CreateActionUseCase> bindCreateActionUseCaseProvider;

    Provider<ChooseActionViewModel> chooseActionViewModelProvider;

    Provider<DisplayAppsUseCaseImpl> displayAppsUseCaseImplProvider;

    Provider<DisplayAppsUseCase> bindDisplayAppsUseCaseProvider;

    Provider<ChooseActivityViewModel> chooseActivityViewModelProvider;

    Provider<DisplayAppShortcutsUseCaseImpl> displayAppShortcutsUseCaseImplProvider;

    Provider<DisplayAppShortcutsUseCase> bindDisplayAppShortcutsUseCaseProvider;

    Provider<ChooseAppShortcutViewModel> chooseAppShortcutViewModelProvider;

    Provider<ChooseAppViewModel> chooseAppViewModelProvider;

    Provider<ChooseBluetoothDeviceUseCaseImpl> chooseBluetoothDeviceUseCaseImplProvider;

    Provider<ChooseBluetoothDeviceUseCase> bindChooseBluetoothDeviceUseCaseProvider;

    Provider<ChooseBluetoothDeviceViewModel> chooseBluetoothDeviceViewModelProvider;

    Provider<CreateConstraintUseCaseImpl> createConstraintUseCaseImplProvider;

    Provider<CreateConstraintUseCase> bindCreateConstraintUseCaseProvider;

    Provider<ChooseConstraintViewModel> chooseConstraintViewModelProvider;

    Provider<ChooseKeyCodeViewModel> chooseKeyCodeViewModelProvider;

    Provider<ChooseSettingViewModel> chooseSettingViewModelProvider;

    Provider<ChooseSoundFileUseCaseImpl> chooseSoundFileUseCaseImplProvider;

    Provider<ChooseSoundFileUseCase> bindChooseSoundFileUseCaseProvider;

    Provider<ChooseSoundFileViewModel> chooseSoundFileViewModelProvider;

    Provider<DisplayKeyMapUseCaseImpl> displayKeyMapUseCaseImplProvider;

    Provider<TestActionUseCaseImpl> testActionUseCaseImplProvider;

    Provider<TestActionUseCase> bindTestActionUseCaseProvider;

    Provider<ConfigConstraintsUseCaseImpl> configConstraintsUseCaseImplProvider;

    Provider<ConfigActionsUseCaseImpl> configActionsUseCaseImplProvider;

    Provider<CoroutineScope> provideViewModelScopeProvider;

    Provider<SetupInputMethodUseCaseImpl> setupInputMethodUseCaseImplProvider;

    Provider<FixKeyEventActionDelegateImpl> fixKeyEventActionDelegateImplProvider;

    Provider<ConfigTriggerUseCaseImpl> configTriggerUseCaseImplProvider;

    Provider<OnboardingTipDelegateImpl> onboardingTipDelegateImplProvider;

    Provider<ConfigActionsViewModel> configActionsViewModelProvider;

    Provider<ConfigConstraintsViewModel> configConstraintsViewModelProvider;

    Provider<ConfigIntentViewModel> configIntentViewModelProvider;

    Provider<ConfigKeyEventUseCaseImpl> configKeyEventUseCaseImplProvider;

    Provider<ConfigKeyEventUseCase> bindConfigKeyEventUseCaseProvider;

    Provider<ConfigKeyEventActionViewModel> configKeyEventActionViewModelProvider;

    Provider<ConfigKeyMapViewModel> configKeyMapViewModelProvider;

    Provider<ConfigShellCommandViewModel> configShellCommandViewModelProvider;

    Provider<CreateKeyMapShortcutUseCaseImpl> createKeyMapShortcutUseCaseImplProvider;

    Provider<TriggerSetupDelegateImpl> triggerSetupDelegateImplProvider;

    Provider<ConfigTriggerViewModel> configTriggerViewModelProvider;

    Provider<ListKeyMapsUseCaseImpl> listKeyMapsUseCaseImplProvider;

    Provider<ListKeyMapsUseCase> bindListKeyMapsUseCaseProvider;

    Provider<CreateKeyMapShortcutViewModel> createKeyMapShortcutViewModelProvider;

    Provider<SystemBridgeSetupUseCaseImpl> systemBridgeSetupUseCaseImplProvider;

    Provider<ExpertModeSetupDelegateImpl> expertModeSetupDelegateImplProvider;

    Provider<ExpertModeSetupViewModel> expertModeSetupViewModelProvider;

    Provider<ExpertModeViewModel> expertModeViewModelProvider;

    Provider<GetEventViewModel> getEventViewModelProvider;

    Provider<BackupRestoreMappingsUseCaseImpl> backupRestoreMappingsUseCaseImplProvider;

    Provider<BackupRestoreMappingsUseCase> bindBackupRestoreMappingsUseCaseProvider;

    Provider<ShowHomeScreenAlertsUseCaseImpl> showHomeScreenAlertsUseCaseImplProvider;

    Provider<ShowHomeScreenAlertsUseCase> bindShowHomeScreenAlertsUseCaseProvider;

    Provider<SortKeyMapsUseCaseImpl> sortKeyMapsUseCaseImplProvider;

    Provider<SortKeyMapsUseCase> bindSortKeyMapsUseCaseProvider;

    Provider<HomeViewModel> homeViewModelProvider;

    Provider<InteractUiElementViewModel> interactUiElementViewModelProvider;

    Provider<DisplayLogUseCaseImpl> displayLogUseCaseImplProvider;

    Provider<DisplayLogUseCase> bindDisplayLogUseCaseProvider;

    Provider<LogViewModel> logViewModelProvider;

    Provider<PickDisplayCoordinateViewModel> pickDisplayCoordinateViewModelProvider;

    Provider<PinchPickDisplayCoordinateViewModel> pinchPickDisplayCoordinateViewModelProvider;

    Provider<RestoreKeyMapsViewModel> restoreKeyMapsViewModelProvider;

    Provider<ConfigSettingsUseCaseImpl> configSettingsUseCaseImplProvider;

    Provider<ConfigSettingsUseCase> bindConfigSettingsUseCaseProvider;

    Provider<ShareLogcatUseCaseImpl> shareLogcatUseCaseImplProvider;

    Provider<SettingsViewModel> settingsViewModelProvider;

    Provider<SwipePickDisplayCoordinateViewModel> swipePickDisplayCoordinateViewModelProvider;

    ViewModelCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ViewModelModule viewModelModuleParam, SavedStateHandle savedStateHandleParam,
        ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.viewModelModule = viewModelModuleParam;
      this.viewModelLifecycle = viewModelLifecycleParam;
      initialize(viewModelModuleParam, savedStateHandleParam, viewModelLifecycleParam);
      initialize2(viewModelModuleParam, savedStateHandleParam, viewModelLifecycleParam);
      initialize3(viewModelModuleParam, savedStateHandleParam, viewModelLifecycleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final ViewModelModule viewModelModuleParam,
        final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.activityViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.createActionUseCaseImplProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.bindCreateActionUseCaseProvider = DoubleCheck.provider((Provider) (createActionUseCaseImplProvider));
      this.chooseActionViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.displayAppsUseCaseImplProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
      this.bindDisplayAppsUseCaseProvider = DoubleCheck.provider((Provider) (displayAppsUseCaseImplProvider));
      this.chooseActivityViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.displayAppShortcutsUseCaseImplProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 6);
      this.bindDisplayAppShortcutsUseCaseProvider = DoubleCheck.provider((Provider) (displayAppShortcutsUseCaseImplProvider));
      this.chooseAppShortcutViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 5);
      this.chooseAppViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 7);
      this.chooseBluetoothDeviceUseCaseImplProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 9);
      this.bindChooseBluetoothDeviceUseCaseProvider = DoubleCheck.provider((Provider) (chooseBluetoothDeviceUseCaseImplProvider));
      this.chooseBluetoothDeviceViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 8);
      this.createConstraintUseCaseImplProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 11);
      this.bindCreateConstraintUseCaseProvider = DoubleCheck.provider((Provider) (createConstraintUseCaseImplProvider));
      this.chooseConstraintViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 10);
      this.chooseKeyCodeViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 12);
      this.chooseSettingViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 13);
      this.chooseSoundFileUseCaseImplProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 15);
      this.bindChooseSoundFileUseCaseProvider = DoubleCheck.provider((Provider) (chooseSoundFileUseCaseImplProvider));
      this.chooseSoundFileViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 14);
      this.displayKeyMapUseCaseImplProvider = DoubleCheck.provider(new SwitchingProvider<DisplayKeyMapUseCaseImpl>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 17));
      this.testActionUseCaseImplProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 18);
      this.bindTestActionUseCaseProvider = DoubleCheck.provider((Provider) (testActionUseCaseImplProvider));
    }

    @SuppressWarnings("unchecked")
    private void initialize2(final ViewModelModule viewModelModuleParam,
        final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.configConstraintsUseCaseImplProvider = DoubleCheck.provider(new SwitchingProvider<ConfigConstraintsUseCaseImpl>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 20));
      this.configActionsUseCaseImplProvider = DoubleCheck.provider(new SwitchingProvider<ConfigActionsUseCaseImpl>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 19));
      this.provideViewModelScopeProvider = DoubleCheck.provider(new SwitchingProvider<CoroutineScope>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 22));
      this.setupInputMethodUseCaseImplProvider = DoubleCheck.provider(new SwitchingProvider<SetupInputMethodUseCaseImpl>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 23));
      this.fixKeyEventActionDelegateImplProvider = DoubleCheck.provider(new SwitchingProvider<FixKeyEventActionDelegateImpl>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 21));
      this.configTriggerUseCaseImplProvider = DoubleCheck.provider(new SwitchingProvider<ConfigTriggerUseCaseImpl>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 25));
      this.onboardingTipDelegateImplProvider = DoubleCheck.provider(new SwitchingProvider<OnboardingTipDelegateImpl>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 24));
      this.configActionsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 16);
      this.configConstraintsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 26);
      this.configIntentViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 27);
      this.configKeyEventUseCaseImplProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 29);
      this.bindConfigKeyEventUseCaseProvider = DoubleCheck.provider((Provider) (configKeyEventUseCaseImplProvider));
      this.configKeyEventActionViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 28);
      this.configKeyMapViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 30);
      this.configShellCommandViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 31);
      this.createKeyMapShortcutUseCaseImplProvider = DoubleCheck.provider(new SwitchingProvider<CreateKeyMapShortcutUseCaseImpl>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 33));
      this.triggerSetupDelegateImplProvider = DoubleCheck.provider(new SwitchingProvider<TriggerSetupDelegateImpl>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 34));
      this.configTriggerViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 32);
      this.listKeyMapsUseCaseImplProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 36);
      this.bindListKeyMapsUseCaseProvider = DoubleCheck.provider((Provider) (listKeyMapsUseCaseImplProvider));
      this.createKeyMapShortcutViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 35);
      this.systemBridgeSetupUseCaseImplProvider = DoubleCheck.provider(new SwitchingProvider<SystemBridgeSetupUseCaseImpl>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 39));
      this.expertModeSetupDelegateImplProvider = DoubleCheck.provider(new SwitchingProvider<ExpertModeSetupDelegateImpl>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 38));
      this.expertModeSetupViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 37);
      this.expertModeViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 40);
    }

    @SuppressWarnings("unchecked")
    private void initialize3(final ViewModelModule viewModelModuleParam,
        final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.getEventViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 41);
      this.backupRestoreMappingsUseCaseImplProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 43);
      this.bindBackupRestoreMappingsUseCaseProvider = DoubleCheck.provider((Provider) (backupRestoreMappingsUseCaseImplProvider));
      this.showHomeScreenAlertsUseCaseImplProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 44);
      this.bindShowHomeScreenAlertsUseCaseProvider = DoubleCheck.provider((Provider) (showHomeScreenAlertsUseCaseImplProvider));
      this.sortKeyMapsUseCaseImplProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 45);
      this.bindSortKeyMapsUseCaseProvider = DoubleCheck.provider((Provider) (sortKeyMapsUseCaseImplProvider));
      this.homeViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 42);
      this.interactUiElementViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 46);
      this.displayLogUseCaseImplProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 48);
      this.bindDisplayLogUseCaseProvider = DoubleCheck.provider((Provider) (displayLogUseCaseImplProvider));
      this.logViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 47);
      this.pickDisplayCoordinateViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 49);
      this.pinchPickDisplayCoordinateViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 50);
      this.restoreKeyMapsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 51);
      this.configSettingsUseCaseImplProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 53);
      this.bindConfigSettingsUseCaseProvider = DoubleCheck.provider((Provider) (configSettingsUseCaseImplProvider));
      this.shareLogcatUseCaseImplProvider = DoubleCheck.provider(new SwitchingProvider<ShareLogcatUseCaseImpl>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 54));
      this.settingsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 52);
      this.swipePickDisplayCoordinateViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 55);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(MapBuilder.<String, javax.inject.Provider<ViewModel>>newMapBuilder(29).put(ActivityViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (activityViewModelProvider))).put(ChooseActionViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (chooseActionViewModelProvider))).put(ChooseActivityViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (chooseActivityViewModelProvider))).put(ChooseAppShortcutViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (chooseAppShortcutViewModelProvider))).put(ChooseAppViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (chooseAppViewModelProvider))).put(ChooseBluetoothDeviceViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (chooseBluetoothDeviceViewModelProvider))).put(ChooseConstraintViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (chooseConstraintViewModelProvider))).put(ChooseKeyCodeViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (chooseKeyCodeViewModelProvider))).put(ChooseSettingViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (chooseSettingViewModelProvider))).put(ChooseSoundFileViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (chooseSoundFileViewModelProvider))).put(ConfigActionsViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (configActionsViewModelProvider))).put(ConfigConstraintsViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (configConstraintsViewModelProvider))).put(ConfigIntentViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (configIntentViewModelProvider))).put(ConfigKeyEventActionViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (configKeyEventActionViewModelProvider))).put(ConfigKeyMapViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (configKeyMapViewModelProvider))).put(ConfigShellCommandViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (configShellCommandViewModelProvider))).put(ConfigTriggerViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (configTriggerViewModelProvider))).put(CreateKeyMapShortcutViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (createKeyMapShortcutViewModelProvider))).put(ExpertModeSetupViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (expertModeSetupViewModelProvider))).put(ExpertModeViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (expertModeViewModelProvider))).put(GetEventViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (getEventViewModelProvider))).put(HomeViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (homeViewModelProvider))).put(InteractUiElementViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (interactUiElementViewModelProvider))).put(LogViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (logViewModelProvider))).put(PickDisplayCoordinateViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (pickDisplayCoordinateViewModelProvider))).put(PinchPickDisplayCoordinateViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (pinchPickDisplayCoordinateViewModelProvider))).put(RestoreKeyMapsViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (restoreKeyMapsViewModelProvider))).put(SettingsViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (settingsViewModelProvider))).put(SwipePickDisplayCoordinateViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (swipePickDisplayCoordinateViewModelProvider))).build());
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return Collections.<Class<?>, Object>emptyMap();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // io.github.sds100.keymapper.base.ActivityViewModel
          return (T) new ActivityViewModel(singletonCImpl.setupAccessibilityServiceDelegateImplProvider.get(), singletonCImpl.resourceProviderImplProvider.get(), singletonCImpl.dialogProviderImplProvider.get(), singletonCImpl.navigationProviderImplProvider.get());

          case 1: // io.github.sds100.keymapper.base.actions.ChooseActionViewModel
          return (T) new ChooseActionViewModel(viewModelCImpl.bindCreateActionUseCaseProvider.get(), singletonCImpl.resourceProviderImplProvider.get(), singletonCImpl.navigationProviderImplProvider.get(), singletonCImpl.dialogProviderImplProvider.get());

          case 2: // io.github.sds100.keymapper.base.actions.CreateActionUseCaseImpl
          return (T) new CreateActionUseCaseImpl((InputMethodAdapter) (((Provider) (singletonCImpl.androidInputMethodAdapterProvider)).get()), singletonCImpl.androidSystemFeatureAdapterProvider.get(), singletonCImpl.androidCameraAdapterProvider.get(), singletonCImpl.androidPermissionAdapterProvider.get(), singletonCImpl.androidPhoneAdapterProvider.get(), singletonCImpl.androidSettingsAdapterProvider.get(), singletonCImpl.androidNotificationAdapterProvider.get());

          case 3: // io.github.sds100.keymapper.base.system.apps.ChooseActivityViewModel
          return (T) new ChooseActivityViewModel(viewModelCImpl.bindDisplayAppsUseCaseProvider.get());

          case 4: // io.github.sds100.keymapper.base.system.apps.DisplayAppsUseCaseImpl
          return (T) new DisplayAppsUseCaseImpl(singletonCImpl.androidPackageManagerAdapterProvider.get());

          case 5: // io.github.sds100.keymapper.base.system.apps.ChooseAppShortcutViewModel
          return (T) new ChooseAppShortcutViewModel(viewModelCImpl.bindDisplayAppShortcutsUseCaseProvider.get(), singletonCImpl.resourceProviderImplProvider.get(), singletonCImpl.dialogProviderImplProvider.get());

          case 6: // io.github.sds100.keymapper.base.system.apps.DisplayAppShortcutsUseCaseImpl
          return (T) new DisplayAppShortcutsUseCaseImpl(singletonCImpl.androidAppShortcutAdapterProvider.get());

          case 7: // io.github.sds100.keymapper.base.system.apps.ChooseAppViewModel
          return (T) new ChooseAppViewModel(viewModelCImpl.bindDisplayAppsUseCaseProvider.get());

          case 8: // io.github.sds100.keymapper.base.system.bluetooth.ChooseBluetoothDeviceViewModel
          return (T) new ChooseBluetoothDeviceViewModel(viewModelCImpl.bindChooseBluetoothDeviceUseCaseProvider.get(), singletonCImpl.resourceProviderImplProvider.get(), singletonCImpl.dialogProviderImplProvider.get());

          case 9: // io.github.sds100.keymapper.base.system.bluetooth.ChooseBluetoothDeviceUseCaseImpl
          return (T) new ChooseBluetoothDeviceUseCaseImpl(singletonCImpl.androidDevicesAdapterProvider.get(), singletonCImpl.androidPermissionAdapterProvider.get());

          case 10: // io.github.sds100.keymapper.base.constraints.ChooseConstraintViewModel
          return (T) new ChooseConstraintViewModel(viewModelCImpl.bindCreateConstraintUseCaseProvider.get(), singletonCImpl.dialogProviderImplProvider.get(), singletonCImpl.navigationProviderImplProvider.get(), singletonCImpl.resourceProviderImplProvider.get());

          case 11: // io.github.sds100.keymapper.base.constraints.CreateConstraintUseCaseImpl
          return (T) new CreateConstraintUseCaseImpl(singletonCImpl.androidNetworkAdapterProvider.get(), (InputMethodAdapter) (((Provider) (singletonCImpl.androidInputMethodAdapterProvider)).get()), singletonCImpl.preferenceRepositoryImplProvider.get(), singletonCImpl.androidCameraAdapterProvider.get(), singletonCImpl.androidDisplayAdapterProvider.get());

          case 12: // io.github.sds100.keymapper.base.actions.keyevent.ChooseKeyCodeViewModel
          return (T) new ChooseKeyCodeViewModel();

          case 13: // io.github.sds100.keymapper.base.actions.ChooseSettingViewModel
          return (T) new ChooseSettingViewModel(singletonCImpl.androidSettingsAdapterProvider.get(), singletonCImpl.systemBridgeConnectionManagerImplProvider.get(), singletonCImpl.resourceProviderImplProvider.get(), singletonCImpl.navigationProviderImplProvider.get(), singletonCImpl.dialogProviderImplProvider.get());

          case 14: // io.github.sds100.keymapper.base.actions.sound.ChooseSoundFileViewModel
          return (T) new ChooseSoundFileViewModel(singletonCImpl.resourceProviderImplProvider.get(), singletonCImpl.dialogProviderImplProvider.get(), viewModelCImpl.bindChooseSoundFileUseCaseProvider.get());

          case 15: // io.github.sds100.keymapper.base.actions.sound.ChooseSoundFileUseCaseImpl
          return (T) new ChooseSoundFileUseCaseImpl(singletonCImpl.androidFileAdapterProvider.get(), singletonCImpl.soundsManagerImplProvider.get());

          case 16: // io.github.sds100.keymapper.base.actions.ConfigActionsViewModel
          return (T) new ConfigActionsViewModel(viewModelCImpl.displayKeyMapUseCaseImplProvider.get(), viewModelCImpl.bindCreateActionUseCaseProvider.get(), viewModelCImpl.bindTestActionUseCaseProvider.get(), viewModelCImpl.configActionsUseCaseImplProvider.get(), singletonCImpl.onboardingUseCaseImplProvider.get(), singletonCImpl.setupAccessibilityServiceDelegateImplProvider.get(), viewModelCImpl.fixKeyEventActionDelegateImplProvider.get(), viewModelCImpl.onboardingTipDelegateImplProvider.get(), singletonCImpl.resourceProviderImplProvider.get(), singletonCImpl.navigationProviderImplProvider.get(), singletonCImpl.dialogProviderImplProvider.get());

          case 17: // io.github.sds100.keymapper.base.keymaps.DisplayKeyMapUseCaseImpl
          return (T) new DisplayKeyMapUseCaseImpl(singletonCImpl.androidPermissionAdapterProvider.get(), singletonCImpl.switchImeAsyncImplProvider.get(), (InputMethodAdapter) (((Provider) (singletonCImpl.androidInputMethodAdapterProvider)).get()), singletonCImpl.androidPackageManagerAdapterProvider.get(), singletonCImpl.preferenceRepositoryImplProvider.get(), singletonCImpl.accessibilityServiceAdapterImplProvider.get(), singletonCImpl.providePurchasingManagerProvider.get(), singletonCImpl.androidRingtoneAdapterProvider.get(), singletonCImpl.getActionErrorUseCaseImplProvider.get(), singletonCImpl.getConstraintErrorUseCaseImplProvider.get(), singletonCImpl.provideBuildConfigProvider.get(), singletonCImpl.navigationProviderImplProvider.get(), singletonCImpl.systemBridgeConnectionManagerImplProvider.get(), singletonCImpl.evdevDevicesDelegateProvider.get());

          case 18: // io.github.sds100.keymapper.base.actions.TestActionUseCaseImpl
          return (T) new TestActionUseCaseImpl(singletonCImpl.accessibilityServiceAdapterImplProvider.get());

          case 19: // io.github.sds100.keymapper.base.actions.ConfigActionsUseCaseImpl
          return (T) new ConfigActionsUseCaseImpl(singletonCImpl.configKeyMapStateImplProvider.get(), singletonCImpl.preferenceRepositoryImplProvider.get(), viewModelCImpl.configConstraintsUseCaseImplProvider.get(), singletonCImpl.getDefaultKeyMapOptionsUseCaseImplProvider.get());

          case 20: // io.github.sds100.keymapper.base.constraints.ConfigConstraintsUseCaseImpl
          return (T) new ConfigConstraintsUseCaseImpl(singletonCImpl.configKeyMapStateImplProvider.get(), singletonCImpl.preferenceRepositoryImplProvider.get());

          case 21: // io.github.sds100.keymapper.base.actions.keyevent.FixKeyEventActionDelegateImpl
          return (T) new FixKeyEventActionDelegateImpl(viewModelCImpl.provideViewModelScopeProvider.get(), singletonCImpl.controlAccessibilityServiceUseCaseImplProvider.get(), singletonCImpl.systemBridgeConnectionManagerImplProvider.get(), viewModelCImpl.setupInputMethodUseCaseImplProvider.get(), singletonCImpl.preferenceRepositoryImplProvider.get(), singletonCImpl.setupAccessibilityServiceDelegateImplProvider.get(), singletonCImpl.resourceProviderImplProvider.get(), singletonCImpl.dialogProviderImplProvider.get(), singletonCImpl.navigationProviderImplProvider.get());

          case 22: // @javax.inject.Named("viewmodel") kotlinx.coroutines.CoroutineScope
          return (T) ViewModelModule_ProvideViewModelScopeFactory.provideViewModelScope(viewModelCImpl.viewModelModule, viewModelCImpl.viewModelLifecycle);

          case 23: // io.github.sds100.keymapper.base.trigger.SetupInputMethodUseCaseImpl
          return (T) new SetupInputMethodUseCaseImpl(singletonCImpl.switchImeAsyncImplProvider.get(), (InputMethodAdapter) (((Provider) (singletonCImpl.androidInputMethodAdapterProvider)).get()), singletonCImpl.provideBuildConfigProvider.get());

          case 24: // io.github.sds100.keymapper.base.onboarding.OnboardingTipDelegateImpl
          return (T) new OnboardingTipDelegateImpl(viewModelCImpl.provideViewModelScopeProvider.get(), singletonCImpl.preferenceRepositoryImplProvider.get(), viewModelCImpl.configTriggerUseCaseImplProvider.get(), viewModelCImpl.configActionsUseCaseImplProvider.get(), singletonCImpl.systemBridgeConnectionManagerImplProvider.get(), singletonCImpl.resourceProviderImplProvider.get(), singletonCImpl.navigationProviderImplProvider.get());

          case 25: // io.github.sds100.keymapper.base.trigger.ConfigTriggerUseCaseImpl
          return (T) new ConfigTriggerUseCaseImpl(singletonCImpl.configKeyMapStateImplProvider.get(), singletonCImpl.preferenceRepositoryImplProvider.get(), singletonCImpl.roomFloatingButtonRepositoryProvider.get(), singletonCImpl.androidDevicesAdapterProvider.get(), singletonCImpl.roomFloatingLayoutRepositoryProvider.get(), singletonCImpl.getDefaultKeyMapOptionsUseCaseImplProvider.get(), singletonCImpl.roomKeyMapRepositoryProvider.get());

          case 26: // io.github.sds100.keymapper.base.constraints.ConfigConstraintsViewModel
          return (T) new ConfigConstraintsViewModel(viewModelCImpl.configConstraintsUseCaseImplProvider.get(), viewModelCImpl.displayKeyMapUseCaseImplProvider.get(), singletonCImpl.resourceProviderImplProvider.get(), singletonCImpl.navigationProviderImplProvider.get(), singletonCImpl.dialogProviderImplProvider.get());

          case 27: // io.github.sds100.keymapper.base.system.intents.ConfigIntentViewModel
          return (T) new ConfigIntentViewModel(singletonCImpl.resourceProviderImplProvider.get(), singletonCImpl.dialogProviderImplProvider.get());

          case 28: // io.github.sds100.keymapper.base.actions.keyevent.ConfigKeyEventActionViewModel
          return (T) new ConfigKeyEventActionViewModel(viewModelCImpl.bindConfigKeyEventUseCaseProvider.get(), singletonCImpl.resourceProviderImplProvider.get());

          case 29: // io.github.sds100.keymapper.base.actions.keyevent.ConfigKeyEventUseCaseImpl
          return (T) new ConfigKeyEventUseCaseImpl(singletonCImpl.preferenceRepositoryImplProvider.get(), singletonCImpl.androidDevicesAdapterProvider.get());

          case 30: // io.github.sds100.keymapper.base.keymaps.ConfigKeyMapViewModel
          return (T) new ConfigKeyMapViewModel(singletonCImpl.configKeyMapStateImplProvider.get(), viewModelCImpl.configTriggerUseCaseImplProvider.get(), singletonCImpl.onboardingUseCaseImplProvider.get(), singletonCImpl.navigationProviderImplProvider.get(), singletonCImpl.dialogProviderImplProvider.get());

          case 31: // io.github.sds100.keymapper.base.actions.ConfigShellCommandViewModel
          return (T) new ConfigShellCommandViewModel(singletonCImpl.executeShellCommandUseCase(), singletonCImpl.navigationProviderImplProvider.get(), singletonCImpl.systemBridgeConnectionManagerImplProvider.get(), singletonCImpl.preferenceRepositoryImplProvider.get(), singletonCImpl.resourceProviderImplProvider.get());

          case 32: // io.github.sds100.keymapper.trigger.ConfigTriggerViewModel
          return (T) new ConfigTriggerViewModel(singletonCImpl.onboardingUseCaseImplProvider.get(), viewModelCImpl.configTriggerUseCaseImplProvider.get(), singletonCImpl.recordTriggerControllerImplProvider.get(), viewModelCImpl.createKeyMapShortcutUseCaseImplProvider.get(), viewModelCImpl.displayKeyMapUseCaseImplProvider.get(), singletonCImpl.fingerprintGesturesSupportedUseCaseImplProvider.get(), singletonCImpl.systemBridgeConnectionManagerImplProvider.get(), singletonCImpl.setupAccessibilityServiceDelegateImplProvider.get(), viewModelCImpl.onboardingTipDelegateImplProvider.get(), viewModelCImpl.triggerSetupDelegateImplProvider.get(), singletonCImpl.resourceProviderImplProvider.get(), singletonCImpl.navigationProviderImplProvider.get(), singletonCImpl.dialogProviderImplProvider.get());

          case 33: // io.github.sds100.keymapper.base.shortcuts.CreateKeyMapShortcutUseCaseImpl
          return (T) new CreateKeyMapShortcutUseCaseImpl(singletonCImpl.androidAppShortcutAdapterProvider.get());

          case 34: // io.github.sds100.keymapper.base.trigger.TriggerSetupDelegateImpl
          return (T) new TriggerSetupDelegateImpl(viewModelCImpl.provideViewModelScopeProvider.get(), singletonCImpl.setupAccessibilityServiceDelegateImplProvider.get(), singletonCImpl.recordTriggerControllerImplProvider.get(), singletonCImpl.systemBridgeConnectionManagerImplProvider.get(), viewModelCImpl.configTriggerUseCaseImplProvider.get(), viewModelCImpl.setupInputMethodUseCaseImplProvider.get(), singletonCImpl.resourceProviderImplProvider.get(), singletonCImpl.dialogProviderImplProvider.get(), singletonCImpl.navigationProviderImplProvider.get());

          case 35: // io.github.sds100.keymapper.base.shortcuts.CreateKeyMapShortcutViewModel
          return (T) new CreateKeyMapShortcutViewModel(singletonCImpl.configKeyMapStateImplProvider.get(), viewModelCImpl.configTriggerUseCaseImplProvider.get(), viewModelCImpl.bindListKeyMapsUseCaseProvider.get(), viewModelCImpl.createKeyMapShortcutUseCaseImplProvider.get(), singletonCImpl.resourceProviderImplProvider.get());

          case 36: // io.github.sds100.keymapper.base.home.ListKeyMapsUseCaseImpl
          return (T) new ListKeyMapsUseCaseImpl(singletonCImpl.roomKeyMapRepositoryProvider.get(), singletonCImpl.roomGroupRepositoryProvider.get(), singletonCImpl.roomFloatingButtonRepositoryProvider.get(), singletonCImpl.androidFileAdapterProvider.get(), singletonCImpl.backupManagerImplProvider.get(), singletonCImpl.resourceProviderImplProvider.get(), viewModelCImpl.displayKeyMapUseCaseImplProvider.get());

          case 37: // io.github.sds100.keymapper.base.expertmode.ExpertModeSetupViewModel
          return (T) new ExpertModeSetupViewModel(viewModelCImpl.expertModeSetupDelegateImplProvider.get(), singletonCImpl.navigationProviderImplProvider.get(), singletonCImpl.resourceProviderImplProvider.get());

          case 38: // io.github.sds100.keymapper.base.expertmode.ExpertModeSetupDelegateImpl
          return (T) new ExpertModeSetupDelegateImpl(viewModelCImpl.provideViewModelScopeProvider.get(), viewModelCImpl.systemBridgeSetupUseCaseImplProvider.get(), singletonCImpl.resourceProviderImplProvider.get(), singletonCImpl.navigationProviderImplProvider.get());

          case 39: // io.github.sds100.keymapper.base.expertmode.SystemBridgeSetupUseCaseImpl
          return (T) new SystemBridgeSetupUseCaseImpl(singletonCImpl.preferenceRepositoryImplProvider.get(), singletonCImpl.suAdapterImplProvider.get(), singletonCImpl.systemBridgeSetupControllerImplProvider.get(), singletonCImpl.systemBridgeConnectionManagerImplProvider.get(), singletonCImpl.shizukuAdapterImplProvider.get(), singletonCImpl.androidPermissionAdapterProvider.get(), singletonCImpl.accessibilityServiceAdapterImplProvider.get(), singletonCImpl.androidNetworkAdapterProvider.get(), singletonCImpl.clockImplProvider.get());

          case 40: // io.github.sds100.keymapper.base.expertmode.ExpertModeViewModel
          return (T) new ExpertModeViewModel(viewModelCImpl.systemBridgeSetupUseCaseImplProvider.get(), singletonCImpl.resourceProviderImplProvider.get(), singletonCImpl.dialogProviderImplProvider.get(), singletonCImpl.navigationProviderImplProvider.get());

          case 41: // io.github.sds100.keymapper.base.debug.GetEventViewModel
          return (T) new GetEventViewModel(singletonCImpl.getEventRecorderImplProvider.get(), singletonCImpl.navigationProviderImplProvider.get(), singletonCImpl.systemBridgeConnectionManagerImplProvider.get(), singletonCImpl.pauseKeyMapsUseCaseImplProvider.get());

          case 42: // io.github.sds100.keymapper.home.HomeViewModel
          return (T) new HomeViewModel(viewModelCImpl.bindListKeyMapsUseCaseProvider.get(), singletonCImpl.pauseKeyMapsUseCaseImplProvider.get(), viewModelCImpl.bindBackupRestoreMappingsUseCaseProvider.get(), viewModelCImpl.bindShowHomeScreenAlertsUseCaseProvider.get(), singletonCImpl.onboardingUseCaseImplProvider.get(), singletonCImpl.resourceProviderImplProvider.get(), viewModelCImpl.bindSortKeyMapsUseCaseProvider.get(), singletonCImpl.bindShowInputMethodPickerUseCaseProvider.get(), singletonCImpl.setupAccessibilityServiceDelegateImplProvider.get(), viewModelCImpl.fixKeyEventActionDelegateImplProvider.get(), singletonCImpl.navigationProviderImplProvider.get(), singletonCImpl.dialogProviderImplProvider.get());

          case 43: // io.github.sds100.keymapper.base.backup.BackupRestoreMappingsUseCaseImpl
          return (T) new BackupRestoreMappingsUseCaseImpl(singletonCImpl.androidFileAdapterProvider.get(), singletonCImpl.backupManagerImplProvider.get());

          case 44: // io.github.sds100.keymapper.base.home.ShowHomeScreenAlertsUseCaseImpl
          return (T) new ShowHomeScreenAlertsUseCaseImpl(singletonCImpl.preferenceRepositoryImplProvider.get(), singletonCImpl.androidPermissionAdapterProvider.get(), singletonCImpl.pauseKeyMapsUseCaseImplProvider.get(), singletonCImpl.systemBridgeConnectionManagerImplProvider.get(), singletonCImpl.systemBridgeSetupControllerImplProvider.get());

          case 45: // io.github.sds100.keymapper.base.sorting.SortKeyMapsUseCaseImpl
          return (T) new SortKeyMapsUseCaseImpl(singletonCImpl.preferenceRepositoryImplProvider.get(), viewModelCImpl.displayKeyMapUseCaseImplProvider.get());

          case 46: // io.github.sds100.keymapper.base.actions.uielement.InteractUiElementViewModel
          return (T) new InteractUiElementViewModel(singletonCImpl.interactUiElementControllerProvider.get(), singletonCImpl.setupAccessibilityServiceDelegateImplProvider.get(), singletonCImpl.resourceProviderImplProvider.get(), singletonCImpl.dialogProviderImplProvider.get(), singletonCImpl.navigationProviderImplProvider.get());

          case 47: // io.github.sds100.keymapper.base.logging.LogViewModel
          return (T) new LogViewModel(viewModelCImpl.bindDisplayLogUseCaseProvider.get());

          case 48: // io.github.sds100.keymapper.base.logging.DisplayLogUseCaseImpl
          return (T) new DisplayLogUseCaseImpl(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideCoroutineScopeProvider.get(), (LogRepository) (((Provider) (singletonCImpl.roomLogRepositoryProvider)).get()), singletonCImpl.resourceProviderImplProvider.get(), singletonCImpl.androidClipboardAdapterProvider.get(), singletonCImpl.androidFileAdapterProvider.get(), singletonCImpl.provideBuildConfigProvider.get());

          case 49: // io.github.sds100.keymapper.base.actions.tapscreen.PickDisplayCoordinateViewModel
          return (T) new PickDisplayCoordinateViewModel(singletonCImpl.resourceProviderImplProvider.get(), singletonCImpl.dialogProviderImplProvider.get());

          case 50: // io.github.sds100.keymapper.base.actions.pinchscreen.PinchPickDisplayCoordinateViewModel
          return (T) new PinchPickDisplayCoordinateViewModel(singletonCImpl.resourceProviderImplProvider.get(), singletonCImpl.dialogProviderImplProvider.get());

          case 51: // io.github.sds100.keymapper.base.backup.RestoreKeyMapsViewModel
          return (T) new RestoreKeyMapsViewModel(viewModelCImpl.bindBackupRestoreMappingsUseCaseProvider.get(), singletonCImpl.resourceProviderImplProvider.get());

          case 52: // io.github.sds100.keymapper.base.settings.SettingsViewModel
          return (T) new SettingsViewModel(viewModelCImpl.bindConfigSettingsUseCaseProvider.get(), singletonCImpl.resourceProviderImplProvider.get(), viewModelCImpl.shareLogcatUseCaseImplProvider.get(), singletonCImpl.dialogProviderImplProvider.get(), singletonCImpl.navigationProviderImplProvider.get());

          case 53: // io.github.sds100.keymapper.base.settings.ConfigSettingsUseCaseImpl
          return (T) new ConfigSettingsUseCaseImpl(singletonCImpl.preferenceRepositoryImplProvider.get(), singletonCImpl.androidPermissionAdapterProvider.get(), (InputMethodAdapter) (((Provider) (singletonCImpl.androidInputMethodAdapterProvider)).get()), singletonCImpl.switchImeAsyncImplProvider.get(), singletonCImpl.soundsManagerImplProvider.get(), singletonCImpl.suAdapterImplProvider.get(), singletonCImpl.androidPackageManagerAdapterProvider.get(), singletonCImpl.shizukuAdapterImplProvider.get(), singletonCImpl.androidDevicesAdapterProvider.get(), singletonCImpl.provideBuildConfigProvider.get(), singletonCImpl.androidNotificationAdapterProvider.get());

          case 54: // io.github.sds100.keymapper.base.logging.ShareLogcatUseCaseImpl
          return (T) new ShareLogcatUseCaseImpl(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.androidFileAdapterProvider.get(), singletonCImpl.standardShellAdapterProvider.get(), singletonCImpl.androidPermissionAdapterProvider.get(), singletonCImpl.provideBuildConfigProvider.get());

          case 55: // io.github.sds100.keymapper.base.actions.swipescreen.SwipePickDisplayCoordinateViewModel
          return (T) new SwipePickDisplayCoordinateViewModel(singletonCImpl.resourceProviderImplProvider.get(), singletonCImpl.dialogProviderImplProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends KeyMapperApp_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends KeyMapperApp_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    Provider<AccessibilityNodeRecorder.Factory> factoryProvider;

    Provider<PerformActionsUseCaseImpl.Factory> factoryProvider2;

    Provider<DetectKeyMapsUseCaseImpl.Factory> factoryProvider3;

    Provider<DetectConstraintsUseCaseImpl.Factory> factoryProvider4;

    Provider<SystemBridgeSetupAssistantController.Factory> factoryProvider5;

    Provider<AutoSwitchImeController.Factory> factoryProvider6;

    Provider<AccessibilityServiceController.Factory> factoryProvider7;

    ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(serviceParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final Service serviceParam) {
      this.factoryProvider = SingleCheck.provider(new SwitchingProvider<AccessibilityNodeRecorder.Factory>(singletonCImpl, serviceCImpl, 1));
      this.factoryProvider2 = SingleCheck.provider(new SwitchingProvider<PerformActionsUseCaseImpl.Factory>(singletonCImpl, serviceCImpl, 2));
      this.factoryProvider3 = SingleCheck.provider(new SwitchingProvider<DetectKeyMapsUseCaseImpl.Factory>(singletonCImpl, serviceCImpl, 3));
      this.factoryProvider4 = SingleCheck.provider(new SwitchingProvider<DetectConstraintsUseCaseImpl.Factory>(singletonCImpl, serviceCImpl, 4));
      this.factoryProvider5 = SingleCheck.provider(new SwitchingProvider<SystemBridgeSetupAssistantController.Factory>(singletonCImpl, serviceCImpl, 5));
      this.factoryProvider6 = SingleCheck.provider(new SwitchingProvider<AutoSwitchImeController.Factory>(singletonCImpl, serviceCImpl, 6));
      this.factoryProvider7 = SingleCheck.provider(new SwitchingProvider<AccessibilityServiceController.Factory>(singletonCImpl, serviceCImpl, 0));
    }

    @Override
    public void injectBaseAccessibilityService(BaseAccessibilityService arg0) {
      injectBaseAccessibilityService2(arg0);
    }

    @Override
    public void injectMyAccessibilityService(MyAccessibilityService arg0) {
      injectMyAccessibilityService2(arg0);
    }

    @Override
    public void injectObserveEnabledAccessibilityServicesJob(
        ObserveEnabledAccessibilityServicesJob arg0) {
      injectObserveEnabledAccessibilityServicesJob2(arg0);
    }

    @Override
    public void injectKeyMapperImeService(KeyMapperImeService arg0) {
      injectKeyMapperImeService2(arg0);
    }

    @Override
    public void injectObserveInputMethodsJob(ObserveInputMethodsJob arg0) {
      injectObserveInputMethodsJob2(arg0);
    }

    @Override
    public void injectNotificationReceiver(NotificationReceiver arg0) {
      injectNotificationReceiver2(arg0);
    }

    @Override
    public void injectObserveNotificationListenersJob(ObserveNotificationListenersJob arg0) {
      injectObserveNotificationListenersJob2(arg0);
    }

    @Override
    public void injectToggleKeyMapperKeyboardTile(ToggleKeyMapperKeyboardTile arg0) {
      injectToggleKeyMapperKeyboardTile2(arg0);
    }

    @Override
    public void injectToggleMappingsTile(ToggleMappingsTile arg0) {
      injectToggleMappingsTile2(arg0);
    }

    @CanIgnoreReturnValue
    private BaseAccessibilityService injectBaseAccessibilityService2(
        BaseAccessibilityService instance) {
      BaseAccessibilityService_MembersInjector.injectAccessibilityServiceAdapterLazy(instance, DoubleCheck.lazy(singletonCImpl.accessibilityServiceAdapterImplProvider));
      BaseAccessibilityService_MembersInjector.injectInputMethodAdapterLazy(instance, DoubleCheck.lazy(((Provider) (singletonCImpl.androidInputMethodAdapterProvider))));
      return instance;
    }

    @CanIgnoreReturnValue
    private MyAccessibilityService injectMyAccessibilityService2(MyAccessibilityService instance2) {
      BaseAccessibilityService_MembersInjector.injectAccessibilityServiceAdapterLazy(instance2, DoubleCheck.lazy(singletonCImpl.accessibilityServiceAdapterImplProvider));
      BaseAccessibilityService_MembersInjector.injectInputMethodAdapterLazy(instance2, DoubleCheck.lazy(((Provider) (singletonCImpl.androidInputMethodAdapterProvider))));
      MyAccessibilityService_MembersInjector.injectControllerFactory(instance2, factoryProvider7.get());
      return instance2;
    }

    @CanIgnoreReturnValue
    private ObserveEnabledAccessibilityServicesJob injectObserveEnabledAccessibilityServicesJob2(
        ObserveEnabledAccessibilityServicesJob instance3) {
      ObserveEnabledAccessibilityServicesJob_MembersInjector.injectAccessibilityServiceAdapter(instance3, singletonCImpl.accessibilityServiceAdapterImplProvider.get());
      return instance3;
    }

    @CanIgnoreReturnValue
    private KeyMapperImeService injectKeyMapperImeService2(KeyMapperImeService instance4) {
      KeyMapperImeService_MembersInjector.injectBuildConfigProvider(instance4, singletonCImpl.provideBuildConfigProvider.get());
      KeyMapperImeService_MembersInjector.injectServiceAdapter(instance4, singletonCImpl.accessibilityServiceAdapterImplProvider.get());
      KeyMapperImeService_MembersInjector.injectKeyEventRelayServiceWrapper(instance4, singletonCImpl.keyEventRelayServiceWrapperImplProvider.get());
      return instance4;
    }

    @CanIgnoreReturnValue
    private ObserveInputMethodsJob injectObserveInputMethodsJob2(ObserveInputMethodsJob instance5) {
      ObserveInputMethodsJob_MembersInjector.injectInputMethodAdapter(instance5, singletonCImpl.androidInputMethodAdapterProvider.get());
      return instance5;
    }

    @CanIgnoreReturnValue
    private NotificationReceiver injectNotificationReceiver2(NotificationReceiver instance6) {
      NotificationReceiver_MembersInjector.injectMediaAdapter(instance6, singletonCImpl.androidMediaAdapterProvider.get());
      NotificationReceiver_MembersInjector.injectServiceAdapter(instance6, singletonCImpl.notificationReceiverAdapterImplProvider.get());
      return instance6;
    }

    @CanIgnoreReturnValue
    private ObserveNotificationListenersJob injectObserveNotificationListenersJob2(
        ObserveNotificationListenersJob instance7) {
      ObserveNotificationListenersJob_MembersInjector.injectPermissionAdapter(instance7, singletonCImpl.androidPermissionAdapterProvider.get());
      return instance7;
    }

    @CanIgnoreReturnValue
    private ToggleKeyMapperKeyboardTile injectToggleKeyMapperKeyboardTile2(
        ToggleKeyMapperKeyboardTile instance8) {
      ToggleKeyMapperKeyboardTile_MembersInjector.injectUseCase(instance8, singletonCImpl.toggleCompatibleImeUseCaseImplProvider.get());
      ToggleKeyMapperKeyboardTile_MembersInjector.injectResourceProvider(instance8, singletonCImpl.resourceProviderImplProvider.get());
      return instance8;
    }

    @CanIgnoreReturnValue
    private ToggleMappingsTile injectToggleMappingsTile2(ToggleMappingsTile instance9) {
      ToggleMappingsTile_MembersInjector.injectServiceAdapter(instance9, singletonCImpl.accessibilityServiceAdapterImplProvider.get());
      ToggleMappingsTile_MembersInjector.injectUseCase(instance9, singletonCImpl.pauseKeyMapsUseCaseImplProvider.get());
      return instance9;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ServiceCImpl serviceCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ServiceCImpl serviceCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.serviceCImpl = serviceCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // io.github.sds100.keymapper.system.accessibility.AccessibilityServiceController.Factory
          return (T) new AccessibilityServiceController.Factory() {
            @Override
            public AccessibilityServiceController create(MyAccessibilityService service) {
              return new AccessibilityServiceController(service, serviceCImpl.factoryProvider.get(), serviceCImpl.factoryProvider2.get(), serviceCImpl.factoryProvider3.get(), serviceCImpl.factoryProvider4.get(), singletonCImpl.fingerprintGesturesSupportedUseCaseImplProvider.get(), singletonCImpl.pauseKeyMapsUseCaseImplProvider.get(), singletonCImpl.preferenceRepositoryImplProvider.get(), singletonCImpl.keyEventRelayServiceWrapperImplProvider.get(), singletonCImpl.inputEventHubImplProvider.get(), singletonCImpl.recordTriggerControllerImplProvider.get(), serviceCImpl.factoryProvider5.get(), serviceCImpl.factoryProvider6.get());
            }
          };

          case 1: // io.github.sds100.keymapper.base.system.accessibility.AccessibilityNodeRecorder.Factory
          return (T) new AccessibilityNodeRecorder.Factory() {
            @Override
            public AccessibilityNodeRecorder create(BaseAccessibilityService service2) {
              return new AccessibilityNodeRecorder(service2, singletonCImpl.roomAccessibilityNodeRepositoryProvider.get());
            }
          };

          case 2: // io.github.sds100.keymapper.base.actions.PerformActionsUseCaseImpl.Factory
          return (T) new PerformActionsUseCaseImpl.Factory() {
            @Override
            public PerformActionsUseCaseImpl create(CoroutineScope coroutineScope,
                IAccessibilityService service3) {
              return new PerformActionsUseCaseImpl(coroutineScope, service3, (InputMethodAdapter) (((Provider) (singletonCImpl.androidInputMethodAdapterProvider)).get()), singletonCImpl.switchImeAsyncImplProvider.get(), singletonCImpl.androidFileAdapterProvider.get(), singletonCImpl.suAdapterImplProvider.get(), singletonCImpl.standardShellAdapterProvider.get(), singletonCImpl.intentAdapterImplProvider.get(), singletonCImpl.getActionErrorUseCaseImplProvider.get(), singletonCImpl.executeShellCommandUseCase(), singletonCImpl.imeInputEventInjectorImplProvider.get(), singletonCImpl.androidPackageManagerAdapterProvider.get(), singletonCImpl.androidAppShortcutAdapterProvider.get(), singletonCImpl.androidToastAdapterProvider.get(), singletonCImpl.androidDevicesAdapterProvider.get(), singletonCImpl.androidPhoneAdapterProvider.get(), singletonCImpl.androidVolumeAdapterProvider.get(), singletonCImpl.androidCameraAdapterProvider.get(), singletonCImpl.androidDisplayAdapterProvider.get(), singletonCImpl.provideLockscreenAdapterProvider.get(), singletonCImpl.androidMediaAdapterProvider.get(), singletonCImpl.androidAirplaneModeAdapterProvider.get(), singletonCImpl.androidNetworkAdapterProvider.get(), singletonCImpl.androidBluetoothAdapterProvider.get(), singletonCImpl.androidNfcAdapterProvider.get(), singletonCImpl.androidOpenUrlAdapterProvider.get(), singletonCImpl.resourceProviderImplProvider.get(), singletonCImpl.soundsManagerImplProvider.get(), singletonCImpl.notificationReceiverAdapterImplProvider.get(), singletonCImpl.androidNotificationAdapterProvider.get(), singletonCImpl.androidRingtoneAdapterProvider.get(), singletonCImpl.preferenceRepositoryImplProvider.get(), singletonCImpl.inputEventHubImplProvider.get(), singletonCImpl.systemBridgeConnectionManagerImplProvider.get(), singletonCImpl.androidSettingsAdapterProvider.get());
            }
          };

          case 3: // io.github.sds100.keymapper.base.detection.DetectKeyMapsUseCaseImpl.Factory
          return (T) new DetectKeyMapsUseCaseImpl.Factory() {
            @Override
            public DetectKeyMapsUseCaseImpl create(IAccessibilityService accessibilityService,
                CoroutineScope coroutineScope2) {
              return new DetectKeyMapsUseCaseImpl(accessibilityService, coroutineScope2, singletonCImpl.roomKeyMapRepositoryProvider.get(), singletonCImpl.roomFloatingButtonRepositoryProvider.get(), singletonCImpl.roomGroupRepositoryProvider.get(), singletonCImpl.preferenceRepositoryImplProvider.get(), singletonCImpl.androidVolumeAdapterProvider.get(), singletonCImpl.androidToastAdapterProvider.get(), singletonCImpl.resourceProviderImplProvider.get(), singletonCImpl.androidVibratorAdapterProvider.get(), singletonCImpl.inputEventHubImplProvider.get());
            }
          };

          case 4: // io.github.sds100.keymapper.base.constraints.DetectConstraintsUseCaseImpl.Factory
          return (T) new DetectConstraintsUseCaseImpl.Factory() {
            @Override
            public DetectConstraintsUseCaseImpl create(
                IAccessibilityService accessibilityService2) {
              return new DetectConstraintsUseCaseImpl(accessibilityService2, singletonCImpl.androidMediaAdapterProvider.get(), singletonCImpl.androidDevicesAdapterProvider.get(), singletonCImpl.androidDisplayAdapterProvider.get(), singletonCImpl.androidCameraAdapterProvider.get(), singletonCImpl.androidNetworkAdapterProvider.get(), (InputMethodAdapter) (((Provider) (singletonCImpl.androidInputMethodAdapterProvider)).get()), singletonCImpl.provideLockscreenAdapterProvider.get(), singletonCImpl.androidPhoneAdapterProvider.get(), singletonCImpl.androidPowerAdapterProvider.get(), singletonCImpl.androidFoldableAdapterProvider.get(), singletonCImpl.androidVolumeAdapterProvider.get());
            }
          };

          case 5: // io.github.sds100.keymapper.base.expertmode.SystemBridgeSetupAssistantController.Factory
          return (T) new SystemBridgeSetupAssistantController.Factory() {
            @Override
            public SystemBridgeSetupAssistantController create(CoroutineScope coroutineScope3,
                BaseAccessibilityService accessibilityService3) {
              return new SystemBridgeSetupAssistantController(coroutineScope3, accessibilityService3, singletonCImpl.bindManageNotificationsUseCaseProvider.get(), singletonCImpl.systemBridgeSetupControllerImplProvider.get(), singletonCImpl.preferenceRepositoryImplProvider.get(), singletonCImpl.systemBridgeConnectionManagerImplProvider.get(), singletonCImpl.provideClassProvider.get(), singletonCImpl.resourceProviderImplProvider.get());
            }
          };

          case 6: // io.github.sds100.keymapper.base.system.inputmethod.AutoSwitchImeController.Factory
          return (T) new AutoSwitchImeController.Factory() {
            @Override
            public AutoSwitchImeController create(BaseAccessibilityService service4,
                CoroutineScope coroutineScope4) {
              return new AutoSwitchImeController(service4, coroutineScope4, singletonCImpl.preferenceRepositoryImplProvider.get(), (InputMethodAdapter) (((Provider) (singletonCImpl.androidInputMethodAdapterProvider)).get()), singletonCImpl.pauseKeyMapsUseCaseImplProvider.get(), singletonCImpl.androidDevicesAdapterProvider.get(), singletonCImpl.androidToastAdapterProvider.get(), singletonCImpl.resourceProviderImplProvider.get(), singletonCImpl.provideBuildConfigProvider.get(), singletonCImpl.provideLockscreenAdapterProvider.get());
            }
          };

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class SingletonCImpl extends KeyMapperApp_HiltComponents.SingletonC {
    private final AppHiltModule appHiltModule;

    private final ApplicationContextModule applicationContextModule;

    private final AppDatabaseModule appDatabaseModule;

    private final SingletonCImpl singletonCImpl = this;

    Provider<CoroutineScope> provideCoroutineScopeProvider;

    Provider<KeyMapperClassProvider> provideClassProvider;

    Provider<AndroidNotificationAdapter> androidNotificationAdapterProvider;

    Provider<SuAdapterImpl> suAdapterImplProvider;

    Provider<BuildConfigProvider> provideBuildConfigProvider;

    Provider<NotificationReceiverAdapterImpl> notificationReceiverAdapterImplProvider;

    Provider<PreferenceRepositoryImpl> preferenceRepositoryImplProvider;

    Provider<AdbManagerImpl> adbManagerImplProvider;

    Provider<SystemBridgeStarter> systemBridgeStarterProvider;

    Provider<SystemBridgeConnectionManagerImpl> systemBridgeConnectionManagerImplProvider;

    Provider<AndroidPackageManagerAdapter> androidPackageManagerAdapterProvider;

    Provider<ShizukuAdapterImpl> shizukuAdapterImplProvider;

    Provider<AndroidPermissionAdapter> androidPermissionAdapterProvider;

    Provider<ManageNotificationsUseCaseImpl> manageNotificationsUseCaseImplProvider;

    Provider<ManageNotificationsUseCase> bindManageNotificationsUseCaseProvider;

    Provider<AndroidMediaAdapter> androidMediaAdapterProvider;

    Provider<AndroidRingtoneAdapter> androidRingtoneAdapterProvider;

    Provider<PauseKeyMapsUseCaseImpl> pauseKeyMapsUseCaseImplProvider;

    Provider<AccessibilityServiceAdapterImpl> accessibilityServiceAdapterImplProvider;

    Provider<ControlAccessibilityServiceUseCaseImpl> controlAccessibilityServiceUseCaseImplProvider;

    Provider<AndroidInputMethodAdapter> androidInputMethodAdapterProvider;

    Provider<SwitchImeAsyncImpl> switchImeAsyncImplProvider;

    Provider<ToggleCompatibleImeUseCaseImpl> toggleCompatibleImeUseCaseImplProvider;

    Provider<ShowHideInputMethodUseCaseImpl> showHideInputMethodUseCaseImplProvider;

    Provider<ShowHideInputMethodUseCase> bindShowHideInputMethodUseCaseProvider;

    Provider<AndroidFileAdapter> androidFileAdapterProvider;

    Provider<AppDatabase> provideAppDatabaseProvider;

    Provider<DispatcherProvider> provideDispatchersProvider;

    Provider<RoomKeyMapRepository> roomKeyMapRepositoryProvider;

    Provider<OnboardingUseCaseImpl> onboardingUseCaseImplProvider;

    Provider<ResourceProviderImpl> resourceProviderImplProvider;

    Provider<NotificationController> notificationControllerProvider;

    Provider<AndroidBluetoothAdapter> androidBluetoothAdapterProvider;

    Provider<AndroidDevicesAdapter> androidDevicesAdapterProvider;

    Provider<AutoGrantPermissionController> autoGrantPermissionControllerProvider;

    Provider<RoomLogRepository> roomLogRepositoryProvider;

    Provider<KeyMapperLoggingTree> keyMapperLoggingTreeProvider;

    Provider<KeyEventRelayServiceWrapperImpl> keyEventRelayServiceWrapperImplProvider;

    Provider<ClockImpl> clockImplProvider;

    Provider<SystemBridgeSetupControllerImpl> systemBridgeSetupControllerImplProvider;

    Provider<AndroidNetworkAdapter> androidNetworkAdapterProvider;

    Provider<SystemBridgeAutoStarter> systemBridgeAutoStarterProvider;

    Provider<SystemBridgeLogger> systemBridgeLoggerProvider;

    Provider<SystemBridgeConfigSync> systemBridgeConfigSyncProvider;

    Provider<EnableKeyMapsUseCaseImpl> enableKeyMapsUseCaseImplProvider;

    Provider<ImeInputEventInjectorImpl> imeInputEventInjectorImplProvider;

    Provider<EvdevDevicesDelegate> evdevDevicesDelegateProvider;

    Provider<InputEventHubImpl> inputEventHubImplProvider;

    Provider<NavigationProviderImpl> navigationProviderImplProvider;

    Provider<RoomFloatingButtonRepository> roomFloatingButtonRepositoryProvider;

    Provider<ConfigKeyMapStateImpl> configKeyMapStateImplProvider;

    Provider<DialogProviderImpl> dialogProviderImplProvider;

    Provider<StandardShellAdapter> standardShellAdapterProvider;

    Provider<AndroidClipboardAdapter> androidClipboardAdapterProvider;

    Provider<GetEventRecorderImpl> getEventRecorderImplProvider;

    Provider<RecordTriggerControllerImpl> recordTriggerControllerImplProvider;

    Provider<SetupAccessibilityServiceDelegateImpl> setupAccessibilityServiceDelegateImplProvider;

    Provider<PurchasingManager> providePurchasingManagerProvider;

    Provider<AndroidSystemFeatureAdapter> androidSystemFeatureAdapterProvider;

    Provider<AndroidCameraAdapter> androidCameraAdapterProvider;

    Provider<AndroidPhoneAdapter> androidPhoneAdapterProvider;

    Provider<AndroidSettingsAdapter> androidSettingsAdapterProvider;

    Provider<KeyMapShortcutActivityIntentBuilderImpl> keyMapShortcutActivityIntentBuilderImplProvider;

    Provider<AndroidAppShortcutAdapter> androidAppShortcutAdapterProvider;

    Provider<AndroidDisplayAdapter> androidDisplayAdapterProvider;

    Provider<SoundsManagerImpl> soundsManagerImplProvider;

    Provider<GetActionErrorUseCaseImpl> getActionErrorUseCaseImplProvider;

    Provider<GetConstraintErrorUseCaseImpl> getConstraintErrorUseCaseImplProvider;

    Provider<GetDefaultKeyMapOptionsUseCaseImpl> getDefaultKeyMapOptionsUseCaseImplProvider;

    Provider<RoomFloatingLayoutRepository> roomFloatingLayoutRepositoryProvider;

    Provider<FingerprintGesturesSupportedUseCaseImpl> fingerprintGesturesSupportedUseCaseImplProvider;

    Provider<RoomGroupRepository> roomGroupRepositoryProvider;

    Provider<DefaultUuidGenerator> defaultUuidGeneratorProvider;

    Provider<UuidGenerator> bindUuidGeneratorProvider;

    Provider<BackupManagerImpl> backupManagerImplProvider;

    Provider<ShowInputMethodPickerUseCaseImpl> showInputMethodPickerUseCaseImplProvider;

    Provider<ShowInputMethodPickerUseCase> bindShowInputMethodPickerUseCaseProvider;

    Provider<RoomAccessibilityNodeRepository> roomAccessibilityNodeRepositoryProvider;

    Provider<InteractUiElementController> interactUiElementControllerProvider;

    Provider<IntentAdapterImpl> intentAdapterImplProvider;

    Provider<AndroidToastAdapter> androidToastAdapterProvider;

    Provider<AndroidVolumeAdapter> androidVolumeAdapterProvider;

    Provider<AndroidLockScreenAdapter> androidLockScreenAdapterProvider;

    Provider<LockScreenAdapter> provideLockscreenAdapterProvider;

    Provider<AndroidAirplaneModeAdapter> androidAirplaneModeAdapterProvider;

    Provider<AndroidNfcAdapter> androidNfcAdapterProvider;

    Provider<AndroidOpenUrlAdapter> androidOpenUrlAdapterProvider;

    Provider<AndroidVibratorAdapter> androidVibratorAdapterProvider;

    Provider<AndroidPowerAdapter> androidPowerAdapterProvider;

    Provider<AndroidFoldableAdapter> androidFoldableAdapterProvider;

    SingletonCImpl(AppDatabaseModule appDatabaseModuleParam, AppHiltModule appHiltModuleParam,
        ApplicationContextModule applicationContextModuleParam) {
      this.appHiltModule = appHiltModuleParam;
      this.applicationContextModule = applicationContextModuleParam;
      this.appDatabaseModule = appDatabaseModuleParam;
      initialize(appDatabaseModuleParam, appHiltModuleParam, applicationContextModuleParam);
      initialize2(appDatabaseModuleParam, appHiltModuleParam, applicationContextModuleParam);
      initialize3(appDatabaseModuleParam, appHiltModuleParam, applicationContextModuleParam);
      initialize4(appDatabaseModuleParam, appHiltModuleParam, applicationContextModuleParam);

    }

    KeyMapDao keyMapDao() {
      return AppDatabaseModule_ProvideKeyMapDaoFactory.provideKeyMapDao(appDatabaseModule, provideAppDatabaseProvider.get());
    }

    FingerprintMapDao fingerprintMapDao() {
      return AppDatabaseModule_ProvideFingerprintMapDaoFactory.provideFingerprintMapDao(appDatabaseModule, provideAppDatabaseProvider.get());
    }

    LogEntryDao logEntryDao() {
      return AppDatabaseModule_ProvideLogEntryDaoFactory.provideLogEntryDao(appDatabaseModule, provideAppDatabaseProvider.get());
    }

    FloatingButtonDao floatingButtonDao() {
      return AppDatabaseModule_ProvideFloatingButtonDaoFactory.provideFloatingButtonDao(appDatabaseModule, provideAppDatabaseProvider.get());
    }

    ExecuteShellCommandUseCase executeShellCommandUseCase() {
      return new ExecuteShellCommandUseCase(standardShellAdapterProvider.get(), suAdapterImplProvider.get(), systemBridgeConnectionManagerImplProvider.get());
    }

    FloatingLayoutDao floatingLayoutDao() {
      return AppDatabaseModule_ProvideFloatingLayoutDaoFactory.provideFloatingLayoutDao(appDatabaseModule, provideAppDatabaseProvider.get());
    }

    GroupDao groupDao() {
      return AppDatabaseModule_ProvideGroupDaoFactory.provideGroupDao(appDatabaseModule, provideAppDatabaseProvider.get());
    }

    AccessibilityNodeDao accessibilityNodeDao() {
      return AppDatabaseModule_ProvideAccessibilityNodeDaoFactory.provideAccessibilityNodeDao(appDatabaseModule, provideAppDatabaseProvider.get());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final AppDatabaseModule appDatabaseModuleParam,
        final AppHiltModule appHiltModuleParam,
        final ApplicationContextModule applicationContextModuleParam) {
      this.provideCoroutineScopeProvider = DoubleCheck.provider(new SwitchingProvider<CoroutineScope>(singletonCImpl, 0));
      this.provideClassProvider = DoubleCheck.provider(new SwitchingProvider<KeyMapperClassProvider>(singletonCImpl, 4));
      this.androidNotificationAdapterProvider = DoubleCheck.provider(new SwitchingProvider<AndroidNotificationAdapter>(singletonCImpl, 3));
      this.suAdapterImplProvider = DoubleCheck.provider(new SwitchingProvider<SuAdapterImpl>(singletonCImpl, 6));
      this.provideBuildConfigProvider = DoubleCheck.provider(new SwitchingProvider<BuildConfigProvider>(singletonCImpl, 8));
      this.notificationReceiverAdapterImplProvider = DoubleCheck.provider(new SwitchingProvider<NotificationReceiverAdapterImpl>(singletonCImpl, 7));
      this.preferenceRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<PreferenceRepositoryImpl>(singletonCImpl, 9));
      this.adbManagerImplProvider = DoubleCheck.provider(new SwitchingProvider<AdbManagerImpl>(singletonCImpl, 12));
      this.systemBridgeStarterProvider = DoubleCheck.provider(new SwitchingProvider<SystemBridgeStarter>(singletonCImpl, 11));
      this.systemBridgeConnectionManagerImplProvider = DoubleCheck.provider(new SwitchingProvider<SystemBridgeConnectionManagerImpl>(singletonCImpl, 10));
      this.androidPackageManagerAdapterProvider = DoubleCheck.provider(new SwitchingProvider<AndroidPackageManagerAdapter>(singletonCImpl, 14));
      this.shizukuAdapterImplProvider = DoubleCheck.provider(new SwitchingProvider<ShizukuAdapterImpl>(singletonCImpl, 13));
      this.androidPermissionAdapterProvider = DoubleCheck.provider(new SwitchingProvider<AndroidPermissionAdapter>(singletonCImpl, 5));
      this.manageNotificationsUseCaseImplProvider = new SwitchingProvider<>(singletonCImpl, 2);
      this.bindManageNotificationsUseCaseProvider = DoubleCheck.provider((Provider) (manageNotificationsUseCaseImplProvider));
      this.androidMediaAdapterProvider = DoubleCheck.provider(new SwitchingProvider<AndroidMediaAdapter>(singletonCImpl, 16));
      this.androidRingtoneAdapterProvider = DoubleCheck.provider(new SwitchingProvider<AndroidRingtoneAdapter>(singletonCImpl, 17));
      this.pauseKeyMapsUseCaseImplProvider = DoubleCheck.provider(new SwitchingProvider<PauseKeyMapsUseCaseImpl>(singletonCImpl, 15));
      this.accessibilityServiceAdapterImplProvider = DoubleCheck.provider(new SwitchingProvider<AccessibilityServiceAdapterImpl>(singletonCImpl, 19));
      this.controlAccessibilityServiceUseCaseImplProvider = DoubleCheck.provider(new SwitchingProvider<ControlAccessibilityServiceUseCaseImpl>(singletonCImpl, 18));
      this.androidInputMethodAdapterProvider = DoubleCheck.provider(new SwitchingProvider<AndroidInputMethodAdapter>(singletonCImpl, 21));
      this.switchImeAsyncImplProvider = DoubleCheck.provider(new SwitchingProvider<SwitchImeAsyncImpl>(singletonCImpl, 22));
      this.toggleCompatibleImeUseCaseImplProvider = DoubleCheck.provider(new SwitchingProvider<ToggleCompatibleImeUseCaseImpl>(singletonCImpl, 20));
      this.showHideInputMethodUseCaseImplProvider = new SwitchingProvider<>(singletonCImpl, 23);
      this.bindShowHideInputMethodUseCaseProvider = DoubleCheck.provider((Provider) (showHideInputMethodUseCaseImplProvider));
    }

    @SuppressWarnings("unchecked")
    private void initialize2(final AppDatabaseModule appDatabaseModuleParam,
        final AppHiltModule appHiltModuleParam,
        final ApplicationContextModule applicationContextModuleParam) {
      this.androidFileAdapterProvider = DoubleCheck.provider(new SwitchingProvider<AndroidFileAdapter>(singletonCImpl, 25));
      this.provideAppDatabaseProvider = DoubleCheck.provider(new SwitchingProvider<AppDatabase>(singletonCImpl, 27));
      this.provideDispatchersProvider = DoubleCheck.provider(new SwitchingProvider<DispatcherProvider>(singletonCImpl, 28));
      this.roomKeyMapRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<RoomKeyMapRepository>(singletonCImpl, 26));
      this.onboardingUseCaseImplProvider = DoubleCheck.provider(new SwitchingProvider<OnboardingUseCaseImpl>(singletonCImpl, 24));
      this.resourceProviderImplProvider = DoubleCheck.provider(new SwitchingProvider<ResourceProviderImpl>(singletonCImpl, 29));
      this.notificationControllerProvider = DoubleCheck.provider(new SwitchingProvider<NotificationController>(singletonCImpl, 1));
      this.androidBluetoothAdapterProvider = DoubleCheck.provider(new SwitchingProvider<AndroidBluetoothAdapter>(singletonCImpl, 31));
      this.androidDevicesAdapterProvider = DoubleCheck.provider(new SwitchingProvider<AndroidDevicesAdapter>(singletonCImpl, 30));
      this.autoGrantPermissionControllerProvider = DoubleCheck.provider(new SwitchingProvider<AutoGrantPermissionController>(singletonCImpl, 32));
      this.roomLogRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<RoomLogRepository>(singletonCImpl, 34));
      this.keyMapperLoggingTreeProvider = new SwitchingProvider<>(singletonCImpl, 33);
      this.keyEventRelayServiceWrapperImplProvider = DoubleCheck.provider(new SwitchingProvider<KeyEventRelayServiceWrapperImpl>(singletonCImpl, 35));
      this.clockImplProvider = DoubleCheck.provider(new SwitchingProvider<ClockImpl>(singletonCImpl, 37));
      this.systemBridgeSetupControllerImplProvider = DoubleCheck.provider(new SwitchingProvider<SystemBridgeSetupControllerImpl>(singletonCImpl, 38));
      this.androidNetworkAdapterProvider = DoubleCheck.provider(new SwitchingProvider<AndroidNetworkAdapter>(singletonCImpl, 39));
      this.systemBridgeAutoStarterProvider = DoubleCheck.provider(new SwitchingProvider<SystemBridgeAutoStarter>(singletonCImpl, 36));
      this.systemBridgeLoggerProvider = DoubleCheck.provider(new SwitchingProvider<SystemBridgeLogger>(singletonCImpl, 40));
      this.systemBridgeConfigSyncProvider = DoubleCheck.provider(new SwitchingProvider<SystemBridgeConfigSync>(singletonCImpl, 41));
      this.enableKeyMapsUseCaseImplProvider = DoubleCheck.provider(new SwitchingProvider<EnableKeyMapsUseCaseImpl>(singletonCImpl, 42));
      this.imeInputEventInjectorImplProvider = DoubleCheck.provider(new SwitchingProvider<ImeInputEventInjectorImpl>(singletonCImpl, 44));
      this.evdevDevicesDelegateProvider = DoubleCheck.provider(new SwitchingProvider<EvdevDevicesDelegate>(singletonCImpl, 45));
      this.inputEventHubImplProvider = DoubleCheck.provider(new SwitchingProvider<InputEventHubImpl>(singletonCImpl, 43));
      this.navigationProviderImplProvider = DoubleCheck.provider(new SwitchingProvider<NavigationProviderImpl>(singletonCImpl, 46));
      this.roomFloatingButtonRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<RoomFloatingButtonRepository>(singletonCImpl, 48));
    }

    @SuppressWarnings("unchecked")
    private void initialize3(final AppDatabaseModule appDatabaseModuleParam,
        final AppHiltModule appHiltModuleParam,
        final ApplicationContextModule applicationContextModuleParam) {
      this.configKeyMapStateImplProvider = DoubleCheck.provider(new SwitchingProvider<ConfigKeyMapStateImpl>(singletonCImpl, 47));
      this.dialogProviderImplProvider = DoubleCheck.provider(new SwitchingProvider<DialogProviderImpl>(singletonCImpl, 49));
      this.standardShellAdapterProvider = DoubleCheck.provider(new SwitchingProvider<StandardShellAdapter>(singletonCImpl, 52));
      this.androidClipboardAdapterProvider = DoubleCheck.provider(new SwitchingProvider<AndroidClipboardAdapter>(singletonCImpl, 53));
      this.getEventRecorderImplProvider = DoubleCheck.provider(new SwitchingProvider<GetEventRecorderImpl>(singletonCImpl, 51));
      this.recordTriggerControllerImplProvider = DoubleCheck.provider(new SwitchingProvider<RecordTriggerControllerImpl>(singletonCImpl, 50));
      this.setupAccessibilityServiceDelegateImplProvider = DoubleCheck.provider(new SwitchingProvider<SetupAccessibilityServiceDelegateImpl>(singletonCImpl, 54));
      this.providePurchasingManagerProvider = DoubleCheck.provider(new SwitchingProvider<PurchasingManager>(singletonCImpl, 55));
      this.androidSystemFeatureAdapterProvider = DoubleCheck.provider(new SwitchingProvider<AndroidSystemFeatureAdapter>(singletonCImpl, 56));
      this.androidCameraAdapterProvider = DoubleCheck.provider(new SwitchingProvider<AndroidCameraAdapter>(singletonCImpl, 57));
      this.androidPhoneAdapterProvider = DoubleCheck.provider(new SwitchingProvider<AndroidPhoneAdapter>(singletonCImpl, 58));
      this.androidSettingsAdapterProvider = DoubleCheck.provider(new SwitchingProvider<AndroidSettingsAdapter>(singletonCImpl, 59));
      this.keyMapShortcutActivityIntentBuilderImplProvider = DoubleCheck.provider(new SwitchingProvider<KeyMapShortcutActivityIntentBuilderImpl>(singletonCImpl, 61));
      this.androidAppShortcutAdapterProvider = DoubleCheck.provider(new SwitchingProvider<AndroidAppShortcutAdapter>(singletonCImpl, 60));
      this.androidDisplayAdapterProvider = DoubleCheck.provider(new SwitchingProvider<AndroidDisplayAdapter>(singletonCImpl, 62));
      this.soundsManagerImplProvider = DoubleCheck.provider(new SwitchingProvider<SoundsManagerImpl>(singletonCImpl, 63));
      this.getActionErrorUseCaseImplProvider = DoubleCheck.provider(new SwitchingProvider<GetActionErrorUseCaseImpl>(singletonCImpl, 64));
      this.getConstraintErrorUseCaseImplProvider = DoubleCheck.provider(new SwitchingProvider<GetConstraintErrorUseCaseImpl>(singletonCImpl, 65));
      this.getDefaultKeyMapOptionsUseCaseImplProvider = DoubleCheck.provider(new SwitchingProvider<GetDefaultKeyMapOptionsUseCaseImpl>(singletonCImpl, 66));
      this.roomFloatingLayoutRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<RoomFloatingLayoutRepository>(singletonCImpl, 67));
      this.fingerprintGesturesSupportedUseCaseImplProvider = DoubleCheck.provider(new SwitchingProvider<FingerprintGesturesSupportedUseCaseImpl>(singletonCImpl, 68));
      this.roomGroupRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<RoomGroupRepository>(singletonCImpl, 69));
      this.defaultUuidGeneratorProvider = new SwitchingProvider<>(singletonCImpl, 71);
      this.bindUuidGeneratorProvider = DoubleCheck.provider((Provider) (defaultUuidGeneratorProvider));
      this.backupManagerImplProvider = DoubleCheck.provider(new SwitchingProvider<BackupManagerImpl>(singletonCImpl, 70));
    }

    @SuppressWarnings("unchecked")
    private void initialize4(final AppDatabaseModule appDatabaseModuleParam,
        final AppHiltModule appHiltModuleParam,
        final ApplicationContextModule applicationContextModuleParam) {
      this.showInputMethodPickerUseCaseImplProvider = new SwitchingProvider<>(singletonCImpl, 72);
      this.bindShowInputMethodPickerUseCaseProvider = DoubleCheck.provider((Provider) (showInputMethodPickerUseCaseImplProvider));
      this.roomAccessibilityNodeRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<RoomAccessibilityNodeRepository>(singletonCImpl, 74));
      this.interactUiElementControllerProvider = DoubleCheck.provider(new SwitchingProvider<InteractUiElementController>(singletonCImpl, 73));
      this.intentAdapterImplProvider = DoubleCheck.provider(new SwitchingProvider<IntentAdapterImpl>(singletonCImpl, 75));
      this.androidToastAdapterProvider = DoubleCheck.provider(new SwitchingProvider<AndroidToastAdapter>(singletonCImpl, 76));
      this.androidVolumeAdapterProvider = DoubleCheck.provider(new SwitchingProvider<AndroidVolumeAdapter>(singletonCImpl, 77));
      this.androidLockScreenAdapterProvider = new SwitchingProvider<>(singletonCImpl, 78);
      this.provideLockscreenAdapterProvider = DoubleCheck.provider((Provider) (androidLockScreenAdapterProvider));
      this.androidAirplaneModeAdapterProvider = DoubleCheck.provider(new SwitchingProvider<AndroidAirplaneModeAdapter>(singletonCImpl, 79));
      this.androidNfcAdapterProvider = DoubleCheck.provider(new SwitchingProvider<AndroidNfcAdapter>(singletonCImpl, 80));
      this.androidOpenUrlAdapterProvider = DoubleCheck.provider(new SwitchingProvider<AndroidOpenUrlAdapter>(singletonCImpl, 81));
      this.androidVibratorAdapterProvider = DoubleCheck.provider(new SwitchingProvider<AndroidVibratorAdapter>(singletonCImpl, 82));
      this.androidPowerAdapterProvider = DoubleCheck.provider(new SwitchingProvider<AndroidPowerAdapter>(singletonCImpl, 83));
      this.androidFoldableAdapterProvider = DoubleCheck.provider(new SwitchingProvider<AndroidFoldableAdapter>(singletonCImpl, 84));
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return Collections.<Boolean>emptySet();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    @Override
    public void injectKeyMapperApp(KeyMapperApp arg0) {
      injectKeyMapperApp2(arg0);
    }

    @Override
    public void injectEnableKeyMapsBroadcastReceiver(EnableKeyMapsBroadcastReceiver arg0) {
      injectEnableKeyMapsBroadcastReceiver2(arg0);
    }

    @Override
    public void injectPauseMappingsBroadcastReceiver(PauseMappingsBroadcastReceiver arg0) {
      injectPauseMappingsBroadcastReceiver2(arg0);
    }

    @Override
    public void injectTriggerKeyMapsBroadcastReceiver(TriggerKeyMapsBroadcastReceiver arg0) {
      injectTriggerKeyMapsBroadcastReceiver2(arg0);
    }

    @Override
    public SystemBridgeConnectionManagerImpl systemBridgeManager() {
      return systemBridgeConnectionManagerImplProvider.get();
    }

    @Override
    public void injectBluetoothBroadcastReceiver(BluetoothBroadcastReceiver arg0) {
      injectBluetoothBroadcastReceiver2(arg0);
    }

    @CanIgnoreReturnValue
    private KeyMapperApp injectKeyMapperApp2(KeyMapperApp instance) {
      BaseKeyMapperApp_MembersInjector.injectAppCoroutineScope(instance, DoubleCheck.lazy(provideCoroutineScopeProvider));
      BaseKeyMapperApp_MembersInjector.injectNotificationController(instance, DoubleCheck.lazy(notificationControllerProvider));
      BaseKeyMapperApp_MembersInjector.injectPackageManagerAdapter(instance, DoubleCheck.lazy(androidPackageManagerAdapterProvider));
      BaseKeyMapperApp_MembersInjector.injectDevicesAdapter(instance, DoubleCheck.lazy(androidDevicesAdapterProvider));
      BaseKeyMapperApp_MembersInjector.injectPermissionAdapter(instance, DoubleCheck.lazy(androidPermissionAdapterProvider));
      BaseKeyMapperApp_MembersInjector.injectAccessibilityServiceAdapter(instance, DoubleCheck.lazy(accessibilityServiceAdapterImplProvider));
      BaseKeyMapperApp_MembersInjector.injectAutoGrantPermissionController(instance, DoubleCheck.lazy(autoGrantPermissionControllerProvider));
      BaseKeyMapperApp_MembersInjector.injectLoggingTree(instance, DoubleCheck.lazy(keyMapperLoggingTreeProvider));
      BaseKeyMapperApp_MembersInjector.injectSettingsRepository(instance, DoubleCheck.lazy(preferenceRepositoryImplProvider));
      BaseKeyMapperApp_MembersInjector.injectLogRepository(instance, DoubleCheck.lazy(((Provider) (roomLogRepositoryProvider))));
      BaseKeyMapperApp_MembersInjector.injectKeyEventRelayServiceWrapper(instance, DoubleCheck.lazy(keyEventRelayServiceWrapperImplProvider));
      BaseKeyMapperApp_MembersInjector.injectSystemBridgeAutoStarter(instance, DoubleCheck.lazy(systemBridgeAutoStarterProvider));
      BaseKeyMapperApp_MembersInjector.injectSystemBridgeConnectionManager(instance, DoubleCheck.lazy(systemBridgeConnectionManagerImplProvider));
      BaseKeyMapperApp_MembersInjector.injectSystemBridgeLogger(instance, DoubleCheck.lazy(systemBridgeLoggerProvider));
      BaseKeyMapperApp_MembersInjector.injectSystemBridgeConfigSync(instance, DoubleCheck.lazy(systemBridgeConfigSyncProvider));
      return instance;
    }

    @CanIgnoreReturnValue
    private EnableKeyMapsBroadcastReceiver injectEnableKeyMapsBroadcastReceiver2(
        EnableKeyMapsBroadcastReceiver instance2) {
      EnableKeyMapsBroadcastReceiver_MembersInjector.injectUseCase(instance2, enableKeyMapsUseCaseImplProvider.get());
      return instance2;
    }

    @CanIgnoreReturnValue
    private PauseMappingsBroadcastReceiver injectPauseMappingsBroadcastReceiver2(
        PauseMappingsBroadcastReceiver instance3) {
      PauseMappingsBroadcastReceiver_MembersInjector.injectUseCase(instance3, pauseKeyMapsUseCaseImplProvider.get());
      return instance3;
    }

    @CanIgnoreReturnValue
    private TriggerKeyMapsBroadcastReceiver injectTriggerKeyMapsBroadcastReceiver2(
        TriggerKeyMapsBroadcastReceiver instance4) {
      TriggerKeyMapsBroadcastReceiver_MembersInjector.injectServiceAdapter(instance4, accessibilityServiceAdapterImplProvider.get());
      TriggerKeyMapsBroadcastReceiver_MembersInjector.injectCoroutineScope(instance4, provideCoroutineScopeProvider.get());
      return instance4;
    }

    @CanIgnoreReturnValue
    private BluetoothBroadcastReceiver injectBluetoothBroadcastReceiver2(
        BluetoothBroadcastReceiver instance5) {
      BluetoothBroadcastReceiver_MembersInjector.injectBluetoothAdapter(instance5, androidBluetoothAdapterProvider.get());
      return instance5;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // kotlinx.coroutines.CoroutineScope
          return (T) AppHiltModule_ProvideCoroutineScopeFactory.provideCoroutineScope(singletonCImpl.appHiltModule);

          case 1: // io.github.sds100.keymapper.base.system.notifications.NotificationController
          return (T) new NotificationController(singletonCImpl.provideCoroutineScopeProvider.get(), singletonCImpl.bindManageNotificationsUseCaseProvider.get(), singletonCImpl.pauseKeyMapsUseCaseImplProvider.get(), singletonCImpl.controlAccessibilityServiceUseCaseImplProvider.get(), singletonCImpl.toggleCompatibleImeUseCaseImplProvider.get(), singletonCImpl.bindShowHideInputMethodUseCaseProvider.get(), singletonCImpl.onboardingUseCaseImplProvider.get(), singletonCImpl.resourceProviderImplProvider.get(), singletonCImpl.systemBridgeConnectionManagerImplProvider.get(), singletonCImpl.provideDispatchersProvider.get());

          case 2: // io.github.sds100.keymapper.base.system.notifications.ManageNotificationsUseCaseImpl
          return (T) new ManageNotificationsUseCaseImpl(singletonCImpl.androidNotificationAdapterProvider.get(), singletonCImpl.androidPermissionAdapterProvider.get());

          case 3: // io.github.sds100.keymapper.base.system.notifications.AndroidNotificationAdapter
          return (T) new AndroidNotificationAdapter(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideCoroutineScopeProvider.get(), singletonCImpl.provideClassProvider.get());

          case 4: // io.github.sds100.keymapper.common.KeyMapperClassProvider
          return (T) AppHiltModule_ProvideClassProviderFactory.provideClassProvider(singletonCImpl.appHiltModule);

          case 5: // io.github.sds100.keymapper.system.permissions.AndroidPermissionAdapter
          return (T) new AndroidPermissionAdapter(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideCoroutineScopeProvider.get(), singletonCImpl.suAdapterImplProvider.get(), singletonCImpl.notificationReceiverAdapterImplProvider.get(), singletonCImpl.preferenceRepositoryImplProvider.get(), singletonCImpl.provideBuildConfigProvider.get(), singletonCImpl.systemBridgeConnectionManagerImplProvider.get(), singletonCImpl.shizukuAdapterImplProvider.get());

          case 6: // io.github.sds100.keymapper.system.root.SuAdapterImpl
          return (T) new SuAdapterImpl(singletonCImpl.provideCoroutineScopeProvider.get());

          case 7: // io.github.sds100.keymapper.system.notifications.NotificationReceiverAdapterImpl
          return (T) new NotificationReceiverAdapterImpl(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideCoroutineScopeProvider.get(), singletonCImpl.provideBuildConfigProvider.get());

          case 8: // io.github.sds100.keymapper.common.BuildConfigProvider
          return (T) AppHiltModule_ProvideBuildConfigProviderFactory.provideBuildConfigProvider(singletonCImpl.appHiltModule);

          case 9: // io.github.sds100.keymapper.data.repositories.PreferenceRepositoryImpl
          return (T) new PreferenceRepositoryImpl(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideCoroutineScopeProvider.get());

          case 10: // io.github.sds100.keymapper.sysbridge.manager.SystemBridgeConnectionManagerImpl
          return (T) new SystemBridgeConnectionManagerImpl(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideCoroutineScopeProvider.get(), singletonCImpl.preferenceRepositoryImplProvider.get(), singletonCImpl.systemBridgeStarterProvider.get(), singletonCImpl.provideBuildConfigProvider.get());

          case 11: // io.github.sds100.keymapper.sysbridge.starter.SystemBridgeStarter
          return (T) new SystemBridgeStarter(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.adbManagerImplProvider.get(), singletonCImpl.provideBuildConfigProvider.get());

          case 12: // io.github.sds100.keymapper.sysbridge.adb.AdbManagerImpl
          return (T) new AdbManagerImpl(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 13: // io.github.sds100.keymapper.system.shizuku.ShizukuAdapterImpl
          return (T) new ShizukuAdapterImpl(singletonCImpl.provideCoroutineScopeProvider.get(), singletonCImpl.androidPackageManagerAdapterProvider.get());

          case 14: // io.github.sds100.keymapper.system.apps.AndroidPackageManagerAdapter
          return (T) new AndroidPackageManagerAdapter(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideCoroutineScopeProvider.get());

          case 15: // io.github.sds100.keymapper.base.keymaps.PauseKeyMapsUseCaseImpl
          return (T) new PauseKeyMapsUseCaseImpl(singletonCImpl.preferenceRepositoryImplProvider.get(), singletonCImpl.androidMediaAdapterProvider.get(), singletonCImpl.androidRingtoneAdapterProvider.get());

          case 16: // io.github.sds100.keymapper.system.media.AndroidMediaAdapter
          return (T) new AndroidMediaAdapter(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideCoroutineScopeProvider.get());

          case 17: // io.github.sds100.keymapper.system.ringtones.AndroidRingtoneAdapter
          return (T) new AndroidRingtoneAdapter(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 18: // io.github.sds100.keymapper.base.system.accessibility.ControlAccessibilityServiceUseCaseImpl
          return (T) new ControlAccessibilityServiceUseCaseImpl(singletonCImpl.accessibilityServiceAdapterImplProvider.get(), singletonCImpl.androidPermissionAdapterProvider.get(), singletonCImpl.androidPackageManagerAdapterProvider.get());

          case 19: // io.github.sds100.keymapper.base.system.accessibility.AccessibilityServiceAdapterImpl
          return (T) new AccessibilityServiceAdapterImpl(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideCoroutineScopeProvider.get(), singletonCImpl.androidPermissionAdapterProvider.get(), singletonCImpl.provideBuildConfigProvider.get(), singletonCImpl.provideClassProvider.get());

          case 20: // io.github.sds100.keymapper.base.system.inputmethod.ToggleCompatibleImeUseCaseImpl
          return (T) new ToggleCompatibleImeUseCaseImpl((InputMethodAdapter) (((Provider) (singletonCImpl.androidInputMethodAdapterProvider)).get()), singletonCImpl.provideBuildConfigProvider.get(), singletonCImpl.switchImeAsyncImplProvider.get(), singletonCImpl.accessibilityServiceAdapterImplProvider.get(), singletonCImpl.androidPermissionAdapterProvider.get());

          case 21: // io.github.sds100.keymapper.system.inputmethod.AndroidInputMethodAdapter
          return (T) new AndroidInputMethodAdapter(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideCoroutineScopeProvider.get(), singletonCImpl.suAdapterImplProvider.get());

          case 22: // io.github.sds100.keymapper.base.system.inputmethod.SwitchImeAsyncImpl
          return (T) new SwitchImeAsyncImpl(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.accessibilityServiceAdapterImplProvider.get(), (InputMethodAdapter) (((Provider) (singletonCImpl.androidInputMethodAdapterProvider)).get()), singletonCImpl.provideBuildConfigProvider.get(), singletonCImpl.androidPermissionAdapterProvider.get(), singletonCImpl.suAdapterImplProvider.get());

          case 23: // io.github.sds100.keymapper.base.system.inputmethod.ShowHideInputMethodUseCaseImpl
          return (T) new ShowHideInputMethodUseCaseImpl(singletonCImpl.accessibilityServiceAdapterImplProvider.get());

          case 24: // io.github.sds100.keymapper.base.onboarding.OnboardingUseCaseImpl
          return (T) new OnboardingUseCaseImpl(singletonCImpl.preferenceRepositoryImplProvider.get(), singletonCImpl.androidFileAdapterProvider.get(), singletonCImpl.shizukuAdapterImplProvider.get(), singletonCImpl.androidPermissionAdapterProvider.get(), singletonCImpl.roomKeyMapRepositoryProvider.get(), singletonCImpl.provideBuildConfigProvider.get());

          case 25: // io.github.sds100.keymapper.system.files.AndroidFileAdapter
          return (T) new AndroidFileAdapter(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideBuildConfigProvider.get());

          case 26: // io.github.sds100.keymapper.data.repositories.RoomKeyMapRepository
          return (T) new RoomKeyMapRepository(singletonCImpl.keyMapDao(), singletonCImpl.fingerprintMapDao(), singletonCImpl.provideCoroutineScopeProvider.get(), singletonCImpl.provideDispatchersProvider.get());

          case 27: // io.github.sds100.keymapper.data.db.AppDatabase
          return (T) AppDatabaseModule_ProvideAppDatabaseFactory.provideAppDatabase(singletonCImpl.appDatabaseModule, ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 28: // io.github.sds100.keymapper.common.utils.DispatcherProvider
          return (T) AppHiltModule_ProvideDispatchersFactory.provideDispatchers(singletonCImpl.appHiltModule);

          case 29: // io.github.sds100.keymapper.base.utils.ui.ResourceProviderImpl
          return (T) new ResourceProviderImpl(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 30: // io.github.sds100.keymapper.system.devices.AndroidDevicesAdapter
          return (T) new AndroidDevicesAdapter(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.androidBluetoothAdapterProvider.get(), singletonCImpl.androidPermissionAdapterProvider.get(), singletonCImpl.provideCoroutineScopeProvider.get());

          case 31: // io.github.sds100.keymapper.system.bluetooth.AndroidBluetoothAdapter
          return (T) new AndroidBluetoothAdapter(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideCoroutineScopeProvider.get(), singletonCImpl.systemBridgeConnectionManagerImplProvider.get());

          case 32: // io.github.sds100.keymapper.base.system.permissions.AutoGrantPermissionController
          return (T) new AutoGrantPermissionController(singletonCImpl.provideCoroutineScopeProvider.get(), singletonCImpl.androidPermissionAdapterProvider.get(), singletonCImpl.shizukuAdapterImplProvider.get());

          case 33: // io.github.sds100.keymapper.base.logging.KeyMapperLoggingTree
          return (T) new KeyMapperLoggingTree(singletonCImpl.provideCoroutineScopeProvider.get(), singletonCImpl.preferenceRepositoryImplProvider.get(), (LogRepository) (((Provider) (singletonCImpl.roomLogRepositoryProvider)).get()));

          case 34: // io.github.sds100.keymapper.data.repositories.RoomLogRepository
          return (T) new RoomLogRepository(singletonCImpl.provideCoroutineScopeProvider.get(), singletonCImpl.logEntryDao());

          case 35: // io.github.sds100.keymapper.system.inputmethod.KeyEventRelayServiceWrapperImpl
          return (T) new KeyEventRelayServiceWrapperImpl(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideBuildConfigProvider.get());

          case 36: // io.github.sds100.keymapper.base.expertmode.SystemBridgeAutoStarter
          return (T) new SystemBridgeAutoStarter(singletonCImpl.provideCoroutineScopeProvider.get(), singletonCImpl.provideBuildConfigProvider.get(), singletonCImpl.clockImplProvider.get(), singletonCImpl.suAdapterImplProvider.get(), singletonCImpl.shizukuAdapterImplProvider.get(), singletonCImpl.systemBridgeConnectionManagerImplProvider.get(), singletonCImpl.systemBridgeSetupControllerImplProvider.get(), singletonCImpl.preferenceRepositoryImplProvider.get(), singletonCImpl.androidNetworkAdapterProvider.get(), singletonCImpl.androidPermissionAdapterProvider.get(), singletonCImpl.androidNotificationAdapterProvider.get(), singletonCImpl.resourceProviderImplProvider.get());

          case 37: // io.github.sds100.keymapper.common.utils.ClockImpl
          return (T) new ClockImpl();

          case 38: // io.github.sds100.keymapper.sysbridge.service.SystemBridgeSetupControllerImpl
          return (T) new SystemBridgeSetupControllerImpl(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideCoroutineScopeProvider.get(), singletonCImpl.adbManagerImplProvider.get(), singletonCImpl.provideClassProvider.get(), singletonCImpl.systemBridgeConnectionManagerImplProvider.get());

          case 39: // io.github.sds100.keymapper.system.network.AndroidNetworkAdapter
          return (T) new AndroidNetworkAdapter(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.suAdapterImplProvider.get(), singletonCImpl.systemBridgeConnectionManagerImplProvider.get());

          case 40: // io.github.sds100.keymapper.base.logging.SystemBridgeLogger
          return (T) new SystemBridgeLogger(singletonCImpl.provideCoroutineScopeProvider.get(), singletonCImpl.systemBridgeConnectionManagerImplProvider.get(), singletonCImpl.preferenceRepositoryImplProvider.get());

          case 41: // io.github.sds100.keymapper.base.expertmode.SystemBridgeConfigSync
          return (T) new SystemBridgeConfigSync(singletonCImpl.provideCoroutineScopeProvider.get(), singletonCImpl.systemBridgeConnectionManagerImplProvider.get(), singletonCImpl.preferenceRepositoryImplProvider.get());

          case 42: // io.github.sds100.keymapper.base.keymaps.EnableKeyMapsUseCaseImpl
          return (T) new EnableKeyMapsUseCaseImpl(singletonCImpl.roomKeyMapRepositoryProvider.get());

          case 43: // io.github.sds100.keymapper.base.input.InputEventHubImpl
          return (T) new InputEventHubImpl(singletonCImpl.provideCoroutineScopeProvider.get(), singletonCImpl.systemBridgeConnectionManagerImplProvider.get(), singletonCImpl.imeInputEventInjectorImplProvider.get(), singletonCImpl.preferenceRepositoryImplProvider.get(), singletonCImpl.evdevDevicesDelegateProvider.get());

          case 44: // io.github.sds100.keymapper.base.system.inputmethod.ImeInputEventInjectorImpl
          return (T) new ImeInputEventInjectorImpl(singletonCImpl.keyEventRelayServiceWrapperImplProvider.get(), (InputMethodAdapter) (((Provider) (singletonCImpl.androidInputMethodAdapterProvider)).get()));

          case 45: // io.github.sds100.keymapper.base.input.EvdevDevicesDelegate
          return (T) new EvdevDevicesDelegate(singletonCImpl.provideCoroutineScopeProvider.get(), singletonCImpl.systemBridgeConnectionManagerImplProvider.get());

          case 46: // io.github.sds100.keymapper.base.utils.navigation.NavigationProviderImpl
          return (T) new NavigationProviderImpl();

          case 47: // io.github.sds100.keymapper.base.keymaps.ConfigKeyMapStateImpl
          return (T) new ConfigKeyMapStateImpl(singletonCImpl.provideCoroutineScopeProvider.get(), singletonCImpl.roomKeyMapRepositoryProvider.get(), singletonCImpl.roomFloatingButtonRepositoryProvider.get());

          case 48: // io.github.sds100.keymapper.data.repositories.RoomFloatingButtonRepository
          return (T) new RoomFloatingButtonRepository(singletonCImpl.floatingButtonDao(), singletonCImpl.provideCoroutineScopeProvider.get(), singletonCImpl.provideDispatchersProvider.get());

          case 49: // io.github.sds100.keymapper.base.utils.ui.DialogProviderImpl
          return (T) new DialogProviderImpl();

          case 50: // io.github.sds100.keymapper.base.trigger.RecordTriggerControllerImpl
          return (T) new RecordTriggerControllerImpl(singletonCImpl.provideCoroutineScopeProvider.get(), singletonCImpl.inputEventHubImplProvider.get(), singletonCImpl.accessibilityServiceAdapterImplProvider.get(), singletonCImpl.getEventRecorderImplProvider.get(), singletonCImpl.systemBridgeConnectionManagerImplProvider.get());

          case 51: // io.github.sds100.keymapper.base.debug.GetEventRecorderImpl
          return (T) new GetEventRecorderImpl(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideCoroutineScopeProvider.get(), singletonCImpl.executeShellCommandUseCase(), singletonCImpl.preferenceRepositoryImplProvider.get(), singletonCImpl.androidClipboardAdapterProvider.get(), singletonCImpl.androidFileAdapterProvider.get(), singletonCImpl.provideBuildConfigProvider.get(), singletonCImpl.resourceProviderImplProvider.get());

          case 52: // io.github.sds100.keymapper.system.shell.StandardShellAdapter
          return (T) new StandardShellAdapter();

          case 53: // io.github.sds100.keymapper.system.clipboard.AndroidClipboardAdapter
          return (T) new AndroidClipboardAdapter(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 54: // io.github.sds100.keymapper.base.onboarding.SetupAccessibilityServiceDelegateImpl
          return (T) new SetupAccessibilityServiceDelegateImpl(singletonCImpl.controlAccessibilityServiceUseCaseImplProvider.get(), singletonCImpl.resourceProviderImplProvider.get());

          case 55: // io.github.sds100.keymapper.base.purchasing.PurchasingManager
          return (T) AppHiltModule_ProvidePurchasingManagerFactory.providePurchasingManager(singletonCImpl.appHiltModule);

          case 56: // io.github.sds100.keymapper.system.AndroidSystemFeatureAdapter
          return (T) new AndroidSystemFeatureAdapter(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 57: // io.github.sds100.keymapper.system.camera.AndroidCameraAdapter
          return (T) new AndroidCameraAdapter(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 58: // io.github.sds100.keymapper.system.phone.AndroidPhoneAdapter
          return (T) new AndroidPhoneAdapter(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideCoroutineScopeProvider.get());

          case 59: // io.github.sds100.keymapper.system.settings.AndroidSettingsAdapter
          return (T) new AndroidSettingsAdapter(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 60: // io.github.sds100.keymapper.system.apps.AndroidAppShortcutAdapter
          return (T) new AndroidAppShortcutAdapter(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.keyMapShortcutActivityIntentBuilderImplProvider.get());

          case 61: // io.github.sds100.keymapper.api.KeyMapShortcutActivityIntentBuilderImpl
          return (T) new KeyMapShortcutActivityIntentBuilderImpl(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 62: // io.github.sds100.keymapper.system.display.AndroidDisplayAdapter
          return (T) new AndroidDisplayAdapter(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideCoroutineScopeProvider.get());

          case 63: // io.github.sds100.keymapper.base.actions.sound.SoundsManagerImpl
          return (T) new SoundsManagerImpl(singletonCImpl.provideCoroutineScopeProvider.get(), singletonCImpl.androidFileAdapterProvider.get());

          case 64: // io.github.sds100.keymapper.base.actions.GetActionErrorUseCaseImpl
          return (T) new GetActionErrorUseCaseImpl(singletonCImpl.androidPackageManagerAdapterProvider.get(), (InputMethodAdapter) (((Provider) (singletonCImpl.androidInputMethodAdapterProvider)).get()), singletonCImpl.switchImeAsyncImplProvider.get(), singletonCImpl.androidPermissionAdapterProvider.get(), singletonCImpl.androidSystemFeatureAdapterProvider.get(), singletonCImpl.androidCameraAdapterProvider.get(), singletonCImpl.soundsManagerImplProvider.get(), singletonCImpl.androidRingtoneAdapterProvider.get(), singletonCImpl.provideBuildConfigProvider.get(), singletonCImpl.systemBridgeConnectionManagerImplProvider.get(), singletonCImpl.preferenceRepositoryImplProvider.get(), singletonCImpl.notificationReceiverAdapterImplProvider.get());

          case 65: // io.github.sds100.keymapper.base.constraints.GetConstraintErrorUseCaseImpl
          return (T) new GetConstraintErrorUseCaseImpl(singletonCImpl.androidPackageManagerAdapterProvider.get(), singletonCImpl.androidPermissionAdapterProvider.get(), singletonCImpl.androidSystemFeatureAdapterProvider.get(), (InputMethodAdapter) (((Provider) (singletonCImpl.androidInputMethodAdapterProvider)).get()), singletonCImpl.androidCameraAdapterProvider.get());

          case 66: // io.github.sds100.keymapper.base.keymaps.GetDefaultKeyMapOptionsUseCaseImpl
          return (T) new GetDefaultKeyMapOptionsUseCaseImpl(singletonCImpl.provideCoroutineScopeProvider.get(), singletonCImpl.preferenceRepositoryImplProvider.get());

          case 67: // io.github.sds100.keymapper.data.repositories.RoomFloatingLayoutRepository
          return (T) new RoomFloatingLayoutRepository(singletonCImpl.floatingLayoutDao(), singletonCImpl.provideCoroutineScopeProvider.get(), singletonCImpl.provideDispatchersProvider.get());

          case 68: // io.github.sds100.keymapper.base.keymaps.FingerprintGesturesSupportedUseCaseImpl
          return (T) new FingerprintGesturesSupportedUseCaseImpl(singletonCImpl.preferenceRepositoryImplProvider.get());

          case 69: // io.github.sds100.keymapper.data.repositories.RoomGroupRepository
          return (T) new RoomGroupRepository(singletonCImpl.groupDao(), singletonCImpl.provideCoroutineScopeProvider.get(), singletonCImpl.provideDispatchersProvider.get());

          case 70: // io.github.sds100.keymapper.base.backup.BackupManagerImpl
          return (T) new BackupManagerImpl(singletonCImpl.provideCoroutineScopeProvider.get(), singletonCImpl.androidFileAdapterProvider.get(), singletonCImpl.roomKeyMapRepositoryProvider.get(), singletonCImpl.preferenceRepositoryImplProvider.get(), singletonCImpl.roomFloatingLayoutRepositoryProvider.get(), singletonCImpl.roomFloatingButtonRepositoryProvider.get(), singletonCImpl.roomGroupRepositoryProvider.get(), singletonCImpl.soundsManagerImplProvider.get(), singletonCImpl.provideDispatchersProvider.get(), singletonCImpl.bindUuidGeneratorProvider.get(), singletonCImpl.provideBuildConfigProvider.get());

          case 71: // io.github.sds100.keymapper.common.utils.DefaultUuidGenerator
          return (T) new DefaultUuidGenerator();

          case 72: // io.github.sds100.keymapper.base.system.inputmethod.ShowInputMethodPickerUseCaseImpl
          return (T) new ShowInputMethodPickerUseCaseImpl((InputMethodAdapter) (((Provider) (singletonCImpl.androidInputMethodAdapterProvider)).get()), singletonCImpl.preferenceRepositoryImplProvider.get());

          case 73: // io.github.sds100.keymapper.base.actions.uielement.InteractUiElementController
          return (T) new InteractUiElementController(singletonCImpl.provideCoroutineScopeProvider.get(), singletonCImpl.accessibilityServiceAdapterImplProvider.get(), singletonCImpl.roomAccessibilityNodeRepositoryProvider.get(), singletonCImpl.androidPackageManagerAdapterProvider.get());

          case 74: // io.github.sds100.keymapper.data.repositories.RoomAccessibilityNodeRepository
          return (T) new RoomAccessibilityNodeRepository(singletonCImpl.provideCoroutineScopeProvider.get(), singletonCImpl.accessibilityNodeDao());

          case 75: // io.github.sds100.keymapper.system.intents.IntentAdapterImpl
          return (T) new IntentAdapterImpl(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 76: // io.github.sds100.keymapper.system.popup.AndroidToastAdapter
          return (T) new AndroidToastAdapter(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 77: // io.github.sds100.keymapper.system.volume.AndroidVolumeAdapter
          return (T) new AndroidVolumeAdapter(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.systemBridgeConnectionManagerImplProvider.get());

          case 78: // io.github.sds100.keymapper.system.lock.AndroidLockScreenAdapter
          return (T) new AndroidLockScreenAdapter(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 79: // io.github.sds100.keymapper.system.airplanemode.AndroidAirplaneModeAdapter
          return (T) new AndroidAirplaneModeAdapter(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.systemBridgeConnectionManagerImplProvider.get(), singletonCImpl.suAdapterImplProvider.get(), singletonCImpl.provideCoroutineScopeProvider.get());

          case 80: // io.github.sds100.keymapper.system.nfc.AndroidNfcAdapter
          return (T) new AndroidNfcAdapter(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.suAdapterImplProvider.get(), singletonCImpl.systemBridgeConnectionManagerImplProvider.get());

          case 81: // io.github.sds100.keymapper.system.url.AndroidOpenUrlAdapter
          return (T) new AndroidOpenUrlAdapter(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 82: // io.github.sds100.keymapper.system.vibrator.AndroidVibratorAdapter
          return (T) new AndroidVibratorAdapter(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 83: // io.github.sds100.keymapper.system.power.AndroidPowerAdapter
          return (T) new AndroidPowerAdapter(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 84: // io.github.sds100.keymapper.system.foldable.AndroidFoldableAdapter
          return (T) new AndroidFoldableAdapter(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
