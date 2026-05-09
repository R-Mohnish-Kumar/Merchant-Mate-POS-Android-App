package com.example.merchantmate.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.merchantmate.ProductViewModel
import com.example.merchantmate.data.api.ApiClient
import com.example.merchantmate.data.model.Transaction
import com.example.merchantmate.data.repository.ProductRepository
import com.example.merchantmate.databinding.FragmentTransactionsBinding
import com.example.merchantmate.ui.products.ProductViewModelFactory
import com.example.merchantmate.ui.products.TransactionAdapter
import com.example.merchantmate.utils.UiState
import android.text.TextWatcher
import com.google.gson.Gson

class TransactionsFragment : Fragment() {

    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!

    private lateinit var productViewModel: ProductViewModel
    private lateinit var transactionAdapter: TransactionAdapter

    private var allTransactions: List<Transaction> = emptyList()
    private var transactionSearchQuery: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupViewModel()
        setupRecyclerView()
        setupSwipeRefresh()
        observeTransactions()
        setupTransactionSearch()

        productViewModel.loadTransactions()
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
        transactionAdapter = TransactionAdapter{transaction ->
            val transactionJson = Gson().toJson(transaction)
            val intent = Intent(requireContext(), ReceiptDetailsActivity::class.java)
            intent.putExtra("transaction_json",transactionJson)
            startActivity(intent)

        }

        binding.rvTransactions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = transactionAdapter
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshTransactions.setOnRefreshListener {
            productViewModel.loadTransactions()
        }
    }

    private fun observeTransactions() {
        productViewModel.transactionsState.observe(viewLifecycleOwner) { state ->
            binding.swipeRefreshTransactions.isRefreshing = false

            when (state) {
                is UiState.Loading -> {
                    binding.progressTransactions.visibility = View.VISIBLE
                    binding.tvTransactionsEmpty.visibility = View.GONE
                }

                is UiState.Success -> {
                    binding.progressTransactions.visibility = View.GONE

                    val transactions = state.data
                    transactionAdapter.submitList(transactions)
                    handleTransactionsSuccess(transactions)

                    binding.tvTransactionsEmpty.visibility =
                        if (transactions.isEmpty()) View.VISIBLE else View.GONE
                }

                is UiState.Error -> {
                    binding.progressTransactions.visibility = View.GONE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }

                else -> {}
            }
        }
    }

    private fun handleTransactionsSuccess(transactions: List<Transaction>) {
        allTransactions = transactions
        applyTransactionFilter()
    }

    private fun setupTransactionSearch() {
        binding.etTransactionSearch.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                transactionSearchQuery = s?.toString()?.trim().orEmpty()
                applyTransactionFilter()
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })
    }

    private fun applyTransactionFilter() {
        val filteredTransactions = allTransactions.filter { transaction ->

            val receiptMatch = transaction.receiptId.contains(
                transactionSearchQuery,
                ignoreCase = true
            )

            val totalMatch = transaction.total.toString().contains(
                transactionSearchQuery,
                ignoreCase = true
            )

            receiptMatch || totalMatch
        }

        transactionAdapter.submitList(filteredTransactions)

        binding.tvTransactionsEmpty.visibility =
            if (filteredTransactions.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}