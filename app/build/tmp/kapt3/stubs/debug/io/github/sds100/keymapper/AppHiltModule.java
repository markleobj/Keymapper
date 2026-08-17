package io.github.sds100.keymapper;

import android.os.Build;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import io.github.sds100.keymapper.base.purchasing.PurchasingManager;
import io.github.sds100.keymapper.common.BuildConfigProvider;
import io.github.sds100.keymapper.common.KeyMapperClassProvider;
import io.github.sds100.keymapper.common.utils.DefaultDispatcherProvider;
import io.github.sds100.keymapper.common.utils.DispatcherProvider;
import io.github.sds100.keymapper.purchasing.PurchasingManagerImpl;
import io.github.sds100.keymapper.system.accessibility.MyAccessibilityService;
import javax.inject.Singleton;

@dagger.Module()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H\u0007J\b\u0010\u0005\u001a\u00020\u0006H\u0007J\b\u0010\u0007\u001a\u00020\bH\u0007J\b\u0010\t\u001a\u00020\nH\u0007J\b\u0010\u000b\u001a\u00020\fH\u0007\u00a8\u0006\r"}, d2 = {"Lio/github/sds100/keymapper/AppHiltModule;", "", "()V", "provideBuildConfigProvider", "Lio/github/sds100/keymapper/common/BuildConfigProvider;", "provideClassProvider", "Lio/github/sds100/keymapper/common/KeyMapperClassProvider;", "provideCoroutineScope", "Lkotlinx/coroutines/CoroutineScope;", "provideDispatchers", "Lio/github/sds100/keymapper/common/utils/DispatcherProvider;", "providePurchasingManager", "Lio/github/sds100/keymapper/base/purchasing/PurchasingManager;", "app_debug"})
@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
public final class AppHiltModule {
    
    public AppHiltModule() {
        super();
    }
    
    @javax.inject.Singleton()
    @dagger.Provides()
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.CoroutineScope provideCoroutineScope() {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final io.github.sds100.keymapper.common.utils.DispatcherProvider provideDispatchers() {
        return null;
    }
    
    @javax.inject.Singleton()
    @dagger.Provides()
    @org.jetbrains.annotations.NotNull()
    public final io.github.sds100.keymapper.common.BuildConfigProvider provideBuildConfigProvider() {
        return null;
    }
    
    @javax.inject.Singleton()
    @dagger.Provides()
    @org.jetbrains.annotations.NotNull()
    public final io.github.sds100.keymapper.common.KeyMapperClassProvider provideClassProvider() {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final io.github.sds100.keymapper.base.purchasing.PurchasingManager providePurchasingManager() {
        return null;
    }
}