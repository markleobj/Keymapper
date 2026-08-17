package io.github.sds100.keymapper;

import dagger.Binds;
import dagger.Component;
import dagger.Module;
import dagger.Subcomponent;
import dagger.hilt.android.components.ActivityComponent;
import dagger.hilt.android.components.ActivityRetainedComponent;
import dagger.hilt.android.components.FragmentComponent;
import dagger.hilt.android.components.ServiceComponent;
import dagger.hilt.android.components.ViewComponent;
import dagger.hilt.android.components.ViewModelComponent;
import dagger.hilt.android.components.ViewWithFragmentComponent;
import dagger.hilt.android.flags.FragmentGetContextFix;
import dagger.hilt.android.flags.HiltWrapper_FragmentGetContextFix_FragmentGetContextFixModule;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.HiltViewModelFactory;
import dagger.hilt.android.internal.lifecycle.HiltWrapper_DefaultViewModelFactories_ActivityModule;
import dagger.hilt.android.internal.lifecycle.HiltWrapper_HiltViewModelFactory_ActivityCreatorEntryPoint;
import dagger.hilt.android.internal.lifecycle.HiltWrapper_HiltViewModelFactory_ViewModelModule;
import dagger.hilt.android.internal.managers.ActivityComponentManager;
import dagger.hilt.android.internal.managers.FragmentComponentManager;
import dagger.hilt.android.internal.managers.HiltWrapper_ActivityRetainedComponentManager_ActivityRetainedComponentBuilderEntryPoint;
import dagger.hilt.android.internal.managers.HiltWrapper_ActivityRetainedComponentManager_ActivityRetainedLifecycleEntryPoint;
import dagger.hilt.android.internal.managers.HiltWrapper_ActivityRetainedComponentManager_LifecycleModule;
import dagger.hilt.android.internal.managers.HiltWrapper_SavedStateHandleModule;
import dagger.hilt.android.internal.managers.ServiceComponentManager;
import dagger.hilt.android.internal.managers.ViewComponentManager;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.HiltWrapper_ActivityModule;
import dagger.hilt.android.scopes.ActivityRetainedScoped;
import dagger.hilt.android.scopes.ActivityScoped;
import dagger.hilt.android.scopes.FragmentScoped;
import dagger.hilt.android.scopes.ServiceScoped;
import dagger.hilt.android.scopes.ViewModelScoped;
import dagger.hilt.android.scopes.ViewScoped;
import dagger.hilt.components.SingletonComponent;
import dagger.hilt.internal.GeneratedComponent;
import dagger.hilt.migration.DisableInstallInCheck;
import io.github.sds100.keymapper.api.ApiHiltModule;
import io.github.sds100.keymapper.api.EnableKeyMapsBroadcastReceiver_GeneratedInjector;
import io.github.sds100.keymapper.api.LaunchKeyMapShortcutActivity_GeneratedInjector;
import io.github.sds100.keymapper.api.PauseMappingsBroadcastReceiver_GeneratedInjector;
import io.github.sds100.keymapper.api.TriggerKeyMapsBroadcastReceiver_GeneratedInjector;
import io.github.sds100.keymapper.base.ActivityViewModel_HiltModules;
import io.github.sds100.keymapper.base.BaseSingletonHiltModule;
import io.github.sds100.keymapper.base.BaseViewModelHiltModule;
import io.github.sds100.keymapper.base.ViewModelModule;
import io.github.sds100.keymapper.base.about.AboutFragment_GeneratedInjector;
import io.github.sds100.keymapper.base.actions.ChooseActionViewModel_HiltModules;
import io.github.sds100.keymapper.base.actions.ChooseSettingViewModel_HiltModules;
import io.github.sds100.keymapper.base.actions.ConfigActionsViewModel_HiltModules;
import io.github.sds100.keymapper.base.actions.ConfigShellCommandViewModel_HiltModules;
import io.github.sds100.keymapper.base.actions.keyevent.ChooseKeyCodeFragment_GeneratedInjector;
import io.github.sds100.keymapper.base.actions.keyevent.ChooseKeyCodeViewModel_HiltModules;
import io.github.sds100.keymapper.base.actions.keyevent.ConfigKeyEventActionFragment_GeneratedInjector;
import io.github.sds100.keymapper.base.actions.keyevent.ConfigKeyEventActionViewModel_HiltModules;
import io.github.sds100.keymapper.base.actions.pinchscreen.PinchPickDisplayCoordinateFragment_GeneratedInjector;
import io.github.sds100.keymapper.base.actions.pinchscreen.PinchPickDisplayCoordinateViewModel_HiltModules;
import io.github.sds100.keymapper.base.actions.sound.ChooseSoundFileFragment_GeneratedInjector;
import io.github.sds100.keymapper.base.actions.sound.ChooseSoundFileViewModel_HiltModules;
import io.github.sds100.keymapper.base.actions.swipescreen.SwipePickDisplayCoordinateFragment_GeneratedInjector;
import io.github.sds100.keymapper.base.actions.swipescreen.SwipePickDisplayCoordinateViewModel_HiltModules;
import io.github.sds100.keymapper.base.actions.tapscreen.PickDisplayCoordinateFragment_GeneratedInjector;
import io.github.sds100.keymapper.base.actions.tapscreen.PickDisplayCoordinateViewModel_HiltModules;
import io.github.sds100.keymapper.base.actions.uielement.InteractUiElementViewModel_HiltModules;
import io.github.sds100.keymapper.base.backup.RestoreKeyMapsActivity_GeneratedInjector;
import io.github.sds100.keymapper.base.backup.RestoreKeyMapsViewModel_HiltModules;
import io.github.sds100.keymapper.base.constraints.ChooseConstraintViewModel_HiltModules;
import io.github.sds100.keymapper.base.constraints.ConfigConstraintsViewModel_HiltModules;
import io.github.sds100.keymapper.base.debug.GetEventViewModel_HiltModules;
import io.github.sds100.keymapper.base.expertmode.ExpertModeSetupViewModel_HiltModules;
import io.github.sds100.keymapper.base.expertmode.ExpertModeViewModel_HiltModules;
import io.github.sds100.keymapper.base.keymaps.ConfigKeyMapViewModel_HiltModules;
import io.github.sds100.keymapper.base.logging.LogViewModel_HiltModules;
import io.github.sds100.keymapper.base.settings.SettingsViewModel_HiltModules;
import io.github.sds100.keymapper.base.shortcuts.CreateKeyMapShortcutActivity_GeneratedInjector;
import io.github.sds100.keymapper.base.shortcuts.CreateKeyMapShortcutViewModel_HiltModules;
import io.github.sds100.keymapper.base.system.accessibility.BaseAccessibilityService_GeneratedInjector;
import io.github.sds100.keymapper.base.system.apps.ChooseActivityFragment_GeneratedInjector;
import io.github.sds100.keymapper.base.system.apps.ChooseActivityViewModel_HiltModules;
import io.github.sds100.keymapper.base.system.apps.ChooseAppFragment_GeneratedInjector;
import io.github.sds100.keymapper.base.system.apps.ChooseAppShortcutFragment_GeneratedInjector;
import io.github.sds100.keymapper.base.system.apps.ChooseAppShortcutViewModel_HiltModules;
import io.github.sds100.keymapper.base.system.apps.ChooseAppViewModel_HiltModules;
import io.github.sds100.keymapper.base.system.bluetooth.ChooseBluetoothDeviceFragment_GeneratedInjector;
import io.github.sds100.keymapper.base.system.bluetooth.ChooseBluetoothDeviceViewModel_HiltModules;
import io.github.sds100.keymapper.base.system.intents.ConfigIntentFragment_GeneratedInjector;
import io.github.sds100.keymapper.base.system.intents.ConfigIntentViewModel_HiltModules;
import io.github.sds100.keymapper.data.DataHiltModule;
import io.github.sds100.keymapper.data.db.AppDatabaseModule;
import io.github.sds100.keymapper.home.HomeViewModel_HiltModules;
import io.github.sds100.keymapper.sysbridge.SystemBridgeHiltModule;
import io.github.sds100.keymapper.sysbridge.provider.SystemBridgeBinderProvider;
import io.github.sds100.keymapper.system.SystemHiltModule;
import io.github.sds100.keymapper.system.accessibility.MyAccessibilityService_GeneratedInjector;
import io.github.sds100.keymapper.system.accessibility.ObserveEnabledAccessibilityServicesJob_GeneratedInjector;
import io.github.sds100.keymapper.system.bluetooth.BluetoothBroadcastReceiver_GeneratedInjector;
import io.github.sds100.keymapper.system.inputmethod.KeyMapperImeService_GeneratedInjector;
import io.github.sds100.keymapper.system.inputmethod.ObserveInputMethodsJob_GeneratedInjector;
import io.github.sds100.keymapper.system.notifications.NotificationReceiver_GeneratedInjector;
import io.github.sds100.keymapper.system.notifications.ObserveNotificationListenersJob_GeneratedInjector;
import io.github.sds100.keymapper.tiles.ToggleKeyMapperKeyboardTile_GeneratedInjector;
import io.github.sds100.keymapper.tiles.ToggleMappingsTile_GeneratedInjector;
import io.github.sds100.keymapper.trigger.ConfigTriggerViewModel_HiltModules;
import javax.annotation.processing.Generated;
import javax.inject.Singleton;

