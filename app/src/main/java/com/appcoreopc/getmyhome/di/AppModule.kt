package com.appcoreopc.getmyhome.di

import android.content.Context
import androidx.room.Room
import com.appcoreopc.getmyhome.data.const.API_BASE_URL
import com.appcoreopc.getmyhome.data.local.AnalysisHistoryDao
import com.appcoreopc.getmyhome.data.local.GetHomeDatabase
import com.appcoreopc.getmyhome.data.local.PropertySearchBackendApi
import com.appcoreopc.getmyhome.data.remote.VertexApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): GetHomeDatabase {
        return Room.databaseBuilder(
            context,
            GetHomeDatabase::class.java,
            "gethome_db"
        ).build()
    }

    @Provides
    fun provideAnalysisHistoryDao(database: GetHomeDatabase): AnalysisHistoryDao {
        return database.analysisHistoryDao()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun providePropertySearchBackendApi(client: OkHttpClient): PropertySearchBackendApi {
        return Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PropertySearchBackendApi::class.java)
    }

    @Provides
    @Singleton
    fun provideVertexApiService(): VertexApiService {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl("https://us-central1-aiplatform.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(VertexApiService::class.java)
    }
}
