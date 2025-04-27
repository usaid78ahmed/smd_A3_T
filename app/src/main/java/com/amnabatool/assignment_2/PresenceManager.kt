package com.amnabatool.assignment_2

import com.google.firebase.database.FirebaseDatabase

object PresenceManager {
    private val database = FirebaseDatabase.getInstance().getReference("presence")
    fun setUserOnline(userId: String) {
        database.child(userId).setValue("online")
    }
    fun setUserOffline(userId: String) {
        database.child(userId).setValue("offline")
    }
}