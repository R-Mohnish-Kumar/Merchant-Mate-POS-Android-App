package com.example.merchantmate.data.model

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T?
)