package com.example.merchantmate.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.merchantmate.R
import com.example.merchantmate.databinding.ActivityMainBinding
import com.example.merchantmate.fragments.CheckoutFragment
import com.example.merchantmate.fragments.DashboardFragment
import com.example.merchantmate.fragments.ProductsFragment
import com.example.merchantmate.fragments.ProfileFragment
import com.example.merchantmate.fragments.TransactionsFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
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

        setupBottomNavigation()

        if (savedInstanceState == null) {
            binding.bottomNavigation.selectedItemId = R.id.nav_dashboard
            replaceFragment(DashboardFragment())
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    replaceFragment(DashboardFragment())
                    true
                }

                R.id.nav_products -> {
                    replaceFragment(ProductsFragment())
                    true
                }

                R.id.nav_checkout -> {
                    replaceFragment(CheckoutFragment())
                    true
                }

                R.id.nav_receipts -> {
                    replaceFragment(TransactionsFragment())
                    true
                }

                R.id.nav_profile -> {
                    replaceFragment(ProfileFragment())
                    true
                }

                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}