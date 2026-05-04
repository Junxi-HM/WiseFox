package com.example.wisefox.network

import com.example.wisefox.utils.SessionManager
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8080/"
    // private const val BASE_URL = "http://192.168.1.147:8080/"
    // Rutas públicas que NO deben llevar JWT
    private val PUBLIC_PATHS = setOf(
        "api/auth/login",
        "api/auth/register",
        "api/auth/google",
        "api/auth/verify-code",
        "api/auth/register/google"
    )

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // ── JWT interceptor — solo rutas protegidas ───────────────────────────────
    private val authInterceptor = okhttp3.Interceptor { chain ->
        val request = chain.request()
        val path = request.url.encodedPath.trimStart('/')

        // Si la ruta es pública, no añadir token
        val isPublic = PUBLIC_PATHS.any { path.startsWith(it) }

        val finalRequest = if (!isPublic) {
            val token = SessionManager.getToken()
            if (token != null) {
                request.newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            } else request
        } else request

        chain.proceed(finalRequest)
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    // ── Gson con soporte para LocalDate ──────────────────────────────────────
    private val gson = GsonBuilder()
        .registerTypeAdapter(
            LocalDate::class.java,
            JsonDeserializer { json, _, _ -> LocalDate.parse(json.asString) }
        )
        .create()

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}