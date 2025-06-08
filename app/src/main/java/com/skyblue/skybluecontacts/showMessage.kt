package com.skyblue.skybluecontacts

import android.app.Activity
import android.graphics.Color
import com.google.android.material.snackbar.Snackbar

fun Activity.showMessage(message: String) {
    val snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT)
    snackbar.setBackgroundTint(getColor(R.color.primary))
    snackbar.setTextColor(Color.WHITE)
    snackbar.show()
}