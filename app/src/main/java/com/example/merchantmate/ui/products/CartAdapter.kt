package com.example.merchantmate.ui.products

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.merchantmate.data.model.CheckoutItem
import com.example.merchantmate.databinding.ItemCartBinding

class CartAdapter(
    private val onIncreaseClick: (CheckoutItem) -> Unit,
    private val onDecreaseClick: (CheckoutItem) -> Unit,
    private val onRemoveClick: (CheckoutItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private val cartItems = mutableListOf<CheckoutItem>()

    fun submitList(newItems: List<CheckoutItem>) {
        cartItems.clear()
        cartItems.addAll(newItems)
        notifyDataSetChanged()
    }

    inner class CartViewHolder(
        private val binding: ItemCartBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CheckoutItem) {
            binding.tvCartItemName.text = item.name
            binding.tvCartItemPrice.text = "£%.2f each".format(item.price)
            binding.tvCartItemQuantity.text = item.quantity.toString()
            binding.tvCartItemLineTotal.text = "£%.2f".format(item.price * item.quantity)

            binding.btnIncreaseCartItem.setOnClickListener {
                onIncreaseClick(item)
            }

            binding.btnDecreaseCartItem.setOnClickListener {
                onDecreaseClick(item)
            }

            binding.btnRemoveCartItem.setOnClickListener {
                onRemoveClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(cartItems[position])
    }

    override fun getItemCount(): Int = cartItems.size
}