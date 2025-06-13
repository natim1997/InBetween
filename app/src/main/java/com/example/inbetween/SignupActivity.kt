package com.example.inbetween

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.userProfileChangeRequest

class SignupActivity : BaseActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()
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

            auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        auth.currentUser?.updateProfile(
                            userProfileChangeRequest { displayName = name }
                        )?.addOnCompleteListener {
                            startActivity(Intent(this, HomeActivity::class.java))
                            finish()
                        }
                    } else {
                        Toast.makeText(
                            this,
                            task.exception?.localizedMessage ?: "Sign up failed",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }
}