@Generated("dagger.hilt.processor.internal.root.RootProcessor")
public final class KeyMapperApp_HiltComponents {
  private KeyMapperApp_HiltComponents() {
  }

  @Module(
      subcomponents = ServiceC.class
  )
  @DisableInstallInCheck
  @Generated("dagger.hilt.processor.internal.root.RootProcessor")
  abstract interface ServiceCBuilderModule {
    @Binds
    ServiceComponentBuilder bind(ServiceC.Builder builder);
  }

  @Module(
      subcomponents = ActivityRetainedC.class
  )
  @DisableInstallInCheck
  @Generated("dagger.hilt.processor.internal.root.RootProcessor")
  abstract interface ActivityRetainedCBuilderModule {
    @Binds
    ActivityRetainedComponentBuilder bind(ActivityRetainedC.Builder builder);
  }

  @Module(
      subcomponents = ActivityC.class
  )
  @DisableInstallInCheck
  @Generated("dagger.hilt.processor.internal.root.RootProcessor")
  abstract interface ActivityCBuilderModule {
    @Binds
    ActivityComponentBuilder bind(ActivityC.Builder builder);
  }

  @Module(
      subcomponents = ViewModelC.class
  )
  @DisableInstallInCheck
  @Generated("dagger.hilt.processor.internal.root.RootProcessor")
  abstract interface ViewModelCBuilderModule {
    @Binds
    ViewModelComponentBuilder bind(ViewModelC.Builder builder);
  }

