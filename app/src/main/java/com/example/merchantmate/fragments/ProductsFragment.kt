package com.example.merchantmate.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.merchantmate.ProductAdapter
import com.example.merchantmate.ProductViewModel
import com.example.merchantmate.data.api.ApiClient
import com.example.merchantmate.data.model.Product
import com.example.merchantmate.data.model.ProductRequest
import com.example.merchantmate.data.repository.ProductRepository
import com.example.merchantmate.databinding.DialogAddProductBinding
import com.example.merchantmate.databinding.FragmentProductsBinding
import com.example.merchantmate.ui.products.ProductViewModelFactory
import com.example.merchantmate.utils.UiState

class ProductsFragment : Fragment() {

    private var _binding: FragmentProductsBinding? = null
    private val binding get() = _binding!!

    private lateinit var productViewModel: ProductViewModel
    private lateinit var productAdapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupViewModel()
        setupRecyclerView()
        setupClickListeners()
        observeProducts()

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

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(
            onAddToCartClick = { product ->
                Toast.makeText(
                    requireContext(),
                    "${product.name} can be added from Checkout screen next.",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onEditClick = { product ->
                showEditProductDialog(product)
            },
            onDeleteClick = { product ->
                confirmDeleteProduct(product)
            }
        )

        binding.rvProducts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = productAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnAddProduct.setOnClickListener {
            showAddProductDialog()
        }

        binding.swipeRefreshProducts.setOnRefreshListener {
            productViewModel.loadProducts()
        }
    }

    private fun observeProducts() {
        productViewModel.productsState.observe(viewLifecycleOwner) { state ->
            binding.swipeRefreshProducts.isRefreshing = false

            when (state) {
                is UiState.Loading -> {
                    binding.progressProducts.visibility = View.VISIBLE
                    binding.tvProductsEmpty.visibility = View.GONE
                }

                is UiState.Success -> {
                    binding.progressProducts.visibility = View.GONE

                    val products = state.data
                    productAdapter.submitList(products)

                    binding.tvProductsEmpty.visibility =
                        if (products.isEmpty()) View.VISIBLE else View.GONE
                }

                is UiState.Error -> {
                    binding.progressProducts.visibility = View.GONE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }

                else -> {}
            }
        }
    }

    private fun showAddProductDialog() {
        val dialogBinding = DialogAddProductBinding.inflate(layoutInflater)

        AlertDialog.Builder(requireContext())
            .setTitle("Add Product")
            .setView(dialogBinding.root)
            .setPositiveButton("Add", null)
            .setNegativeButton("Cancel", null)
            .create()
            .apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val name = dialogBinding.etProductName.text.toString().trim()
                        val category = dialogBinding.etProductCategory.text.toString().trim()
                        val priceText = dialogBinding.etProductPrice.text.toString().trim()
                        val stockText = dialogBinding.etProductStock.text.toString().trim()

                        if (name.isEmpty() || category.isEmpty() || priceText.isEmpty() || stockText.isEmpty()) {
                            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        val price = priceText.toDoubleOrNull()
                        val stock = stockText.toIntOrNull()

                        if (price == null || price <= 0) {
                            Toast.makeText(requireContext(), "Enter valid price", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        if (stock == null || stock < 0) {
                            Toast.makeText(requireContext(), "Enter valid stock", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        val request = ProductRequest(
                            name = name,
                            category = category,
                            price = price,
                            stock = stock
                        )

                        productViewModel.addProduct(request)
                        dismiss()
                    }
                }

                show()
            }
    }

    private fun showEditProductDialog(product: Product) {
        val dialogBinding = DialogAddProductBinding.inflate(layoutInflater)

        dialogBinding.etProductName.setText(product.name)
        dialogBinding.etProductCategory.setText(product.category)
        dialogBinding.etProductPrice.setText(product.price.toString())
        dialogBinding.etProductStock.setText(product.stock.toString())

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Product")
            .setView(dialogBinding.root)
            .setPositiveButton("Update", null)
            .setNegativeButton("Cancel", null)
            .create()
            .apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val name = dialogBinding.etProductName.text.toString().trim()
                        val category = dialogBinding.etProductCategory.text.toString().trim()
                        val priceText = dialogBinding.etProductPrice.text.toString().trim()
                        val stockText = dialogBinding.etProductStock.text.toString().trim()

                        if (name.isEmpty() || category.isEmpty() || priceText.isEmpty() || stockText.isEmpty()) {
                            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        val price = priceText.toDoubleOrNull()
                        val stock = stockText.toIntOrNull()

                        if (price == null || price <= 0) {
                            Toast.makeText(requireContext(), "Enter valid price", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        if (stock == null || stock < 0) {
                            Toast.makeText(requireContext(), "Enter valid stock", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        val request = ProductRequest(
                            name = name,
                            category = category,
                            price = price,
                            stock = stock
                        )

                        productViewModel.updateProduct(product.id, request)
                        dismiss()
                    }
                }

                show()
            }
    }

    private fun confirmDeleteProduct(product: Product) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Product")
            .setMessage("Are you sure you want to delete ${product.name}?")
            .setPositiveButton("Delete") { _, _ ->
                productViewModel.deleteProduct(product.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}