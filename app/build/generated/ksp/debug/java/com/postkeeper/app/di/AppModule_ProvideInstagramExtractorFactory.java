package com.postkeeper.app.di;

import com.postkeeper.app.util.InstagramExtractor;
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
public final class AppModule_ProvideInstagramExtractorFactory implements Factory<InstagramExtractor> {
  @Override
  public InstagramExtractor get() {
    return provideInstagramExtractor();
  }

  public static AppModule_ProvideInstagramExtractorFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static InstagramExtractor provideInstagramExtractor() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideInstagramExtractor());
  }

  private static final class InstanceHolder {
    private static final AppModule_ProvideInstagramExtractorFactory INSTANCE = new AppModule_ProvideInstagramExtractorFactory();
  }
}
