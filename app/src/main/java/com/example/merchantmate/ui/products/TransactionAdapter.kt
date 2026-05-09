package com.example.merchantmate.ui.products

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.merchantmate.data.model.FirestoreTimestamp
import com.example.merchantmate.databinding.ItemTransactionBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.merchantmate.data.model.Transaction
import com.example.merchantmate.data.model.TransactionItem

class TransactionAdapter : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    private val transactions = mutableListOf<Transaction>()

    fun submitList(newTransactions: List<Transaction>) {
        transactions.clear()
        transactions.addAll(newTransactions)
        notifyDataSetChanged()
    }

    inner class TransactionViewHolder(
        private val binding: ItemTransactionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: Transaction) {
            val receiptText = transaction.receiptId.ifBlank {
                transaction.id.ifBlank { "Unknown receipt" }
            }

            binding.tvReceiptId.text = "Receipt: $receiptText"
            binding.tvTransactionTotal.text = "£%.2f".format(transaction.total)
            binding.tvTransactionStatus.text = transaction.status.ifBlank { "UNKNOWN" }

            val itemCount = transaction.items.sumOf { it.quantity }
            binding.tvTransactionMeta.text =
                "Payment: ${transaction.paymentMethod.ifBlank { "-" }} • Items: $itemCount"

            binding.tvTransactionDate.text = formatFirestoreTimestamp(transaction.createdAt)
            binding.tvTransactionItems.text = buildItemsPreview(transaction.items)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    override fun getItemCount(): Int = transactions.size

    private fun buildItemsPreview(items: List<TransactionItem>): String {
        if (items.isEmpty()) {
            return "Items: No item details"
        }

        val preview = items.take(3).joinToString(", ") { item ->
            "${item.name} x${item.quantity}"
        }

        return if (items.size > 3) {
            "Items: $preview +${items.size - 3} more"
        } else {
            "Items: $preview"
        }
    }

    private fun formatFirestoreTimestamp(timestamp: FirestoreTimestamp?): String {
        if (timestamp == null || timestamp._seconds <= 0) {
            return "-"
        }

        return try {
            val date = Date(timestamp._seconds * 1000)
            val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            formatter.format(date)
        } catch (e: Exception) {
            "-"
        }
    }
}