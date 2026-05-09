package com.example.merchantmate.data.model

data class TodayInsights(
    val topSeller: TopSeller? = null,
    val lowStock: List<LowStockInsight> = emptyList(),
    val slowMoving: List<SlowMovingInsight> = emptyList(),
    val averageOrderValue: Double = 0.0,
    val revenueComparison: RevenueComparison? = null,
    val suggestedActions: List<String> = emptyList()
)

data class TopSeller(
    val productId: String = "",
    val productName: String = "",
    val quantitySold: Int = 0,
    val revenue: Double = 0.0,
    val message: String = ""
)

data class LowStockInsight(
    val productId: String = "",
    val productName: String = "",
    val stock: Int = 0,
    val message: String = ""
)

data class SlowMovingInsight(
    val productId: String = "",
    val productName: String = "",
    val stock: Int = 0,
    val message: String = ""
)

data class RevenueComparison(
    val todayRevenue: Double = 0.0,
    val yesterdayRevenue: Double = 0.0,
    val percentageChange: Double = 0.0,
    val message: String = ""
)