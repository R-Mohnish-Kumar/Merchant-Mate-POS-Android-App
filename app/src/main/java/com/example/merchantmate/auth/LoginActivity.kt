package com.example.merchantmate.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.merchantmate.ui.MainActivity
import com.example.merchantmate.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        if (firebaseAuth.currentUser != null) {
            openMainActivity()
            return
        }

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            loginUser()
        }

        binding.btnRegister.setOnClickListener {
            createAccount()
        }
    }

    private fun loginUser() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (!validateInput(email, password)) return

        showLoading(true)

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                showLoading(false)
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                firebaseAuth.currentUser?.getIdToken(false)
                    ?.addOnSuccessListener { result ->
                        val token = result.token
                        android.util.Log.d("FIREBASE_ID_TOKEN", token ?: "No token found")
                    }
                    ?.addOnFailureListener { e ->
                        android.util.Log.e("FIREBASE_ID_TOKEN", "Failed to get token", e)
                    }
                openMainActivity()
            }
            .addOnFailureListener { exception ->
                showLoading(false)
                Toast.makeText(
                    this,
                    exception.message ?: "Login failed",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun createAccount() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (!validateInput(email, password)) return

        showLoading(true)

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                showLoading(false)
                Toast.makeText(this, "Account created", Toast.LENGTH_SHORT).show()
                openMainActivity()
            }
            .addOnFailureListener { exception ->
                showLoading(false)
                Toast.makeText(
                    this,
                    exception.message ?: "Account creation failed",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            binding.etEmail.error = "Email is required"
            return false
        }

        if (password.isEmpty()) {
            binding.etPassword.error = "Password is required"
            return false
        }

        if (password.length < 6) {
            binding.etPassword.error = "Password must be at least 6 characters"
            return false
        }

        return true
    }

    private fun openMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !isLoading
        binding.btnRegister.isEnabled = !isLoading
    }
}