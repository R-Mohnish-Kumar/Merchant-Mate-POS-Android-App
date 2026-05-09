package com.example.merchantmate.data.api

import com.example.merchantmate.data.model.ApiResponse
import com.example.merchantmate.data.model.CheckoutRequest
import com.example.merchantmate.data.model.DashboardSummary
import com.example.merchantmate.data.model.MerchantProfile
import com.example.merchantmate.data.model.Product
import com.example.merchantmate.data.model.ProductRequest
import com.example.merchantmate.data.model.ProfileRequest
import com.example.merchantmate.data.model.TodayInsights
import com.example.merchantmate.data.model.Transaction
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    @GET("api/profile")
    suspend fun getProfile(): Response<ApiResponse<MerchantProfile>>

    @PUT("api/profile")
    suspend fun updateProfile(
        @Body request: ProfileRequest
    ): Response<ApiResponse<MerchantProfile>>

    @GET("api/products")
    suspend fun getProducts(): Response<ApiResponse<List<Product>>>

    @POST("api/products")
    suspend fun addProduct(
        @Body productRequest: ProductRequest
    ): Response<ApiResponse<Product>>

    @PUT("api/products/{id}")
    suspend fun updateProduct(
        @Path("id") productId: String,
        @Body request: ProductRequest
    ): Response<ApiResponse<Product>>

    @DELETE("api/products/{id}")
    suspend fun deleteProduct(
        @Path("id") productId: String
    ): Response<ApiResponse<Unit>>

    @POST("api/checkout")
    suspend fun checkout(
        @Body checkoutRequest: CheckoutRequest
    ): Response<ApiResponse<Any>>

    @GET("api/dashboard/summary")
    suspend fun getDashboardSummary(): Response<ApiResponse<DashboardSummary>>

    @GET("api/insights/today")
    suspend fun getTodayInsights(): Response<ApiResponse<TodayInsights>>

    @GET("api/transactions")
    suspend fun getTransactions(): Response<ApiResponse<List<Transaction>>>
}