package com.rodrigo.androidapp.futtrack.domain.model

import com.google.firebase.firestore.PropertyName

data class UserProfile(
    var uid: String = "",
    @get:PropertyName("isAdmin")
    @set:PropertyName("isAdmin")
    var isAdmin: Boolean = false
)