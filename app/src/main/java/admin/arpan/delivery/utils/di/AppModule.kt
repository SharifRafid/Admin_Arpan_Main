package admin.arpan.delivery.utils.di

import admin.arpan.delivery.utils.Preference
import admin.arpan.delivery.utils.networking.RetrofitBuilder
import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

  @Singleton
  @Provides
  fun providePreference(application: Application): Preference {
    return Preference(application)
  }

  @Singleton
  @Provides
  fun provideRetrofitBuilder(): RetrofitBuilder {
    return RetrofitBuilder()
  }

}