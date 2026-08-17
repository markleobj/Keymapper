package io.github.sds100.keymapper;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import io.github.sds100.keymapper.common.utils.DispatcherProvider;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class AppHiltModule_ProvideDispatchersFactory implements Factory<DispatcherProvider> {
  private final AppHiltModule module;

  public AppHiltModule_ProvideDispatchersFactory(AppHiltModule module) {
    this.module = module;
  }

  @Override
  public DispatcherProvider get() {
    return provideDispatchers(module);
  }

  public static AppHiltModule_ProvideDispatchersFactory create(AppHiltModule module) {
    return new AppHiltModule_ProvideDispatchersFactory(module);
  }

  public static DispatcherProvider provideDispatchers(AppHiltModule instance) {
    return Preconditions.checkNotNullFromProvides(instance.provideDispatchers());
  }
}
