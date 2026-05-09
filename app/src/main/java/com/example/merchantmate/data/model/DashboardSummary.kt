package com.example.merchantmate.data.model

data class DashboardSummary(
    val todayRevenue: Double = 0.0,
    val transactionCount: Int = 0,
    val averageOrderValue: Double = 0.0,
    val bestSellingProduct: BestSellingProduct? = null,
    val recentTransactions: List<RecentTransaction> = emptyList()
)

data class BestSellingProduct(
    val productId: String = "",
    val name: String = "",
    val quantitySold: Int = 0,
    val revenue: Double = 0.0
)

data class RecentTransaction(
    val id: String = "",
    val receiptId: String = "",
    val total: Double = 0.0,
    val paymentMethod: String = "",
    val status: String = "",
    val createdAt: FirestoreTimestamp? = null
)