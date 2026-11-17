// model/RuleCondition.kt
package com.example.dialer.core.domain.model

data class RuleCondition(
    val type: ConditionType,
    val value: String
)

enum class ConditionType { LENGTH, STARTS_WITH, LABEL }