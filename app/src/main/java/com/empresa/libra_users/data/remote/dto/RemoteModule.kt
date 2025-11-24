package com.empresa.libra_users.data.remote.dto

import com.empresa.libra_users.data.remote.AuthInterceptor
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RemoteModule {

    // URLs base para cada servicio
    // Para emulador: http://10.0.2.2:PORT
    // Para dispositivo físico: http://192.168.1.X:PORT (configurable)
    private const val BASE_URL_USER_SERVICE = "http://10.0.2.2:8081/"
    private const val BASE_URL_BOOK_SERVICE = "http://10.0.2.2:8082/"
    private const val BASE_URL_LOAN_SERVICE = "http://10.0.2.2:8083/"
    private const val BASE_URL_NOTIFICATION_SERVICE = "http://10.0.2.2:8085/"
    private const val BASE_URL_REPORT_SERVICE = "http://10.0.2.2:8084/"

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor) // Agrega el token JWT automáticamente
            .addInterceptor(loggingInterceptor) // Logging de peticiones HTTP
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @Named("UserService")
    fun provideRetrofitUserService(
        gson: Gson,
        okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL_USER_SERVICE)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    @Named("BookService")
    fun provideRetrofitBookService(
        gson: Gson,
        okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL_BOOK_SERVICE)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    @Named("LoanService")
    fun provideRetrofitLoanService(
        gson: Gson,
        okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL_LOAN_SERVICE)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    @Named("NotificationService")
    fun provideRetrofitNotificationService(
        gson: Gson,
        okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL_NOTIFICATION_SERVICE)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    @Named("ReportService")
    fun provideRetrofitReportService(
        gson: Gson,
        okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL_REPORT_SERVICE)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideUserApi(@Named("UserService") retrofit: Retrofit): UserApi {
        return retrofit.create(UserApi::class.java)
    }

    @Provides
    @Singleton
    fun provideBookApi(@Named("BookService") retrofit: Retrofit): BookApi {
        return retrofit.create(BookApi::class.java)
    }

    @Provides
    @Singleton
    fun provideLoanApi(@Named("LoanService") retrofit: Retrofit): LoanApi {
        return retrofit.create(LoanApi::class.java)
    }

    @Provides
    @Singleton
    fun provideNotificationApi(@Named("NotificationService") retrofit: Retrofit): NotificationApi {
        return retrofit.create(NotificationApi::class.java)
    }

    @Provides
    @Singleton
    fun provideReportApi(@Named("ReportService") retrofit: Retrofit): ReportApi {
        return retrofit.create(ReportApi::class.java)
    }
}

