package com.nvv.mediadata.data.provide

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.internal.tls.OkHostnameVerifier
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BaseNetworkModuleProvider {
	@Provides
	@Singleton
	fun provideJson(): Json = Json {
		ignoreUnknownKeys = true
		isLenient = true
		coerceInputValues = true
	}

	@Provides
	@Singleton
	@Named("BaseOkHttpClient")
	fun provideOkHttpClient(): OkHttpClient {
		return OkHttpClient.Builder()
			.connectTimeout(60, TimeUnit.SECONDS)
			.readTimeout(60, TimeUnit.SECONDS)
			.writeTimeout(60, TimeUnit.SECONDS)
			.addInterceptor(
				Interceptor { chain ->
					val newRequest: Request = chain.request().newBuilder()
						.header("User-Agent", "MediaDataPlayer/App")
						.build()
					chain.proceed(newRequest)
				}
			)
			.retryOnConnectionFailure(true)
			.hostnameVerifier { hostname, session ->
				return@hostnameVerifier OkHostnameVerifier.verify(hostname, session)
			}
			.build()
	}
}