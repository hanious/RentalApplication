package com.example.rentalapplication

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Item(
    val name: String,
    val rating: Float,
    var attribute: String = "", //default value
    val price: Int,
    val imageResId: Int,
    var isBorrowed: Boolean = false //default value
) : Parcelable