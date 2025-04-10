package com.example.nomadnest.auth

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nomadnest.ui.main.HomeActivity
import com.example.nomadnest.R
import com.example.nomadnest.data.models.UserProfile
import com.example.nomadnest.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var oneTapClient: SignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        sharedPreferences = getSharedPreferences("AuthPrefs", MODE_PRIVATE)

        // Google Sign-In Setup
        oneTapClient = Identity.getSignInClient(this)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // From Firebase
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)

        val googleSignInLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.result
                    firebaseAuthWithGoogle(account)
                } catch (e: Exception) {
                    Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show()
                }
            }

        binding.loginwithgoogle.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }

        binding.loginwithApple.setOnClickListener{
            Toast.makeText(this, "Not supported in Android Version", Toast.LENGTH_SHORT).show()
        }
        binding.loginwithFB.setOnClickListener{
            Toast.makeText(this, "Try Sign Up with Google Instead", Toast.LENGTH_SHORT).show()
        }

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

                    val user = auth.currentUser
                    if (user != null) {
                        sharedPreferences.edit {
                            putLong("lastLogin", System.currentTimeMillis())
                            putString("userId", user.uid)
                            apply()
                        }
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

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                if (user != null)
                {
                    sharedPreferences.edit {
                        putString("userId", user.uid) // Save userId
                        apply()
                    }
                    // Save to Firestore if user doc doesn't exist
                    val db = FirebaseFirestore.getInstance()
                    val userRef = db.collection("users").document(user.uid)
                    userRef.get().addOnSuccessListener { doc ->
                        if (!doc.exists()) {
                            val userProfile = UserProfile(
                                userId = user.uid,
                                name = user.displayName ?: "",
                                email = user.email ?: "",
                                country = "",
                                phone = user.phoneNumber ?: "",
                                gender = "",
                                bio = ""
                            )
                            userRef.set(userProfile)
                        }
                    }
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
            } else {
                Toast.makeText(this, "Firebase Auth Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}