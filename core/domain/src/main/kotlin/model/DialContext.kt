// model/DialContext.kt
package com.example.dialer.core.domain.model

data class DialContext(
    val number: String,
    val simSlot: Int?,
    val contactLabels: Set<String>
)