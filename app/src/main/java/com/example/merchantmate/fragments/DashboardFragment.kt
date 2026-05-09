package com.example.merchantmate.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.merchantmate.ProductViewModel
import com.example.merchantmate.data.api.ApiClient
import com.example.merchantmate.data.model.DashboardSummary
import com.example.merchantmate.data.model.FirestoreTimestamp
import com.example.merchantmate.data.model.LowStockInsight
import com.example.merchantmate.data.model.MerchantProfile
import com.example.merchantmate.data.model.RecentTransaction
import com.example.merchantmate.data.model.TodayInsights
import com.example.merchantmate.data.repository.ProductRepository
import com.example.merchantmate.databinding.FragmentDashboardBinding
import com.example.merchantmate.ui.products.ProductViewModelFactory
import com.example.merchantmate.utils.UiState
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var productViewModel: ProductViewModel

    private var dashboardLoaded = false
    private var insightsLoaded = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        firebaseAuth = FirebaseAuth.getInstance()

        setupViewModel()
        setupUserGreeting()
        setupSwipeRefresh()
        observeProfile()
        observeDashboardSummary()
        observeTodayInsights()


        productViewModel.loadProfile()
        refreshDashboard()
    }

    private fun setupViewModel() {
        val repository = ProductRepository(ApiClient.apiService)
        val factory = ProductViewModelFactory(repository)

        productViewModel = ViewModelProvider(
            requireActivity(),
            factory
        )[ProductViewModel::class.java]
    }

    private fun setupUserGreeting(profile: MerchantProfile? = null) {
        val fallbackEmail = firebaseAuth.currentUser?.email ?: "merchant"

        val displayName = if (!profile?.ownerName.isNullOrBlank()) {
            profile?.ownerName.orEmpty().trim().split(" ").first()
        } else {
            fallbackEmail.substringBefore("@").replaceFirstChar {
                if (it.isLowerCase()) it.titlecase() else it.toString()
            }
        }

        binding.tvGreeting.text = "Hi, $displayName 👋"
        binding.tvSubtitle.text = "Here is your store performance today"
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshDashboard.setOnRefreshListener {
            refreshDashboard()
        }
    }

    private fun refreshDashboard() {
        dashboardLoaded = false
        insightsLoaded = false

        binding.swipeRefreshDashboard.isRefreshing = true

        productViewModel.loadDashboardSummary()
        productViewModel.loadTodayInsights()
    }

    private fun observeDashboardSummary() {
        productViewModel.dashboardState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    dashboardLoaded = false
                }

                is UiState.Success -> {
                    dashboardLoaded = true
                    bindDashboardSummary(state.data)
                    stopRefreshIfComplete()
                }

                is UiState.Error -> {
                    dashboardLoaded = true
                    stopRefreshIfComplete()
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }

                else -> {}
            }
        }
    }

    private fun observeTodayInsights() {
        productViewModel.insightsState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    insightsLoaded = false
                }

                is UiState.Success -> {
                    insightsLoaded = true
                    bindTodayInsights(state.data)
                    stopRefreshIfComplete()
                }

                is UiState.Error -> {
                    insightsLoaded = true
                    stopRefreshIfComplete()
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }

                else -> {}
            }
        }
    }

    private fun observeProfile() {
        productViewModel.profileState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Success -> {
                    setupUserGreeting(state.data)
                }

                is UiState.Error -> {
                    setupUserGreeting()
                }

                is UiState.Loading -> {
                    // Keep default email greeting while profile loads
                }

                else -> {}
            }
        }
    }

    private fun stopRefreshIfComplete() {
        if (dashboardLoaded && insightsLoaded) {
            binding.swipeRefreshDashboard.isRefreshing = false
        }
    }

    private fun bindDashboardSummary(summary: DashboardSummary) {
        binding.tvTodayRevenue.text = formatCurrency(summary.todayRevenue)
        binding.tvOrdersCount.text = summary.transactionCount.toString()
        binding.tvAverageOrderValue.text = formatCurrency(summary.averageOrderValue)

        val bestSeller = summary.bestSellingProduct

        binding.tvBestSeller.text = if (bestSeller != null && bestSeller.name.isNotBlank()) {
            "${bestSeller.name} (${bestSeller.quantitySold} sold)"
        } else {
            "No sales yet"
        }

        bindRecentTransaction(summary.recentTransactions.firstOrNull())
    }

    private fun bindTodayInsights(insights: TodayInsights) {
        val topSellerMessage = insights.topSeller?.message

        binding.tvInsightMessage.text = if (!topSellerMessage.isNullOrBlank()) {
            topSellerMessage
        } else {
            "Start selling to unlock smart insights."
        }

        binding.tvLowStockInsight.text = buildLowStockMessage(insights.lowStock)

        binding.tvRevenueComparison.text = insights.revenueComparison?.message
            ?: "Revenue comparison will appear after transactions."
    }

    private fun buildLowStockMessage(lowStockItems: List<LowStockInsight>): String {
        if (lowStockItems.isEmpty()) {
            return "No low stock products found."
        }

        if (lowStockItems.size == 1) {
            val item = lowStockItems.first()
            return item.message.ifBlank {
                "Low stock: ${item.productName} has only ${item.stock} left."
            }
        }

        val itemPreview = lowStockItems.take(2).joinToString(", ") { item ->
            "${item.productName} (${item.stock} left)"
        }

        return if (lowStockItems.size > 2) {
            "Low stock: $itemPreview and ${lowStockItems.size - 2} more item(s)."
        } else {
            "Low stock: $itemPreview."
        }
    }

    private fun bindRecentTransaction(transaction: RecentTransaction?) {
        if (transaction == null) {
            binding.tvRecentReceiptId.text = "No recent transaction"
            binding.tvRecentTransactionTotal.text = "£0.00"
            binding.tvRecentTransactionPayment.text = "-"
            binding.tvRecentTransactionStatus.text = "-"
            return
        }

        binding.tvRecentReceiptId.text = "Receipt: ${transaction.receiptId.ifBlank { transaction.id }}"
        binding.tvRecentTransactionTotal.text = formatCurrency(transaction.total)
        binding.tvRecentTransactionPayment.text = "Payment: ${transaction.paymentMethod.ifBlank { "-" }}"
        binding.tvRecentTransactionStatus.text =
            "Status: ${transaction.status.ifBlank { "-" }} • ${formatFirestoreTimestamp(transaction.createdAt)}"
    }

    private fun formatCurrency(value: Double): String {
        return "£%.2f".format(value)
    }

    private fun formatFirestoreTimestamp(timestamp: FirestoreTimestamp?): String {
        if (timestamp == null || timestamp._seconds <= 0) {
            return "-"
        }

        return try {
            val date = Date(timestamp._seconds * 1000)
            val formatter = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
            formatter.format(date)
        } catch (e: Exception) {
            "-"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}