package com.example.merchantmate.ui.products

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlin.text.get
import com.example.merchantmate.data.model.CheckoutItem
import com.example.merchantmate.data.model.Product

object CartManager {

    private val _cartItems = MutableLiveData<Map<String, CheckoutItem>>(emptyMap())
    val cartItems: LiveData<Map<String, CheckoutItem>> = _cartItems

    fun addToCart(product: Product): String? {
        if (product.id.isBlank()) {
            return "Invalid product"
        }

        if (product.stock <= 0) {
            return "${product.name} is out of stock"
        }

        val currentCart = _cartItems.value?.toMutableMap() ?: mutableMapOf()
        val existingItem = currentCart[product.id]
        val currentQuantity = existingItem?.quantity ?: 0

        if (currentQuantity >= product.stock) {
            return "Only ${product.stock} available in stock"
        }

        currentCart[product.id] = CheckoutItem(
            productId = product.id,
            name = product.name,
            price = product.price,
            quantity = currentQuantity + 1
        )

        _cartItems.value = currentCart
        return null
    }

    fun removeOneFromCart(productId: String) {
        val currentCart = _cartItems.value?.toMutableMap() ?: mutableMapOf()
        val existingItem = currentCart[productId] ?: return

        if (existingItem.quantity <= 1) {
            currentCart.remove(productId)
        } else {
            currentCart[productId] = existingItem.copy(
                quantity = existingItem.quantity - 1
            )
        }

        _cartItems.value = currentCart
    }

    fun removeCartItem(productId: String) {
        val currentCart = _cartItems.value?.toMutableMap() ?: mutableMapOf()
        currentCart.remove(productId)
        _cartItems.value = currentCart
    }

    fun clearCart() {
        _cartItems.value = emptyMap()
    }

    fun getCartItemsList(): List<CheckoutItem> {
        return _cartItems.value?.values?.toList() ?: emptyList()
    }

    fun getCartTotal(): Double {
        return getCartItemsList().sumOf { item ->
            item.price * item.quantity
        }
    }

    fun getCartItemCount(): Int {
        return getCartItemsList().sumOf { item ->
            item.quantity
        }
    }

    fun syncCartWithLatestProducts(products: List<Product>) {
        val currentCart = _cartItems.value?.toMutableMap() ?: mutableMapOf()

        if (currentCart.isEmpty()) return

        val latestProductsById = products.associateBy { it.id }
        val iterator = currentCart.iterator()

        while (iterator.hasNext()) {
            val entry = iterator.next()
            val latestProduct = latestProductsById[entry.key]

            if (latestProduct == null || latestProduct.stock <= 0) {
                iterator.remove()
            } else if (entry.value.quantity > latestProduct.stock) {
                currentCart[entry.key] = entry.value.copy(
                    quantity = latestProduct.stock,
                    price = latestProduct.price,
                    name = latestProduct.name
                )
            } else {
                currentCart[entry.key] = entry.value.copy(
                    price = latestProduct.price,
                    name = latestProduct.name
                )
            }
        }

        _cartItems.value = currentCart
    }
}