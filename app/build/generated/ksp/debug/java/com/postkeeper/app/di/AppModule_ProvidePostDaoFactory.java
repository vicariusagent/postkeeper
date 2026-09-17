package com.postkeeper.app.di;

import com.postkeeper.app.data.AppDatabase;
import com.postkeeper.app.data.dao.PostDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class AppModule_ProvidePostDaoFactory implements Factory<PostDao> {
  private final Provider<AppDatabase> databaseProvider;

  public AppModule_ProvidePostDaoFactory(Provider<AppDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public PostDao get() {
    return providePostDao(databaseProvider.get());
  }

  public static AppModule_ProvidePostDaoFactory create(Provider<AppDatabase> databaseProvider) {
    return new AppModule_ProvidePostDaoFactory(databaseProvider);
  }

  public static PostDao providePostDao(AppDatabase database) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.providePostDao(database));
  }
}
