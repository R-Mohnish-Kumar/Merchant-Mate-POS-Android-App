package com.example.merchantmate.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.merchantmate.ProductViewModel
import com.example.merchantmate.data.api.ApiClient
import com.example.merchantmate.data.model.CheckoutItem
import com.example.merchantmate.data.model.Product
import com.example.merchantmate.data.repository.ProductRepository
import com.example.merchantmate.databinding.ActivityCartBinding
import com.example.merchantmate.ui.products.CartAdapter
import com.example.merchantmate.ui.products.CartManager
import com.example.merchantmate.ui.products.ProductViewModelFactory
import com.example.merchantmate.utils.UiState


class CartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCartBinding
    private lateinit var productViewModel: ProductViewModel
    private lateinit var cartAdapter: CartAdapter

    private val latestProducts = mutableListOf<Product>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            view.setPadding(
                view.paddingLeft,
                systemBars.top,
                view.paddingRight,
                view.paddingBottom
            )

            insets
        }

        setupViewModel()
        setupRecyclerView()
        setupClickListeners()
        observeProducts()
        observeCart()
        observeCheckout()

        productViewModel.loadProducts()
    }

    private fun setupViewModel() {
        val repository = ProductRepository(ApiClient.apiService)
        val factory = ProductViewModelFactory(repository)

        productViewModel = ViewModelProvider(
            this,
            factory
        )[ProductViewModel::class.java]
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(
            onIncreaseClick = { item ->
                val product = latestProducts.find { it.id == item.productId }

                if (product == null) {
                    Toast.makeText(this, "Product no longer available", Toast.LENGTH_SHORT).show()
                    CartManager.removeCartItem(item.productId)
                } else {
                    val errorMessage = CartManager.addToCart(product)

                    if (errorMessage != null) {
                        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onDecreaseClick = { item ->
                CartManager.removeOneFromCart(item.productId)
            },
            onRemoveClick = { item ->
                CartManager.removeCartItem(item.productId)
            }
        )

        binding.rvCartItems.apply {
            layoutManager = LinearLayoutManager(this@CartActivity)
            adapter = cartAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnCloseCart.setOnClickListener {
            finish()
        }

        binding.btnClearCart.setOnClickListener {
            CartManager.clearCart()
        }

        binding.btnCompleteCheckout.setOnClickListener {
            showPaymentMethodDialog()
        }
    }

    private fun observeProducts() {
        productViewModel.productsState.observe(this) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.progressCartCheckout.visibility = View.VISIBLE
                }

                is UiState.Success -> {
                    binding.progressCartCheckout.visibility = View.GONE

                    latestProducts.clear()
                    latestProducts.addAll(state.data)

                    CartManager.syncCartWithLatestProducts(state.data)
                }

                is UiState.Error -> {
                    binding.progressCartCheckout.visibility = View.GONE
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }

                else -> {}
            }
        }
    }

    private fun observeCart() {
        CartManager.cartItems.observe(this) { cartMap ->
            val cartList = cartMap.values.toList()
            cartAdapter.submitList(cartList)
            updateCartSummary(cartList)
        }
    }

    private fun observeCheckout() {
        productViewModel.checkoutState.observe(this) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.progressCartCheckout.visibility = View.VISIBLE
                    binding.btnCompleteCheckout.isEnabled = false
                    binding.btnClearCart.isEnabled = false
                }

                is UiState.Success -> {
                    binding.progressCartCheckout.visibility = View.GONE

                    Toast.makeText(
                        this,
                        "Checkout completed successfully",
                        Toast.LENGTH_LONG
                    ).show()

                    CartManager.clearCart()
                    finish()
                }

                is UiState.Error -> {
                    binding.progressCartCheckout.visibility = View.GONE
                    binding.btnCompleteCheckout.isEnabled = true
                    binding.btnClearCart.isEnabled = true

                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }

                else -> {}
            }
        }
    }

    private fun updateCartSummary(cartList: List<CheckoutItem>) {
        val itemCount = cartList.sumOf { it.quantity }
        val total = cartList.sumOf { it.price * it.quantity }

        binding.tvCartTotalItems.text = "Items: $itemCount"
        binding.tvCartGrandTotal.text = "Total: ${formatCurrency(total)}"

        binding.tvCartEmpty.visibility =
            if (cartList.isEmpty()) View.VISIBLE else View.GONE

        binding.cartBottomCard.visibility =
            if (cartList.isEmpty()) View.GONE else View.VISIBLE

        binding.btnCompleteCheckout.isEnabled = cartList.isNotEmpty()
        binding.btnClearCart.isEnabled = cartList.isNotEmpty()
    }

    private fun showPaymentMethodDialog() {
        val items = CartManager.getCartItemsList()

        if (items.isEmpty()) {
            Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show()
            return
        }

        val radioGroup = RadioGroup(this).apply {
            orientation = RadioGroup.VERTICAL
            setPadding(48, 24, 48, 8)

            val cashButton = RadioButton(this@CartActivity).apply {
                id = View.generateViewId()
                text = "CASH"
                textSize = 16f
            }

            val cardButton = RadioButton(this@CartActivity).apply {
                id = View.generateViewId()
                text = "CARD"
                textSize = 16f
            }

            addView(cashButton)
            addView(cardButton)

            check(cardButton.id)
        }

        AlertDialog.Builder(this)
            .setTitle("Choose Payment Method")
            .setView(radioGroup)
            .setPositiveButton("Confirm") { _, _ ->
                val selectedButtonId = radioGroup.checkedRadioButtonId
                val selectedButton =
                    radioGroup.findViewById<RadioButton>(selectedButtonId)

                val paymentMethod = selectedButton.text.toString()

                confirmCheckout(paymentMethod)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmCheckout(paymentMethod: String) {
        val items = CartManager.getCartItemsList()

        if (items.isEmpty()) {
            Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Confirm Checkout")
            .setMessage(
                "Payment method: $paymentMethod\n" +
                        "Items: ${items.sumOf { it.quantity }}\n" +
                        "Total: ${formatCurrency(CartManager.getCartTotal())}\n\n" +
                        "Complete this transaction?"
            )
            .setPositiveButton("Complete") { _, _ ->
                productViewModel.checkout(items, paymentMethod)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun formatCurrency(value: Double): String {
        return "£%.2f".format(value)
    }
}