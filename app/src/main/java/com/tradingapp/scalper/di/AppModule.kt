package com.tradingapp.scalper.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.tradingapp.scalper.data.api.DeltaApiService
import com.tradingapp.scalper.data.repository.TradingRepository
import com.tradingapp.scalper.data.websocket.DeltaWebSocketService
import com.tradingapp.scalper.domain.repository.ITradingRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val BASE_URL = "https://api.delta.exchange/"

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }

    private fun getUnsafeTrustManager(): X509TrustManager {
        return object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return try {
            val trustManager = getUnsafeTrustManager()
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, arrayOf<TrustManager>(trustManager), SecureRandom())

            OkHttpClient.Builder()
                .sslSocketFactory(sslContext.socketFactory, trustManager)
                .hostnameVerifier { _, _ -> true }
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to default client
            OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build()
        }
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideDeltaApiService(retrofit: Retrofit): DeltaApiService {
        return retrofit.create(DeltaApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideDeltaWebSocketService(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): DeltaWebSocketService {
        return DeltaWebSocketService(okHttpClient, gson)
    }

    @Provides
    @Singleton
    fun provideTradingRepository(
        webSocketService: DeltaWebSocketService,
        apiService: DeltaApiService
    ): ITradingRepository {
        return TradingRepository(webSocketService, apiService)
    }
}
