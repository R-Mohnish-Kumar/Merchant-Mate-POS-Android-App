package com.example.merchantmate

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.merchantmate.data.model.Product
import com.example.merchantmate.databinding.ItemProductBinding

class ProductAdapter(
    private val onAddToCartClick: (Product) -> Unit,
    private val onEditClick: (Product) -> Unit,
    private val onDeleteClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    private val products = mutableListOf<Product>()

    fun submitList(newProducts: List<Product>) {
        products.clear()
        products.addAll(newProducts)
        notifyDataSetChanged()
    }

    inner class ProductViewHolder(
        private val binding: ItemProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.tvProductName.text = product.name
            binding.tvProductCategory.text = product.category
            binding.tvProductPrice.text = "£%.2f".format(product.price)
            binding.tvProductStock.text = "Stock: ${product.stock}"

            binding.btnAddToCart.setOnClickListener {
                onAddToCartClick(product)
            }

            binding.btnEditProduct.setOnClickListener {
                onEditClick(product)
            }

            binding.btnDeleteProduct.setOnClickListener {
                onDeleteClick(product)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size
}