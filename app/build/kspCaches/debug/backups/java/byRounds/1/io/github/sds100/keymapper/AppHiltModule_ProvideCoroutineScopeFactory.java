package io.github.sds100.keymapper;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import kotlinx.coroutines.CoroutineScope;

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
public final class AppHiltModule_ProvideCoroutineScopeFactory implements Factory<CoroutineScope> {
  private final AppHiltModule module;

  public AppHiltModule_ProvideCoroutineScopeFactory(AppHiltModule module) {
    this.module = module;
  }

  @Override
  public CoroutineScope get() {
    return provideCoroutineScope(module);
  }

  public static AppHiltModule_ProvideCoroutineScopeFactory create(AppHiltModule module) {
    return new AppHiltModule_ProvideCoroutineScopeFactory(module);
  }

  public static CoroutineScope provideCoroutineScope(AppHiltModule instance) {
    return Preconditions.checkNotNullFromProvides(instance.provideCoroutineScope());
  }
}
