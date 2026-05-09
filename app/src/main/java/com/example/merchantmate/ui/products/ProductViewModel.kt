package com.example.merchantmate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.merchantmate.data.model.CheckoutItem
import com.example.merchantmate.data.model.CheckoutRequest
import com.example.merchantmate.data.model.DashboardSummary
import com.example.merchantmate.data.model.MerchantProfile
import com.example.merchantmate.data.model.Product
import com.example.merchantmate.data.model.ProductRequest
import com.example.merchantmate.data.model.ProfileRequest
import com.example.merchantmate.data.model.TodayInsights
import com.example.merchantmate.data.model.Transaction
import com.example.merchantmate.data.model.WeeklySales
import com.example.merchantmate.data.repository.ProductRepository
import com.example.merchantmate.utils.UiState
import kotlinx.coroutines.launch

class ProductViewModel(
    private val repository: ProductRepository
) : ViewModel() {

    private val _productsState = MutableLiveData<UiState<List<Product>>>()
    val productsState: LiveData<UiState<List<Product>>> = _productsState

    private val _profileState = MutableLiveData<UiState<MerchantProfile>>()
    val profileState: LiveData<UiState<MerchantProfile>> = _profileState

    private val _checkoutState = MutableLiveData<UiState<Any>>()
    val checkoutState: LiveData<UiState<Any>> = _checkoutState

    private val _dashboardState = MutableLiveData<UiState<DashboardSummary>>()
    val dashboardState: LiveData<UiState<DashboardSummary>> = _dashboardState

    private val _insightsState = MutableLiveData<UiState<TodayInsights>>()
    val insightsState: LiveData<UiState<TodayInsights>> = _insightsState

    private val _transactionsState = MutableLiveData<UiState<List<Transaction>>>()
    val transactionsState: LiveData<UiState<List<Transaction>>> = _transactionsState

    private val _cartItems = MutableLiveData<Map<String, CheckoutItem>>(emptyMap())
    val cartItems: LiveData<Map<String, CheckoutItem>> = _cartItems

    private val _weeklySalesState = MutableLiveData<UiState<List<WeeklySales>>>()
    val weeklySalesState: LiveData<UiState<List<WeeklySales>>> = _weeklySalesState

    fun loadProducts() {
        viewModelScope.launch {
            _productsState.value = UiState.Loading

            try {
                val result = repository.getProducts()
                _productsState.value = UiState.Success(result.data ?: emptyList())
            } catch (e: Exception) {
                _productsState.value = UiState.Error(e.message ?: "Something went wrong")
            }
        }
    }

    fun addProduct(request: ProductRequest) {
        viewModelScope.launch {
            try {
                repository.addProduct(request)
                loadProducts()
            } catch (e: Exception) {
                _productsState.value = UiState.Error(e.message ?: "Failed to add product")
            }
        }
    }

    fun updateProduct(productId: String, request: ProductRequest) {
        viewModelScope.launch {
            try {
                repository.updateProduct(productId, request)
                loadProducts()
            } catch (e: Exception) {
                _productsState.value = UiState.Error(e.message ?: "Failed to update product")
            }
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            try {
                repository.deleteProduct(productId)
                loadProducts()
            } catch (e: Exception) {
                _productsState.value = UiState.Error(e.message ?: "Failed to delete product")
            }
        }
    }

    fun loadProfile() {
        viewModelScope.launch {
            _profileState.value = UiState.Loading

            try {
                val result = repository.getProfile()
                result.data?.let {
                    _profileState.value = UiState.Success(it)
                } ?: run {
                    _profileState.value = UiState.Error("Profile data not found")
                }
            } catch (e: Exception) {
                _profileState.value = UiState.Error(e.message ?: "Failed to load profile")
            }
        }
    }

    fun updateProfile(request: ProfileRequest) {
        viewModelScope.launch {
            _profileState.value = UiState.Loading

            try {
                val result = repository.updateProfile(request)
                result.data?.let {
                    _profileState.value = UiState.Success(it)
                } ?: run {
                    _profileState.value = UiState.Error("Profile update failed")
                }
            } catch (e: Exception) {
                _profileState.value = UiState.Error(e.message ?: "Failed to update profile")
            }
        }
    }

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

        val updatedItem = CheckoutItem(
            productId = product.id,
            name = product.name,
            price = product.price,
            quantity = currentQuantity + 1
        )

        currentCart[product.id] = updatedItem
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
        return _cartItems.value?.values?.sumOf { item ->
            item.price * item.quantity
        } ?: 0.0
    }

    fun getCartItemCount(): Int {
        return _cartItems.value?.values?.sumOf { item ->
            item.quantity
        } ?: 0
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

    fun checkout(items: List<CheckoutItem>, paymentMethod: String) {
        viewModelScope.launch {
            _checkoutState.value = UiState.Loading

            try {
                val request = CheckoutRequest(
                    items = items,
                    paymentMethod = paymentMethod
                )

                val result = repository.checkout(request)
                _checkoutState.value = UiState.Success(result.data ?: Any())

                clearCart()
                refreshHomeData()
            } catch (e: Exception) {
                _checkoutState.value = UiState.Error(e.message ?: "Checkout failed")
            }
        }
    }

    fun loadDashboardSummary() {
        viewModelScope.launch {
            _dashboardState.value = UiState.Loading

            try {
                val result = repository.getDashboardSummary()
                result.data?.let {
                    _dashboardState.value = UiState.Success(it)
                } ?: run {
                    _dashboardState.value = UiState.Error("Dashboard data not found")
                }
            } catch (e: Exception) {
                _dashboardState.value = UiState.Error(e.message ?: "Failed to load dashboard")
            }
        }
    }

    fun loadTodayInsights() {
        viewModelScope.launch {
            _insightsState.value = UiState.Loading

            try {
                val result = repository.getTodayInsights()
                result.data?.let {
                    _insightsState.value = UiState.Success(it)
                } ?: run {
                    _insightsState.value = UiState.Error("Insights data not found")
                }
            } catch (e: Exception) {
                _insightsState.value = UiState.Error(e.message ?: "Failed to load insights")
            }
        }
    }

    fun loadTransactions() {
        viewModelScope.launch {
            _transactionsState.value = UiState.Loading

            try {
                val result = repository.getTransactions()
                _transactionsState.value = UiState.Success(result.data ?: emptyList())
            } catch (e: Exception) {
                _transactionsState.value = UiState.Error(e.message ?: "Failed to load transactions")
            }
        }
    }

    fun refreshHomeData() {
        loadProducts()
        loadDashboardSummary()
        loadTodayInsights()
        loadTransactions()
    }

    fun loadWeeklySales() {
        viewModelScope.launch {
            _weeklySalesState.value = UiState.Loading

            try {
                val response = repository.getWeeklySales()

                if (response.success && response.data != null) {
                    _weeklySalesState.value = UiState.Success(response.data)
                } else {
                    _weeklySalesState.value =
                        UiState.Error(response.message ?: "Failed to load weekly sales")
                }

            } catch (e: Exception) {
                _weeklySalesState.value =
                    UiState.Error(e.message ?: "Something went wrong")
            }
        }
    }
}