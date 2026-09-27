package com.postkeeper.app.di;

import com.postkeeper.app.util.TwitterExtractor;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
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
    "deprecation"
})
public final class AppModule_ProvideTwitterExtractorFactory implements Factory<TwitterExtractor> {
  @Override
  public TwitterExtractor get() {
    return provideTwitterExtractor();
  }

  public static AppModule_ProvideTwitterExtractorFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static TwitterExtractor provideTwitterExtractor() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideTwitterExtractor());
  }

  private static final class InstanceHolder {
    private static final AppModule_ProvideTwitterExtractorFactory INSTANCE = new AppModule_ProvideTwitterExtractorFactory();
  }
}
