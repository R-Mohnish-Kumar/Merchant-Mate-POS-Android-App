package com.example.merchantmate.data.model

data class ProductRequest(
    val name: String,
    val category: String,
    val price: Double,
    val stock: Int
)
