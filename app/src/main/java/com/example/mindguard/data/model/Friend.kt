package com.example.mindguard.data.model

import kotlinx.serialization.*

@Serializable
data class Friend(
    val name:String,
    val uuid:String
)