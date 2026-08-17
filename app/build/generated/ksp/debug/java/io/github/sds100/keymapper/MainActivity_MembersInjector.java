package io.github.sds100.keymapper;

import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import io.github.sds100.keymapper.base.BaseMainActivity_MembersInjector;
import io.github.sds100.keymapper.base.input.InputEventHubImpl;
import io.github.sds100.keymapper.base.keymaps.ConfigKeyMapStateImpl;
import io.github.sds100.keymapper.base.onboarding.OnboardingUseCase;
import io.github.sds100.keymapper.base.system.accessibility.AccessibilityServiceAdapterImpl;
import io.github.sds100.keymapper.base.utils.navigation.NavigationProvider;
import io.github.sds100.keymapper.base.utils.ui.DialogProvider;
import io.github.sds100.keymapper.base.utils.ui.ResourceProviderImpl;
import io.github.sds100.keymapper.common.BuildConfigProvider;
import io.github.sds100.keymapper.sysbridge.service.SystemBridgeSetupControllerImpl;
import io.github.sds100.keymapper.system.devices.AndroidDevicesAdapter;
import io.github.sds100.keymapper.system.network.AndroidNetworkAdapter;
import io.github.sds100.keymapper.system.notifications.NotificationReceiverAdapterImpl;
import io.github.sds100.keymapper.system.permissions.AndroidPermissionAdapter;
import io.github.sds100.keymapper.system.root.SuAdapterImpl;
import io.github.sds100.keymapper.system.shizuku.ShizukuAdapter;
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
public final class MainActivity_MembersInjector implements MembersInjector<MainActivity> {
  private final Provider<AndroidPermissionAdapter> permissionAdapterProvider;

  private final Provider<AccessibilityServiceAdapterImpl> serviceAdapterProvider;

  private final Provider<ResourceProviderImpl> resourceProvider;

  private final Provider<OnboardingUseCase> onboardingUseCaseProvider;

  private final Provider<NotificationReceiverAdapterImpl> notificationReceiverAdapterProvider;

  private final Provider<ShizukuAdapter> shizukuAdapterProvider;

  private final Provider<BuildConfigProvider> buildConfigProvider;

  private final Provider<SystemBridgeSetupControllerImpl> systemBridgeSetupControllerProvider;

  private final Provider<SuAdapterImpl> suAdapterProvider;

  private final Provider<AndroidDevicesAdapter> devicesAdapterProvider;

  private final Provider<AndroidNetworkAdapter> networkAdapterProvider;

  private final Provider<InputEventHubImpl> inputEventHubProvider;

  private final Provider<NavigationProvider> navigationProvider;

  private final Provider<ConfigKeyMapStateImpl> configKeyMapStateProvider;

  private final Provider<DialogProvider> dialogProvider;

  public MainActivity_MembersInjector(Provider<AndroidPermissionAdapter> permissionAdapterProvider,
      Provider<AccessibilityServiceAdapterImpl> serviceAdapterProvider,
      Provider<ResourceProviderImpl> resourceProvider,
      Provider<OnboardingUseCase> onboardingUseCaseProvider,
      Provider<NotificationReceiverAdapterImpl> notificationReceiverAdapterProvider,
      Provider<ShizukuAdapter> shizukuAdapterProvider,
      Provider<BuildConfigProvider> buildConfigProvider,
      Provider<SystemBridgeSetupControllerImpl> systemBridgeSetupControllerProvider,
      Provider<SuAdapterImpl> suAdapterProvider,
      Provider<AndroidDevicesAdapter> devicesAdapterProvider,
      Provider<AndroidNetworkAdapter> networkAdapterProvider,
      Provider<InputEventHubImpl> inputEventHubProvider,
      Provider<NavigationProvider> navigationProvider,
      Provider<ConfigKeyMapStateImpl> configKeyMapStateProvider,
      Provider<DialogProvider> dialogProvider) {
    this.permissionAdapterProvider = permissionAdapterProvider;
    this.serviceAdapterProvider = serviceAdapterProvider;
    this.resourceProvider = resourceProvider;
    this.onboardingUseCaseProvider = onboardingUseCaseProvider;
    this.notificationReceiverAdapterProvider = notificationReceiverAdapterProvider;
    this.shizukuAdapterProvider = shizukuAdapterProvider;
    this.buildConfigProvider = buildConfigProvider;
    this.systemBridgeSetupControllerProvider = systemBridgeSetupControllerProvider;
    this.suAdapterProvider = suAdapterProvider;
    this.devicesAdapterProvider = devicesAdapterProvider;
    this.networkAdapterProvider = networkAdapterProvider;
    this.inputEventHubProvider = inputEventHubProvider;
    this.navigationProvider = navigationProvider;
    this.configKeyMapStateProvider = configKeyMapStateProvider;
    this.dialogProvider = dialogProvider;
  }

