package com.example.merchantmate.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.merchantmate.ProductViewModel
import com.example.merchantmate.data.api.ApiClient
import com.example.merchantmate.data.model.Product
import com.example.merchantmate.data.repository.ProductRepository
import com.example.merchantmate.databinding.FragmentCheckoutBinding
import com.example.merchantmate.ui.products.CartManager
import com.example.merchantmate.ui.products.CheckoutProductAdapter
import com.example.merchantmate.ui.products.ProductViewModelFactory
import com.example.merchantmate.utils.UiState


class CheckoutFragment : Fragment() {

    private var _binding: FragmentCheckoutBinding? = null
    private val binding get() = _binding!!

    private lateinit var productViewModel: ProductViewModel
    private lateinit var checkoutProductAdapter: CheckoutProductAdapter

    private val latestProducts = mutableListOf<Product>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCheckoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupViewModel()
        setupProductRecyclerView()
        setupClickListeners()
        observeProducts()
        observeCart()

        productViewModel.loadProducts()
    }

    override fun onResume() {
        super.onResume()
        updateViewCartButton()
        productViewModel.loadProducts()
    }

    private fun setupViewModel() {
        val repository = ProductRepository(ApiClient.apiService)
        val factory = ProductViewModelFactory(repository)

        productViewModel = ViewModelProvider(
            requireActivity(),
            factory
        )[ProductViewModel::class.java]
    }

    private fun setupProductRecyclerView() {
        checkoutProductAdapter = CheckoutProductAdapter(
            onAddToCartClick = { product ->
                val errorMessage = CartManager.addToCart(product)

                if (errorMessage != null) {
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "${product.name} added to cart",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                updateViewCartButton()
            }
        )

        binding.rvCheckoutProducts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = checkoutProductAdapter
        }
    }

    private fun setupClickListeners() {
        binding.swipeRefreshCheckout.setOnRefreshListener {
            productViewModel.loadProducts()
        }

        binding.btnViewCart.setOnClickListener {
            startActivity(Intent(requireContext(), CartActivity::class.java))
        }
    }

    private fun observeProducts() {
        productViewModel.productsState.observe(viewLifecycleOwner) { state ->
            binding.swipeRefreshCheckout.isRefreshing = false

            when (state) {
                is UiState.Loading -> {
                    binding.progressCheckout.visibility = View.VISIBLE
                    binding.tvCheckoutEmpty.visibility = View.GONE
                }

                is UiState.Success -> {
                    binding.progressCheckout.visibility = View.GONE

                    latestProducts.clear()
                    latestProducts.addAll(state.data)

                    checkoutProductAdapter.submitList(state.data)

                    binding.tvCheckoutEmpty.visibility =
                        if (state.data.isEmpty()) View.VISIBLE else View.GONE

                    CartManager.syncCartWithLatestProducts(state.data)
                    updateViewCartButton()
                }

                is UiState.Error -> {
                    binding.progressCheckout.visibility = View.GONE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }

                else -> {}
            }
        }
    }

    private fun observeCart() {
        CartManager.cartItems.observe(viewLifecycleOwner) {
            updateViewCartButton()
        }
    }

    private fun updateViewCartButton() {
        val itemCount = CartManager.getCartItemCount()
        val total = CartManager.getCartTotal()

        if (itemCount > 0) {
            binding.btnViewCart.visibility = View.VISIBLE
            binding.btnViewCart.text = "View Cart • $itemCount item(s) • ${formatCurrency(total)}"
        } else {
            binding.btnViewCart.visibility = View.GONE
        }
    }

    private fun formatCurrency(value: Double): String {
        return "£%.2f".format(value)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}