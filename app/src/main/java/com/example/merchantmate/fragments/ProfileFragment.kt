package com.example.merchantmate.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.merchantmate.ProductViewModel
import com.example.merchantmate.auth.LoginActivity
import com.example.merchantmate.data.api.ApiClient
import com.example.merchantmate.data.model.MerchantProfile
import com.example.merchantmate.data.model.ProfileRequest
import com.example.merchantmate.data.repository.ProductRepository
import com.example.merchantmate.databinding.FragmentProfileBinding
import com.example.merchantmate.ui.products.CartManager
import com.example.merchantmate.ui.products.ProductViewModelFactory
import com.example.merchantmate.utils.UiState
import com.google.firebase.auth.FirebaseAuth


class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var productViewModel: ProductViewModel

    private var hasLoadedProfileOnce = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        firebaseAuth = FirebaseAuth.getInstance()

        setupViewModel()
        setupClickListeners()
        observeProfile()

        binding.etEmail.setText(firebaseAuth.currentUser?.email ?: "")

        productViewModel.loadProfile()
    }

    private fun setupViewModel() {
        val repository = ProductRepository(ApiClient.apiService)
        val factory = ProductViewModelFactory(repository)

        productViewModel = ViewModelProvider(
            requireActivity(),
            factory
        )[ProductViewModel::class.java]
    }

    private fun setupClickListeners() {
        binding.btnSaveProfile.setOnClickListener {
            saveProfile()
        }

        binding.btnLogout.setOnClickListener {
            confirmLogout()
        }
    }

    private fun observeProfile() {
        productViewModel.profileState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.progressProfile.visibility = View.VISIBLE
                    binding.btnSaveProfile.isEnabled = false
                }

                is UiState.Success -> {
                    binding.progressProfile.visibility = View.GONE
                    binding.btnSaveProfile.isEnabled = true

                    bindProfile(state.data)

                    if (hasLoadedProfileOnce) {
                        Toast.makeText(
                            requireContext(),
                            "Profile saved successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    hasLoadedProfileOnce = true
                }

                is UiState.Error -> {
                    binding.progressProfile.visibility = View.GONE
                    binding.btnSaveProfile.isEnabled = true

                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }

                else -> {}
            }
        }
    }

    private fun bindProfile(profile: MerchantProfile) {
        binding.etShopName.setText(profile.shopName)
        binding.etOwnerName.setText(profile.ownerName)
        binding.etEmail.setText(
            profile.email.ifBlank { firebaseAuth.currentUser?.email ?: "" }
        )
        binding.etContactNumber.setText(profile.contactNumber)
        binding.etShopAddress.setText(profile.shopAddress)
    }

    private fun saveProfile() {
        val shopName = binding.etShopName.text.toString().trim()
        val ownerName = binding.etOwnerName.text.toString().trim()
        val contactNumber = binding.etContactNumber.text.toString().trim()
        val shopAddress = binding.etShopAddress.text.toString().trim()

        if (shopName.isEmpty()) {
            binding.etShopName.error = "Shop name required"
            return
        }

        if (ownerName.isEmpty()) {
            binding.etOwnerName.error = "Owner name required"
            return
        }

        if (contactNumber.isEmpty()) {
            binding.etContactNumber.error = "Contact number required"
            return
        }

        if (shopAddress.isEmpty()) {
            binding.etShopAddress.error = "Shop address required"
            return
        }

        val request = ProfileRequest(
            shopName = shopName,
            ownerName = ownerName,
            contactNumber = contactNumber,
            shopAddress = shopAddress
        )

        productViewModel.updateProfile(request)
    }

    private fun confirmLogout() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                firebaseAuth.signOut()
                CartManager.clearCart()

                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}