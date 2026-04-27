package com.example.gethome.di

import android.content.Context
import androidx.room.Room
import com.example.gethome.data.local.AnalysisHistoryDao
import com.example.gethome.data.local.GetHomeDatabase
import com.example.gethome.data.remote.VertexApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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
