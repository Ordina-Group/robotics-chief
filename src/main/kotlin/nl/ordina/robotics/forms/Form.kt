package nl.ordina.robotics.forms

import kotlinx.serialization.Serializable

@Serializable
data class Form(
    val url: String,
    val template: String,
    val method: String = "POST",
)
