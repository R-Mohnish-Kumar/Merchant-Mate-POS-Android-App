package com.example.merchantmate.data.repository

import com.example.merchantmate.data.api.ApiService
import com.example.merchantmate.data.model.ApiResponse
import com.example.merchantmate.data.model.CheckoutRequest
import com.example.merchantmate.data.model.DashboardSummary
import com.example.merchantmate.data.model.MerchantProfile
import com.example.merchantmate.data.model.Product
import com.example.merchantmate.data.model.ProductRequest
import com.example.merchantmate.data.model.ProfileRequest
import com.example.merchantmate.data.model.TodayInsights
import com.example.merchantmate.data.model.Transaction
import com.example.merchantmate.data.model.WeeklySales

class ProductRepository(
    private val apiService: ApiService
) {

    suspend fun getProfile(): ApiResponse<MerchantProfile> {
        val response = apiService.getProfile()

        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }

        throw Exception(response.errorBody()?.string() ?: "Failed to load profile")
    }

    suspend fun updateProfile(request: ProfileRequest): ApiResponse<MerchantProfile> {
        val response = apiService.updateProfile(request)

        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }

        throw Exception(response.errorBody()?.string() ?: "Failed to update profile")
    }

    suspend fun getProducts(): ApiResponse<List<Product>> {
        val response = apiService.getProducts()

        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }

        throw Exception(response.errorBody()?.string() ?: "Failed to load products")
    }

    suspend fun addProduct(request: ProductRequest): ApiResponse<Product> {
        val response = apiService.addProduct(request)

        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }

        throw Exception(response.errorBody()?.string() ?: "Failed to add product")
    }

    suspend fun updateProduct(
        productId: String,
        request: ProductRequest
    ): ApiResponse<Product> {
        val response = apiService.updateProduct(productId, request)

        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }

        throw Exception(response.errorBody()?.string() ?: "Failed to update product")
    }

    suspend fun deleteProduct(productId: String): ApiResponse<Unit> {
        val response = apiService.deleteProduct(productId)

        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }

        throw Exception(response.errorBody()?.string() ?: "Failed to delete product")
    }

    suspend fun checkout(request: CheckoutRequest): ApiResponse<Any> {
        val response = apiService.checkout(request)

        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }

        throw Exception(response.errorBody()?.string() ?: "Checkout failed")
    }

    suspend fun getDashboardSummary(): ApiResponse<DashboardSummary> {
        val response = apiService.getDashboardSummary()

        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }

        throw Exception(response.errorBody()?.string() ?: "Failed to load dashboard summary")
    }

    suspend fun getTodayInsights(): ApiResponse<TodayInsights> {
        val response = apiService.getTodayInsights()

        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }

        throw Exception(response.errorBody()?.string() ?: "Failed to load today insights")
    }

    suspend fun getTransactions(): ApiResponse<List<Transaction>> {
        val response = apiService.getTransactions()

        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }

        throw Exception(response.errorBody()?.string() ?: "Failed to load transactions")
    }

    suspend fun getWeeklySales(): ApiResponse<List<WeeklySales>> {
        return apiService.getWeeklySales()
    }
}