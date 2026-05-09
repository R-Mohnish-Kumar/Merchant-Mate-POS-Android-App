package com.example.merchantmate.data.model

data class Transaction(
    val id: String = "",
    val receiptId: String = "",
    val items: List<TransactionItem> = emptyList(),
    val subtotal: Double = 0.0,
    val total: Double = 0.0,
    val paymentMethod: String = "",
    val status: String = "",
    val merchantId: String = "",
    val createdAt: FirestoreTimestamp? = null,
    val updatedAt: FirestoreTimestamp? = null
)

data class TransactionItem(
    val productId: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0,
    val lineTotal: Double = 0.0
)