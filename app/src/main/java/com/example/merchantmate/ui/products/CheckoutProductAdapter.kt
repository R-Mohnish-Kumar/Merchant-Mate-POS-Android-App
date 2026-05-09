package com.example.merchantmate.ui.products

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.merchantmate.data.model.Product
import com.example.merchantmate.databinding.ItemCheckoutProductBinding

class CheckoutProductAdapter(
    private val onAddToCartClick: (Product) -> Unit
) : RecyclerView.Adapter<CheckoutProductAdapter.CheckoutProductViewHolder>() {

    private val products = mutableListOf<Product>()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newProducts: List<Product>) {
        products.clear()
        products.addAll(newProducts)
        notifyDataSetChanged()
    }

    inner class CheckoutProductViewHolder(
        private val binding: ItemCheckoutProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.tvCheckoutProductName.text = product.name
            binding.tvCheckoutProductCategory.text = product.category
            binding.tvCheckoutProductPrice.text = "£%.2f".format(product.price)
            binding.tvCheckoutProductStock.text = "Stock: ${product.stock}"

            binding.btnCheckoutAddToCart.isEnabled = product.stock > 0
            binding.btnCheckoutAddToCart.text = if (product.stock > 0) {
                "Add to Cart"
            } else {
                "Out of Stock"
            }

            binding.btnCheckoutAddToCart.setOnClickListener {
                onAddToCartClick(product)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckoutProductViewHolder {
        val binding = ItemCheckoutProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return CheckoutProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CheckoutProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size
}