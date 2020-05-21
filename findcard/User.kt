package kz.diploma.findcard

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    var id: String? = "",
    var name: String = "",
    var email: String = "",
    var phone: String = "",
    var profit: Int = 0
)