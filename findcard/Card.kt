package kz.diploma.findcard

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Card (
    var id: String? = null,
    val number: String,
    val cardHolder: String,
    var date: String,
    var founderId: String?
)