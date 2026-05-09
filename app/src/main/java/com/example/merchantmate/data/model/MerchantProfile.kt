package com.example.merchantmate.data.model

data class MerchantProfile(
    val merchantId: String = "",
    val shopName: String = "",
    val ownerName: String = "",
    val email: String = "",
    val contactNumber: String = "",
    val shopAddress: String = "",
    val createdAt: FirestoreTimestamp? = null,
    val updatedAt: FirestoreTimestamp? = null
)
