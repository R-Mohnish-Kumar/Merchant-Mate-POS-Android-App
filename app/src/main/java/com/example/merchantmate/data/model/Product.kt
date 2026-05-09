package com.example.merchantmate.data.model
data class Product(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val price: Double = 0.0,
    val stock: Int = 0,
    val createdAt: FirestoreTimestamp? = null,
    val updatedAt: FirestoreTimestamp? = null
)

data class FirestoreTimestamp(
    val _seconds: Long = 0,
    val _nanoseconds: Int = 0
)