  @Module(
      subcomponents = ViewC.class
  )
  @DisableInstallInCheck
  @Generated("dagger.hilt.processor.internal.root.RootProcessor")
  abstract interface ViewCBuilderModule {
    @Binds
    ViewComponentBuilder bind(ViewC.Builder builder);
  }

  @Module(
      subcomponents = FragmentC.class
  )
  @DisableInstallInCheck
  @Generated("dagger.hilt.processor.internal.root.RootProcessor")
  abstract interface FragmentCBuilderModule {
    @Binds
    FragmentComponentBuilder bind(FragmentC.Builder builder);
  }

  @Module(
      subcomponents = ViewWithFragmentC.class
  )
  @DisableInstallInCheck
  @Generated("dagger.hilt.processor.internal.root.RootProcessor")
  abstract interface ViewWithFragmentCBuilderModule {
    @Binds
    ViewWithFragmentComponentBuilder bind(ViewWithFragmentC.Builder builder);
  }

  @Component(
      modules = {
          ApiHiltModule.class,
          AppDatabaseModule.class,
          AppHiltModule.class,
          ApplicationContextModule.class,
          BaseSingletonHiltModule.class,
          DataHiltModule.class,
          HiltWrapper_FragmentGetContextFix_FragmentGetContextFixModule.class,
          ActivityRetainedCBuilderModule.class,
          ServiceCBuilderModule.class,
          SystemBridgeHiltModule.class,
          SystemHiltModule.class
      }
  )
  @Singleton
  public abstract static class SingletonC implements FragmentGetContextFix.FragmentGetContextFixEntryPoint,
      HiltWrapper_ActivityRetainedComponentManager_ActivityRetainedComponentBuilderEntryPoint,
      ServiceComponentManager.ServiceComponentBuilderEntryPoint,
      SingletonComponent,
      GeneratedComponent,
      KeyMapperApp_GeneratedInjector,
      EnableKeyMapsBroadcastReceiver_GeneratedInjector,
      PauseMappingsBroadcastReceiver_GeneratedInjector,
      TriggerKeyMapsBroadcastReceiver_GeneratedInjector,
      SystemBridgeBinderProvider.SystemBridgeProviderEntryPoint,
      BluetoothBroadcastReceiver_GeneratedInjector {
  }

