// model/TransformResult.kt
package com.example.dialer.core.domain.model

sealed interface TransformResult {
    data class Transformed(
        val dialedNumber: String,
        val appliedRuleId: Long?
    ) : TransformResult

    data class Unchanged(val originalNumber: String) : TransformResult
}