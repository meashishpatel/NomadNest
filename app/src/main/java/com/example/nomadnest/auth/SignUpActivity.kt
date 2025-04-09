package com.example.nomadnest.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.nomadnest.data.db.AppDatabase
import com.example.nomadnest.ui.main.HomeActivity
import com.example.nomadnest.R
import com.example.nomadnest.data.models.UserProfile
import com.example.nomadnest.databinding.ActivitySignUpBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Email/Password Sign-Up
        binding.signUpBtn.setOnClickListener {
            val name = binding.nameET.text.toString()
            val email = binding.emailET.text.toString()
            val password = binding.passwordET.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            user?.updateProfile(
                                com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build()
                            )?.addOnCompleteListener {
                                val userProfile = UserProfile(
                                    userId = user.uid,
                                    name = name,
                                    email = user.email ?: "",
                                    country = "",
                                    phone = user.phoneNumber ?: "",
                                    gender = "",
                                    bio = ""
                                )

                                // Save to Firestore
                                val firestore = FirebaseFirestore.getInstance()
                                firestore.collection("users")
                                    .document(user.uid)
                                    .set(userProfile)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Signed up successfully", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(this, "Failed to save user profile", Toast.LENGTH_SHORT).show()
                                    }

                                // Save to RoomDB if needed
                                val db = AppDatabase.getDatabase(this)
                                lifecycleScope.launch(Dispatchers.IO) {
                                    db.userDao().insertUser(userProfile)
                                }

                                startActivity(Intent(this, LoginActivity::class.java))
                                finish()
                            }
                        } else {
                            Toast.makeText(this, "Sign Up failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        // Google Sign-In Button
        binding.signupwithgooglebtn.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
        binding.signupwithapplebtn.setOnClickListener{
            Toast.makeText(this, "Not supported in Android Version", Toast.LENGTH_SHORT).show()
        }
        binding.signupwithfacebookbtn.setOnClickListener{
            Toast.makeText(this, "Try Sign Up with Google Instead", Toast.LENGTH_SHORT).show()
        }
        binding.loginTV.setOnClickListener{
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google Sign-In failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        val userProfile = UserProfile(
                            userId = user.uid,
                            name = user.displayName ?: "",
                            email = user.email ?: "",
                            country = "",
                            phone = user.phoneNumber ?: "",
                            gender = "",
                            bio = ""
                        )

                        // Save to Firestore
                        val firestore = FirebaseFirestore.getInstance()
                        firestore.collection("users")
                            .document(user.uid)
                            .set(userProfile)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Welcome, ${user?.displayName}", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to save user profile", Toast.LENGTH_SHORT).show()
                            }

                        // Optional: Save to RoomDB if your app uses it
                        val db = AppDatabase.getDatabase(this)
                        lifecycleScope.launch(Dispatchers.IO) {
                            db.userDao().insertUser(userProfile)
                        }

                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    }
                } else {
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