  @Subcomponent
  @ServiceScoped
  public abstract static class ServiceC implements ServiceComponent,
      GeneratedComponent,
      BaseAccessibilityService_GeneratedInjector,
      MyAccessibilityService_GeneratedInjector,
      ObserveEnabledAccessibilityServicesJob_GeneratedInjector,
      KeyMapperImeService_GeneratedInjector,
      ObserveInputMethodsJob_GeneratedInjector,
      NotificationReceiver_GeneratedInjector,
      ObserveNotificationListenersJob_GeneratedInjector,
      ToggleKeyMapperKeyboardTile_GeneratedInjector,
      ToggleMappingsTile_GeneratedInjector {
    @Subcomponent.Builder
    abstract interface Builder extends ServiceComponentBuilder {
    }
  }

  @Subcomponent(
      modules = {
          ActivityViewModel_HiltModules.KeyModule.class,
          ChooseActionViewModel_HiltModules.KeyModule.class,
          ChooseActivityViewModel_HiltModules.KeyModule.class,
          ChooseAppShortcutViewModel_HiltModules.KeyModule.class,
          ChooseAppViewModel_HiltModules.KeyModule.class,
          ChooseBluetoothDeviceViewModel_HiltModules.KeyModule.class,
          ChooseConstraintViewModel_HiltModules.KeyModule.class,
          ChooseKeyCodeViewModel_HiltModules.KeyModule.class,
          ChooseSettingViewModel_HiltModules.KeyModule.class,
          ChooseSoundFileViewModel_HiltModules.KeyModule.class,
          ConfigActionsViewModel_HiltModules.KeyModule.class,
          ConfigConstraintsViewModel_HiltModules.KeyModule.class,
          ConfigIntentViewModel_HiltModules.KeyModule.class,
          ConfigKeyEventActionViewModel_HiltModules.KeyModule.class,
          ConfigKeyMapViewModel_HiltModules.KeyModule.class,
          ConfigShellCommandViewModel_HiltModules.KeyModule.class,
          ConfigTriggerViewModel_HiltModules.KeyModule.class,
          CreateKeyMapShortcutViewModel_HiltModules.KeyModule.class,
          ExpertModeSetupViewModel_HiltModules.KeyModule.class,
          ExpertModeViewModel_HiltModules.KeyModule.class,
          GetEventViewModel_HiltModules.KeyModule.class,
          HiltWrapper_ActivityRetainedComponentManager_LifecycleModule.class,
          HiltWrapper_SavedStateHandleModule.class,
          HomeViewModel_HiltModules.KeyModule.class,
          InteractUiElementViewModel_HiltModules.KeyModule.class,
          ActivityCBuilderModule.class,
          ViewModelCBuilderModule.class,
          LogViewModel_HiltModules.KeyModule.class,
          PickDisplayCoordinateViewModel_HiltModules.KeyModule.class,
          PinchPickDisplayCoordinateViewModel_HiltModules.KeyModule.class,
          RestoreKeyMapsViewModel_HiltModules.KeyModule.class,
          SettingsViewModel_HiltModules.KeyModule.class,
          SwipePickDisplayCoordinateViewModel_HiltModules.KeyModule.class
      }
  )
  @ActivityRetainedScoped
  public abstract static class ActivityRetainedC implements ActivityRetainedComponent,
      ActivityComponentManager.ActivityComponentBuilderEntryPoint,
      HiltWrapper_ActivityRetainedComponentManager_ActivityRetainedLifecycleEntryPoint,
      GeneratedComponent {
    @Subcomponent.Builder
    abstract interface Builder extends ActivityRetainedComponentBuilder {
    }
  }

  @Subcomponent(
      modules = {
          HiltWrapper_ActivityModule.class,
          HiltWrapper_DefaultViewModelFactories_ActivityModule.class,
          FragmentCBuilderModule.class,
          ViewCBuilderModule.class
      }
  )
  @ActivityScoped
  public abstract static class ActivityC implements ActivityComponent,
      DefaultViewModelFactories.ActivityEntryPoint,
      HiltWrapper_HiltViewModelFactory_ActivityCreatorEntryPoint,
      FragmentComponentManager.FragmentComponentBuilderEntryPoint,
      ViewComponentManager.ViewComponentBuilderEntryPoint,
      GeneratedComponent,
      MainActivity_GeneratedInjector,
      LaunchKeyMapShortcutActivity_GeneratedInjector,
      RestoreKeyMapsActivity_GeneratedInjector,
      CreateKeyMapShortcutActivity_GeneratedInjector {
    @Subcomponent.Builder
    abstract interface Builder extends ActivityComponentBuilder {
    }
  }

