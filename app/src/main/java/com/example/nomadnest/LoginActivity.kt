package com.example.nomadnest

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nomadnest.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast
import android.content.SharedPreferences
import android.content.Intent
import androidx.core.content.edit

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        sharedPreferences = getSharedPreferences("AuthPrefs", MODE_PRIVATE)

        binding.loginBtn.setOnClickListener {
            val email = binding.emailET.text.toString().trim()
            val password = binding.passwordET.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (binding.stayLoggedInCheckBox.isChecked) {
                        sharedPreferences.edit { putLong("lastLogin", System.currentTimeMillis()) }
                    }
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.forgetPassword.setOnClickListener {
            val email = binding.emailET.text.toString()
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email to reset password", Toast.LENGTH_SHORT).show()
            } else {
                auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Reset link sent to your email", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    }
}