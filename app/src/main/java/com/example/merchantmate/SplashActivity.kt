package com.example.merchantmate


import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.example.merchantmate.ui.MainActivity
import com.example.merchantmate.auth.LoginActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_MerchantMate_Starting)
        super.onCreate(savedInstanceState)

        val firebaseAuth = FirebaseAuth.getInstance()

        val nextActivity = if (firebaseAuth.currentUser != null) {
            MainActivity::class.java
        } else {
            LoginActivity::class.java
        }

        startActivity(Intent(this, nextActivity))
        finish()
    }
}