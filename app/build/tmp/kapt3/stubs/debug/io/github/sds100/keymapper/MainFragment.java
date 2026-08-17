package io.github.sds100.keymapper;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.compose.foundation.layout.WindowInsetsSides;
import androidx.compose.material3.SnackbarHostState;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.platform.ComposeView;
import androidx.compose.ui.platform.ViewCompositionStrategy;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavGraphBuilder;
import androidx.navigation.NavHostController;
import androidx.navigation.compose.ComposeNavigator;
import androidx.navigation.compose.DialogNavigator;
import dagger.hilt.android.AndroidEntryPoint;
import io.github.sds100.keymapper.base.actions.ConfigActionsViewModel;
import io.github.sds100.keymapper.base.constraints.ConfigConstraintsViewModel;
import io.github.sds100.keymapper.base.keymaps.ConfigKeyMapViewModel;
import io.github.sds100.keymapper.base.onboarding.SetupAccessibilityServiceDelegateImpl;
import io.github.sds100.keymapper.base.utils.navigation.NavDestination;
import io.github.sds100.keymapper.base.utils.navigation.NavigationProviderImpl;
import io.github.sds100.keymapper.home.HomeViewModel;
import io.github.sds100.keymapper.trigger.ConfigTriggerViewModel;
import javax.inject.Inject;

@dagger.hilt.android.AndroidEntryPoint()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000R\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0012\u0010\u0013\u001a\u00020\u00142\b\u0010\u0015\u001a\u0004\u0018\u00010\u0016H\u0016J$\u0010\u0017\u001a\u00020\u00182\u0006\u0010\u0019\u001a\u00020\u001a2\b\u0010\u001b\u001a\u0004\u0018\u00010\u001c2\b\u0010\u0015\u001a\u0004\u0018\u00010\u0016H\u0016J\b\u0010\u001d\u001a\u00020\u0014H\u0016J\u0010\u0010\u001e\u001a\u00020\u00142\u0006\u0010\u001f\u001a\u00020\u0016H\u0016J\u001a\u0010 \u001a\u00020\u00142\u0006\u0010!\u001a\u00020\u00182\b\u0010\u0015\u001a\u0004\u0018\u00010\u0016H\u0016J\u0014\u0010\"\u001a\u00020\u0014*\u00020#2\u0006\u0010\u0005\u001a\u00020$H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001e\u0010\u0007\u001a\u00020\b8\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\t\u0010\n\"\u0004\b\u000b\u0010\fR\u001e\u0010\r\u001a\u00020\u000e8\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000f\u0010\u0010\"\u0004\b\u0011\u0010\u0012\u00a8\u0006%"}, d2 = {"Lio/github/sds100/keymapper/MainFragment;", "Landroidx/fragment/app/Fragment;", "()V", "composeView", "Landroidx/compose/ui/platform/ComposeView;", "navController", "Landroidx/navigation/NavHostController;", "navigationProvider", "Lio/github/sds100/keymapper/base/utils/navigation/NavigationProviderImpl;", "getNavigationProvider", "()Lio/github/sds100/keymapper/base/utils/navigation/NavigationProviderImpl;", "setNavigationProvider", "(Lio/github/sds100/keymapper/base/utils/navigation/NavigationProviderImpl;)V", "setupAccessibilityServiceDelegate", "Lio/github/sds100/keymapper/base/onboarding/SetupAccessibilityServiceDelegateImpl;", "getSetupAccessibilityServiceDelegate", "()Lio/github/sds100/keymapper/base/onboarding/SetupAccessibilityServiceDelegateImpl;", "setSetupAccessibilityServiceDelegate", "(Lio/github/sds100/keymapper/base/onboarding/SetupAccessibilityServiceDelegateImpl;)V", "onCreate", "", "savedInstanceState", "Landroid/os/Bundle;", "onCreateView", "Landroid/view/View;", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "onDestroyView", "onSaveInstanceState", "outState", "onViewCreated", "view", "composableDestinations", "Landroidx/navigation/NavGraphBuilder;", "Landroidx/navigation/NavController;", "app_debug"})
public final class MainFragment extends androidx.fragment.app.Fragment {
    @javax.inject.Inject()
    public io.github.sds100.keymapper.base.utils.navigation.NavigationProviderImpl navigationProvider;
    @javax.inject.Inject()
    public io.github.sds100.keymapper.base.onboarding.SetupAccessibilityServiceDelegateImpl setupAccessibilityServiceDelegate;
    private androidx.compose.ui.platform.ComposeView composeView;
    @org.jetbrains.annotations.Nullable()
    private androidx.navigation.NavHostController navController;
    
    public MainFragment() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final io.github.sds100.keymapper.base.utils.navigation.NavigationProviderImpl getNavigationProvider() {
        return null;
    }
    
    public final void setNavigationProvider(@org.jetbrains.annotations.NotNull()
    io.github.sds100.keymapper.base.utils.navigation.NavigationProviderImpl p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final io.github.sds100.keymapper.base.onboarding.SetupAccessibilityServiceDelegateImpl getSetupAccessibilityServiceDelegate() {
        return null;
    }
    
    public final void setSetupAccessibilityServiceDelegate(@org.jetbrains.annotations.NotNull()
    io.github.sds100.keymapper.base.onboarding.SetupAccessibilityServiceDelegateImpl p0) {
    }
    
    @java.lang.Override()
    public void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public android.view.View onCreateView(@org.jetbrains.annotations.NotNull()
    android.view.LayoutInflater inflater, @org.jetbrains.annotations.Nullable()
    android.view.ViewGroup container, @org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
        return null;
    }
    
    @java.lang.Override()
    public void onViewCreated(@org.jetbrains.annotations.NotNull()
    android.view.View view, @org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    @java.lang.Override()
    public void onSaveInstanceState(@org.jetbrains.annotations.NotNull()
    android.os.Bundle outState) {
    }
    
    @java.lang.Override()
    public void onDestroyView() {
    }
    
    private final void composableDestinations(androidx.navigation.NavGraphBuilder $this$composableDestinations, androidx.navigation.NavController navController) {
    }
}