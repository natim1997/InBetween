package com.example.inbetween

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class SignupActivity : BaseActivity() {
    private val firestoreDb = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val returnText    = findViewById<TextView>(R.id.btnReturn)
        val nameInput     = findViewById<EditText>(R.id.nameInput)
        val emailInput    = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val confirmInput  = findViewById<EditText>(R.id.confirmPasswordInput)
        val signUpButton  = findViewById<Button>(R.id.signUpButton)

        returnText.setOnClickListener { finish() }

        signUpButton.setOnClickListener {
            val name    = nameInput.text.toString().trim()
            val email   = emailInput.text.toString().trim()
            val pass    = passwordInput.text.toString()
            val confirm = confirmInput.text.toString()

            if (name.isEmpty() || email.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (pass != confirm) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // משתמשים ב-auth שמגיע מ-BaseActivity
            auth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener { result ->
                    result.user?.updateProfile(
                        userProfileChangeRequest { displayName = name }
                    )

                    val uid = result.user!!.uid
                    val profileData = mapOf(
                        "email"       to email,
                        "displayName" to name,
                        "createdAt"   to FieldValue.serverTimestamp()
                    )
                    firestoreDb.collection("users")
                        .document(uid)
                        .set(profileData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Welcome, $name!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, HomeActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                this,
                                "Couldn’t save profile: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        e.localizedMessage ?: "Sign up failed",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }
}
