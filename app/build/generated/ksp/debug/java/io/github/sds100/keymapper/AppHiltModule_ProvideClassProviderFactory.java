package io.github.sds100.keymapper;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import io.github.sds100.keymapper.common.KeyMapperClassProvider;
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
public final class AppHiltModule_ProvideClassProviderFactory implements Factory<KeyMapperClassProvider> {
  private final AppHiltModule module;

  public AppHiltModule_ProvideClassProviderFactory(AppHiltModule module) {
    this.module = module;
  }

  @Override
  public KeyMapperClassProvider get() {
    return provideClassProvider(module);
  }

  public static AppHiltModule_ProvideClassProviderFactory create(AppHiltModule module) {
    return new AppHiltModule_ProvideClassProviderFactory(module);
  }

  public static KeyMapperClassProvider provideClassProvider(AppHiltModule instance) {
    return Preconditions.checkNotNullFromProvides(instance.provideClassProvider());
  }
}
