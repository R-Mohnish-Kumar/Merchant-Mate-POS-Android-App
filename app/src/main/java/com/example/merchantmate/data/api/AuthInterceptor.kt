package com.example.merchantmate.data.api

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser == null) {
            return chain.proceed(originalRequest)
        }

        val tokenResult = Tasks.await(currentUser.getIdToken(false))
        val token = tokenResult.token

        val authenticatedRequest = originalRequest.newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()

        return chain.proceed(authenticatedRequest)
    }
}