  public static MembersInjector<MainActivity> create(
      Provider<AndroidPermissionAdapter> permissionAdapterProvider,
      Provider<AccessibilityServiceAdapterImpl> serviceAdapterProvider,
      Provider<ResourceProviderImpl> resourceProvider,
      Provider<OnboardingUseCase> onboardingUseCaseProvider,
      Provider<NotificationReceiverAdapterImpl> notificationReceiverAdapterProvider,
      Provider<ShizukuAdapter> shizukuAdapterProvider,
      Provider<BuildConfigProvider> buildConfigProvider,
      Provider<SystemBridgeSetupControllerImpl> systemBridgeSetupControllerProvider,
      Provider<SuAdapterImpl> suAdapterProvider,
      Provider<AndroidDevicesAdapter> devicesAdapterProvider,
      Provider<AndroidNetworkAdapter> networkAdapterProvider,
      Provider<InputEventHubImpl> inputEventHubProvider,
      Provider<NavigationProvider> navigationProvider,
      Provider<ConfigKeyMapStateImpl> configKeyMapStateProvider,
      Provider<DialogProvider> dialogProvider) {
    return new MainActivity_MembersInjector(permissionAdapterProvider, serviceAdapterProvider, resourceProvider, onboardingUseCaseProvider, notificationReceiverAdapterProvider, shizukuAdapterProvider, buildConfigProvider, systemBridgeSetupControllerProvider, suAdapterProvider, devicesAdapterProvider, networkAdapterProvider, inputEventHubProvider, navigationProvider, configKeyMapStateProvider, dialogProvider);
  }

  @Override
  public void injectMembers(MainActivity instance) {
    BaseMainActivity_MembersInjector.injectPermissionAdapter(instance, permissionAdapterProvider.get());
    BaseMainActivity_MembersInjector.injectServiceAdapter(instance, serviceAdapterProvider.get());
    BaseMainActivity_MembersInjector.injectResourceProvider(instance, resourceProvider.get());
    BaseMainActivity_MembersInjector.injectOnboardingUseCase(instance, onboardingUseCaseProvider.get());
    BaseMainActivity_MembersInjector.injectNotificationReceiverAdapter(instance, notificationReceiverAdapterProvider.get());
    BaseMainActivity_MembersInjector.injectShizukuAdapter(instance, shizukuAdapterProvider.get());
    BaseMainActivity_MembersInjector.injectBuildConfigProvider(instance, buildConfigProvider.get());
    BaseMainActivity_MembersInjector.injectSystemBridgeSetupController(instance, systemBridgeSetupControllerProvider.get());
    BaseMainActivity_MembersInjector.injectSuAdapter(instance, suAdapterProvider.get());
    BaseMainActivity_MembersInjector.injectDevicesAdapter(instance, devicesAdapterProvider.get());
    BaseMainActivity_MembersInjector.injectNetworkAdapter(instance, networkAdapterProvider.get());
    BaseMainActivity_MembersInjector.injectInputEventHub(instance, inputEventHubProvider.get());
    BaseMainActivity_MembersInjector.injectNavigationProvider(instance, navigationProvider.get());
    BaseMainActivity_MembersInjector.injectConfigKeyMapState(instance, configKeyMapStateProvider.get());
    injectDialogProvider(instance, dialogProvider.get());
  }

  @InjectedFieldSignature("io.github.sds100.keymapper.MainActivity.dialogProvider")
  public static void injectDialogProvider(MainActivity instance, DialogProvider dialogProvider) {
    instance.dialogProvider = dialogProvider;
  }
}
