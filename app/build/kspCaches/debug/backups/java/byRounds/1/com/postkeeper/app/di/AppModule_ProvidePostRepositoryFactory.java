package com.postkeeper.app.di;

import android.content.Context;
import com.postkeeper.app.data.dao.PostDao;
import com.postkeeper.app.data.repository.PostRepository;
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
public final class AppModule_ProvidePostRepositoryFactory implements Factory<PostRepository> {
  private final Provider<PostDao> postDaoProvider;

  private final Provider<Context> contextProvider;

  public AppModule_ProvidePostRepositoryFactory(Provider<PostDao> postDaoProvider,
      Provider<Context> contextProvider) {
    this.postDaoProvider = postDaoProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public PostRepository get() {
    return providePostRepository(postDaoProvider.get(), contextProvider.get());
  }

  public static AppModule_ProvidePostRepositoryFactory create(Provider<PostDao> postDaoProvider,
      Provider<Context> contextProvider) {
    return new AppModule_ProvidePostRepositoryFactory(postDaoProvider, contextProvider);
  }

  public static PostRepository providePostRepository(PostDao postDao, Context context) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.providePostRepository(postDao, context));
  }
}
