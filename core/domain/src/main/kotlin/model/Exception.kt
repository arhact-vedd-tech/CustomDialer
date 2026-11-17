// model/Exception.kt
package com.example.dialer.core.domain.model

data class Exception(
    val id: Long,
    val type: TargetType,
    val value: String,
    val locked: Boolean
)

enum class TargetType { NUMBER, LABEL }