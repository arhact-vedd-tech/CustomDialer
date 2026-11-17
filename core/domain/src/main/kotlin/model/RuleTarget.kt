// model/RuleTarget.kt
package com.example.dialer.core.domain.model

data class RuleTarget(
    val ruleId: Long,
    val type: TargetType,
    val value: String
)