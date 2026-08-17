package io.github.sds100.keymapper;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import io.github.sds100.keymapper.common.BuildConfigProvider;
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
public final class AppHiltModule_ProvideBuildConfigProviderFactory implements Factory<BuildConfigProvider> {
  private final AppHiltModule module;

  public AppHiltModule_ProvideBuildConfigProviderFactory(AppHiltModule module) {
    this.module = module;
  }

  @Override
  public BuildConfigProvider get() {
    return provideBuildConfigProvider(module);
  }

  public static AppHiltModule_ProvideBuildConfigProviderFactory create(AppHiltModule module) {
    return new AppHiltModule_ProvideBuildConfigProviderFactory(module);
  }

  public static BuildConfigProvider provideBuildConfigProvider(AppHiltModule instance) {
    return Preconditions.checkNotNullFromProvides(instance.provideBuildConfigProvider());
  }
}
