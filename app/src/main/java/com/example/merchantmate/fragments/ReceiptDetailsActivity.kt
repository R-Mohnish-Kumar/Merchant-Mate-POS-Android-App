package com.example.merchantmate.fragments

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.merchantmate.databinding.ActivityReceiptDetailsBinding
import com.example.merchantmate.data.model.Transaction
import com.example.merchantmate.utils.ReceiptPdfGenerator
import com.google.gson.Gson

class ReceiptDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReceiptDetailsBinding
    private lateinit var transaction: Transaction

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityReceiptDetailsBinding.inflate(layoutInflater)
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

        val transactionJson = intent.getStringExtra("transaction_json")

        if (transactionJson.isNullOrBlank()) {
            Toast.makeText(this, "Receipt not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        transaction = Gson().fromJson(transactionJson, Transaction::class.java)

        bindReceiptDetails(transaction)

        binding.btnSavePdf.setOnClickListener {
            val file = ReceiptPdfGenerator.generateReceiptPdf(this, transaction)

            Toast.makeText(
                this,
                "PDF saved: ${file.absolutePath}",
                Toast.LENGTH_LONG
            ).show()
            Log.e("PDF locatioj", file.absolutePath)
        }
    }

    private fun bindReceiptDetails(transaction: Transaction) {
        binding.tvReceiptId.text = transaction.receiptId
        binding.tvPaymentMethod.text = transaction.paymentMethod
        binding.tvStatus.text = transaction.status
        binding.tvGrandTotal.text = "£%.2f".format(transaction.total)

        val itemBreakdown = transaction.items.joinToString(separator = "\n\n") { item ->
            val lineTotal = item.price * item.quantity

            "${item.name}\n" +
                    "Qty: ${item.quantity} × £%.2f".format(item.price) +
                    "\nLine total: £%.2f".format(lineTotal)
        }

        binding.tvReceiptItems.text = itemBreakdown.ifBlank {
            "No item breakdown available"
        }
    }
}