package com.example.merchantmate.data.model

data class CheckoutRequest(
    val items: List<CheckoutItem>,
    val paymentMethod: String
)

data class CheckoutItem(
    val productId: String,
    val name: String,
    val price: Double,
    val quantity: Int
)
