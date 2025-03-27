package com.amnabatool.assignment_2

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Message(
    val text: String,
    val isSentByUser: Boolean,
    val time: String
) : Parcelable
