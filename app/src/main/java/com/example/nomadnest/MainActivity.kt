package com.example.nomadnest

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nomadnest.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.result
                firebaseAuthWithGoogle(account)
            } catch (e: Exception) {
                Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.signuptextview.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        binding.loginbtn.setOnClickListener {
            startActivity(Intent(this, AuthenticationActivity::class.java))
        }
        binding.continuewithapplebtnm.setOnClickListener{
            Toast.makeText(this, "Not supported in Android Version", Toast.LENGTH_SHORT).show()
        }
        binding.continuewithfacebookbtnm.setOnClickListener{
            Toast.makeText(this, "Try Sign Up with Google Instead", Toast.LENGTH_SHORT).show()
        }

        binding.continuewithgooglebtnm.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                if (user != null) {
                    // Save to Firestore if user doesn't exist
                    val db = FirebaseFirestore.getInstance()
                    val userRef = db.collection("users").document(user.uid)
                    userRef.get().addOnSuccessListener { doc ->
                        if (!doc.exists()) {
                            val userProfile = UserProfile(
                                userId = user.uid,
                                name = user.displayName ?: "",
                                country = "",
                                email = user.email ?: "",
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
                Toast.makeText(this, "Firebase Auth failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}