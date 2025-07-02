package com.example.inbetween

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : BaseActivity() {
    private val firestoreDb = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passInput  = findViewById<EditText>(R.id.passwordInput)
        val loginBtn   = findViewById<Button>(R.id.loginButton)
        val signUpBtn  = findViewById<Button>(R.id.signUpButton)

        Log.d("LoginActivity", "onCreate")

        signUpBtn.setOnClickListener {
            Log.d("LoginActivity", "Sign-Up button clicked")
            startActivity(Intent(this, SignupActivity::class.java))
        }

        loginBtn.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val pass  = passInput.text.toString()
            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener { result ->
                    Log.d("LoginActivity", "Auth signIn success")
                    ensureUserProfile(result.user!!) {
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("LoginActivity", "Auth signIn failed", e)
                    Toast.makeText(
                        this,
                        e.localizedMessage ?: "Login failed",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }

    private fun ensureUserProfile(user: FirebaseUser, onComplete: () -> Unit) {
        val uid = user.uid
        val doc = firestoreDb.collection("users").document(uid)
        doc.get()
            .addOnSuccessListener { snap ->
                if (snap.exists()) {
                    onComplete()
                } else {
                    val email = user.email ?: ""
                    val name  = user.displayName ?: ""
                    doc.set(mapOf(
                        "email"       to email,
                        "displayName" to name,
                        "createdAt"   to FieldValue.serverTimestamp()
                    ))
                        .addOnSuccessListener {
                            Log.d("LoginActivity", "Profile created in Firestore")
                            onComplete()
                        }
                        .addOnFailureListener { e ->
                            Log.e("LoginActivity", "Failed creating profile", e)
                            Toast.makeText(this,
                                "Error creating profile: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("LoginActivity", "Error fetching profile", e)
                Toast.makeText(this,
                    "Error loading profile: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}
