package com.example.inbetween

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ContactsActivity : BaseActivity() {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestoreDb  = FirebaseFirestore.getInstance()

    private lateinit var etFriendEmail       : TextInputEditText
    private lateinit var rgPermission        : RadioGroup
    private lateinit var rbViewOnly          : RadioButton
    private lateinit var rbEdit              : RadioButton
    private lateinit var btnSendRequest      : Button
    private lateinit var btnMySchedule       : Button

    private lateinit var rvRequests          : RecyclerView
    private lateinit var rvViewOnlyFriends   : RecyclerView
    private lateinit var rvFullAccessFriends : RecyclerView

    private lateinit var requestAdapter      : ContactRequestAdapter
    private lateinit var viewOnlyAdapter     : FriendAdapter
    private lateinit var fullAccessAdapter   : FriendAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        etFriendEmail       = findViewById(R.id.etFriendEmail)
        rgPermission        = findViewById(R.id.rgPermission)
        rbViewOnly          = findViewById(R.id.rbViewOnly)
        rbEdit              = findViewById(R.id.rbEdit)
        btnSendRequest      = findViewById(R.id.btnSendRequest)
        btnMySchedule       = findViewById(R.id.btnMySchedule)

        rvRequests          = findViewById(R.id.rvRequests)
        rvViewOnlyFriends   = findViewById(R.id.rvViewOnlyFriends)
        rvFullAccessFriends = findViewById(R.id.rvFullAccessFriends)

        requestAdapter = ContactRequestAdapter(::handleAccept, ::handleDecline)

        viewOnlyAdapter = FriendAdapter(
            onClick  = { openFriendCalendar(it) },
            onDelete = { removeFriend(it) }
        )
        fullAccessAdapter = FriendAdapter(
            onClick  = { openFriendCalendar(it) },
            onDelete = { removeFriend(it) }
        )

        rvRequests.layoutManager          = LinearLayoutManager(this)
        rvRequests.adapter                = requestAdapter

        rvViewOnlyFriends.layoutManager   = LinearLayoutManager(this)
        rvViewOnlyFriends.adapter         = viewOnlyAdapter

        rvFullAccessFriends.layoutManager = LinearLayoutManager(this)
        rvFullAccessFriends.adapter       = fullAccessAdapter

        btnSendRequest.setOnClickListener {
            val email = etFriendEmail.text.toString().trim().lowercase()
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter an email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val permission = if (rbEdit.isChecked) "edit" else "view"
            sendFriendRequest(email, permission)
        }

        btnMySchedule.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        loadRequests()
        loadFriends()
    }

    private fun sendFriendRequest(email: String, permission: String) {
        Log.d("Contacts", "Trying to send request to $email with perm=$permission")
        firestoreDb.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { snaps ->
                if (snaps.isEmpty) {
                    Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
                val friendUid = snaps.documents.first().id
                val me        = firebaseAuth.currentUser!!.uid
                val myName    = firebaseAuth.currentUser!!.displayName ?: "Unknown"

                firestoreDb.collection("users")
                    .document(me)
                    .collection("friends")
                    .document(friendUid)
                    .set(mapOf(
                        "permission" to permission,
                        "status"     to "pending"
                    ))

                firestoreDb.collection("users")
                    .document(friendUid)
                    .collection("friends")
                    .document(me)
                    .set(mapOf(
                        "permission" to permission,
                        "status"     to "pending",
                        "fromName"   to myName
                    ))
                    .addOnSuccessListener {
                        Log.d("Contacts", "Request sent successfully")
                        Toast.makeText(this, "Request sent", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Log.e("Contacts", "Failed to send request", e)
                        Toast.makeText(
                            this,
                            "Error sending request: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
            .addOnFailureListener { e ->
                Log.e("Contacts", "Lookup by email failed", e)
                Toast.makeText(
                    this,
                    "Error finding user: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun loadRequests() {
        val me = firebaseAuth.currentUser!!.uid
        firestoreDb.collection("users")
            .document(me)
            .collection("friends")
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snaps, _ ->
                val pending = snaps?.documents.orEmpty().mapNotNull { doc ->
                    val uid      = doc.id
                    val fromName = doc.getString("fromName") ?: return@mapNotNull null
                    val perm     = doc.getString("permission") ?: "view"
                    Request(uid, fromName, perm)
                }
                requestAdapter.submitList(pending)
            }
    }

    private fun loadFriends() {
        val me = firebaseAuth.currentUser!!.uid
        firestoreDb.collection("users")
            .document(me)
            .collection("friends")
            .whereEqualTo("status", "accepted")
            .addSnapshotListener { snaps, _ ->
                val viewList = mutableListOf<Friend>()
                val editList = mutableListOf<Friend>()
                snaps?.documents.orEmpty().forEach { doc ->
                    val uid        = doc.id
                    val permission = doc.getString("permission") ?: "view"
                    firestoreDb.collection("users")
                        .document(uid)
                        .get()
                        .addOnSuccessListener { userDoc ->
                            val name = userDoc.getString("displayName") ?: uid
                            val friend = Friend(uid, name, permission)
                            if (permission == "view") viewList += friend
                            else                     editList += friend

                            viewOnlyAdapter.submitList(viewList.toList())
                            fullAccessAdapter.submitList(editList.toList())
                        }
                }
            }
    }

    private fun handleAccept(req: Request) {
        val me = firebaseAuth.currentUser!!.uid
        firestoreDb.collection("users")
            .document(me)
            .collection("friends")
            .document(req.fromUid)
            .update("status", "accepted")
        firestoreDb.collection("users")
            .document(req.fromUid)
            .collection("friends")
            .document(me)
            .update("status", "accepted")
    }

    private fun handleDecline(req: Request) {
        val me = firebaseAuth.currentUser!!.uid
        firestoreDb.collection("users")
            .document(me)
            .collection("friends")
            .document(req.fromUid)
            .delete()
        firestoreDb.collection("users")
            .document(req.fromUid)
            .collection("friends")
            .document(me)
            .delete()
    }

    private fun openFriendCalendar(friend: Friend) {
        startActivity(Intent(this, HomeActivity::class.java).apply {
            putExtra("EXTRA_VIEW_USER", friend.uid)
            putExtra("EXTRA_PERMISSION", friend.permission)
        })
    }

    private fun removeFriend(friend: Friend) {
        val me = firebaseAuth.currentUser!!.uid

        viewOnlyAdapter.submitList(
            viewOnlyAdapter.currentList.filter { it.uid != friend.uid }
        )
        fullAccessAdapter.submitList(
            fullAccessAdapter.currentList.filter { it.uid != friend.uid }
        )

        firestoreDb.collection("users")
            .document(me)
            .collection("friends")
            .document(friend.uid)
            .delete()
        firestoreDb.collection("users")
            .document(friend.uid)
            .collection("friends")
            .document(me)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Removed ${friend.name}", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error removing friend: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                Log.e("Contacts", "removeFriend failed", e)
            }
    }
}
