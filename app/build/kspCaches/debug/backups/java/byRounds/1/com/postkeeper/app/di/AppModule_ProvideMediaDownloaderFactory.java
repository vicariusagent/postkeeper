package com.postkeeper.app.di;

import android.content.Context;
import com.postkeeper.app.util.MediaDownloader;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class AppModule_ProvideMediaDownloaderFactory implements Factory<MediaDownloader> {
  private final Provider<Context> contextProvider;

  public AppModule_ProvideMediaDownloaderFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public MediaDownloader get() {
    return provideMediaDownloader(contextProvider.get());
  }

  public static AppModule_ProvideMediaDownloaderFactory create(Provider<Context> contextProvider) {
    return new AppModule_ProvideMediaDownloaderFactory(contextProvider);
  }

  public static MediaDownloader provideMediaDownloader(Context context) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideMediaDownloader(context));
  }
}
