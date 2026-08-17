package io.github.sds100.keymapper;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import io.github.sds100.keymapper.base.purchasing.PurchasingManager;
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
public final class AppHiltModule_ProvidePurchasingManagerFactory implements Factory<PurchasingManager> {
  private final AppHiltModule module;

  public AppHiltModule_ProvidePurchasingManagerFactory(AppHiltModule module) {
    this.module = module;
  }

  @Override
  public PurchasingManager get() {
    return providePurchasingManager(module);
  }

  public static AppHiltModule_ProvidePurchasingManagerFactory create(AppHiltModule module) {
    return new AppHiltModule_ProvidePurchasingManagerFactory(module);
  }

  public static PurchasingManager providePurchasingManager(AppHiltModule instance) {
    return Preconditions.checkNotNullFromProvides(instance.providePurchasingManager());
  }
}
