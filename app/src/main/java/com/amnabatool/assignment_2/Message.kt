package com.amnabatool.assignment_2

import android.os.Parcelable
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import kotlinx.parcelize.Parcelize

@IgnoreExtraProperties
@Parcelize
data class Message(
    val id: String? = null,
    val text: String? = null,
    // Change from val to var so that the @set:PropertyName annotation works.
    @get:PropertyName("sentByUser") @set:PropertyName("sentByUser")
    var isSentByUser: Boolean = false,
    val time: String = "",
    val type: MessageType = MessageType.TEXT,
    val imageUri: String? = null,
    val creationTime: Long = System.currentTimeMillis(),
    val isEphemeral: Boolean = false,
    val vanishDeadline: Long? = null
) : Parcelable {
    // No-argument constructor for Firestore deserialization
    constructor() : this(
        id = null,
        text = null,
        isSentByUser = false,
        time = "",
        type = MessageType.TEXT,
        imageUri = null,
        creationTime = 0L,
        isEphemeral = false,
        vanishDeadline = null
    )
}
