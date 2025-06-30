package com.example.inbetween

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import com.google.android.material.textfield.TextInputEditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class ContactsActivity : BaseActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    private lateinit var etFriendEmail: TextInputEditText
    private lateinit var btnSendRequest: Button
    private lateinit var rvRequests: RecyclerView
    private lateinit var rvFriends: RecyclerView

    private val incomingRequests = mutableListOf<FriendRequest>()
    private val friendsList      = mutableListOf<Friend>()
    private lateinit var requestsAdapter: FriendRequestAdapter
    private lateinit var friendsAdapter: FriendAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        etFriendEmail   = findViewById(R.id.etFriendEmail)
        btnSendRequest  = findViewById(R.id.btnSendRequest)
        rvRequests      = findViewById(R.id.rvRequests)
        rvFriends       = findViewById(R.id.rvFriends)

        requestsAdapter = FriendRequestAdapter(
            items    = incomingRequests,
            onAccept = { req -> acceptRequest(req) },
            onReject = { req -> rejectRequest(req) }
        )
        rvRequests.layoutManager = LinearLayoutManager(this)
        rvRequests.adapter       = requestsAdapter

        friendsAdapter = FriendAdapter(friendsList)
        rvFriends.layoutManager = LinearLayoutManager(this)
        rvFriends.adapter       = friendsAdapter

        btnSendRequest.setOnClickListener { sendFriendRequest() }

        loadIncomingRequests()
        loadFriends()
    }

    private fun sendFriendRequest() {
        val email = etFriendEmail.text.toString().trim()
        if (email.isEmpty()) return

        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { snaps: QuerySnapshot ->
                if (snaps.isEmpty) return@addOnSuccessListener
                val targetUid = snaps.documents[0].id
                val meUid     = auth.currentUser!!.uid
                val meEmail   = auth.currentUser!!.email ?: ""

                db.collection("users")
                    .document(targetUid)
                    .collection("incoming_requests")
                    .document(meUid)
                    .set(mapOf("fromUid" to meUid, "fromEmail" to meEmail))
            }
            .addOnFailureListener {
            }
    }

    private fun loadIncomingRequests() {
        val meUid = auth.currentUser!!.uid
        db.collection("users")
            .document(meUid)
            .collection("incoming_requests")
            .addSnapshotListener { snaps: QuerySnapshot?, _ ->
                incomingRequests.clear()
                snaps?.documents?.forEach { doc ->
                    val fromUid   = doc.id
                    val fromEmail = doc.getString("fromEmail") ?: ""
                    incomingRequests += FriendRequest(fromUid, fromEmail)
                }
                requestsAdapter.notifyDataSetChanged()
            }
    }

    private fun acceptRequest(req: FriendRequest) {
        val meUid   = auth.currentUser!!.uid
        val meEmail = auth.currentUser!!.email ?: ""

        db.collection("users").document(meUid)
            .collection("friends").document(req.fromUid)
            .set(mapOf("email" to req.fromEmail))

        db.collection("users").document(req.fromUid)
            .collection("friends").document(meUid)
            .set(mapOf("email" to meEmail))

        db.collection("users")
            .document(meUid)
            .collection("incoming_requests")
            .document(req.fromUid)
            .delete()
    }

    private fun rejectRequest(req: FriendRequest) {
        val meUid = auth.currentUser!!.uid
        db.collection("users")
            .document(meUid)
            .collection("incoming_requests")
            .document(req.fromUid)
            .delete()
    }

    private fun loadFriends() {
        val meUid = auth.currentUser!!.uid
        db.collection("users")
            .document(meUid)
            .collection("friends")
            .addSnapshotListener { snaps: QuerySnapshot?, _ ->
                friendsList.clear()
                snaps?.documents?.forEach { d ->
                    val uid   = d.id
                    val email = d.getString("email") ?: ""
                    friendsList += Friend(uid, email)
                }
                friendsAdapter.notifyDataSetChanged()
            }
    }
}