  @Subcomponent(
      modules = {
          ActivityViewModel_HiltModules.BindsModule.class,
          BaseViewModelHiltModule.class,
          ChooseActionViewModel_HiltModules.BindsModule.class,
          ChooseActivityViewModel_HiltModules.BindsModule.class,
          ChooseAppShortcutViewModel_HiltModules.BindsModule.class,
          ChooseAppViewModel_HiltModules.BindsModule.class,
          ChooseBluetoothDeviceViewModel_HiltModules.BindsModule.class,
          ChooseConstraintViewModel_HiltModules.BindsModule.class,
          ChooseKeyCodeViewModel_HiltModules.BindsModule.class,
          ChooseSettingViewModel_HiltModules.BindsModule.class,
          ChooseSoundFileViewModel_HiltModules.BindsModule.class,
          ConfigActionsViewModel_HiltModules.BindsModule.class,
          ConfigConstraintsViewModel_HiltModules.BindsModule.class,
          ConfigIntentViewModel_HiltModules.BindsModule.class,
          ConfigKeyEventActionViewModel_HiltModules.BindsModule.class,
          ConfigKeyMapViewModel_HiltModules.BindsModule.class,
          ConfigShellCommandViewModel_HiltModules.BindsModule.class,
          ConfigTriggerViewModel_HiltModules.BindsModule.class,
          CreateKeyMapShortcutViewModel_HiltModules.BindsModule.class,
          ExpertModeSetupViewModel_HiltModules.BindsModule.class,
          ExpertModeViewModel_HiltModules.BindsModule.class,
          GetEventViewModel_HiltModules.BindsModule.class,
          HiltWrapper_HiltViewModelFactory_ViewModelModule.class,
          HomeViewModel_HiltModules.BindsModule.class,
          InteractUiElementViewModel_HiltModules.BindsModule.class,
          LogViewModel_HiltModules.BindsModule.class,
          PickDisplayCoordinateViewModel_HiltModules.BindsModule.class,
          PinchPickDisplayCoordinateViewModel_HiltModules.BindsModule.class,
          RestoreKeyMapsViewModel_HiltModules.BindsModule.class,
          SettingsViewModel_HiltModules.BindsModule.class,
          SwipePickDisplayCoordinateViewModel_HiltModules.BindsModule.class,
          ViewModelModule.class
      }
  )
  @ViewModelScoped
  public abstract static class ViewModelC implements ViewModelComponent,
      HiltViewModelFactory.ViewModelFactoriesEntryPoint,
      GeneratedComponent {
    @Subcomponent.Builder
    abstract interface Builder extends ViewModelComponentBuilder {
    }
  }

  @Subcomponent
  @ViewScoped
  public abstract static class ViewC implements ViewComponent,
      GeneratedComponent {
    @Subcomponent.Builder
    abstract interface Builder extends ViewComponentBuilder {
    }
  }

  @Subcomponent(
      modules = ViewWithFragmentCBuilderModule.class
  )
  @FragmentScoped
  public abstract static class FragmentC implements FragmentComponent,
      DefaultViewModelFactories.FragmentEntryPoint,
      ViewComponentManager.ViewWithFragmentComponentBuilderEntryPoint,
      GeneratedComponent,
      MainFragment_GeneratedInjector,
      AboutFragment_GeneratedInjector,
      ChooseKeyCodeFragment_GeneratedInjector,
      ConfigKeyEventActionFragment_GeneratedInjector,
      PinchPickDisplayCoordinateFragment_GeneratedInjector,
      ChooseSoundFileFragment_GeneratedInjector,
      SwipePickDisplayCoordinateFragment_GeneratedInjector,
      PickDisplayCoordinateFragment_GeneratedInjector,
      ChooseActivityFragment_GeneratedInjector,
      ChooseAppFragment_GeneratedInjector,
      ChooseAppShortcutFragment_GeneratedInjector,
      ChooseBluetoothDeviceFragment_GeneratedInjector,
      ConfigIntentFragment_GeneratedInjector {
    @Subcomponent.Builder
    abstract interface Builder extends FragmentComponentBuilder {
    }
  }

  @Subcomponent
  @ViewScoped
  public abstract static class ViewWithFragmentC implements ViewWithFragmentComponent,
      GeneratedComponent {
    @Subcomponent.Builder
    abstract interface Builder extends ViewWithFragmentComponentBuilder {
    }
  }
}
