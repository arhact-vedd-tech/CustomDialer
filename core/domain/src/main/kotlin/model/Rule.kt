// model/Rule.kt
package com.example.dialer.core.domain.model

data class Rule(
    val id: Long,
    val name: String,
    val enabled: Boolean,
    val priority: Int,
    val stripCount: Int?,
    val prependValue: String?,
    val simSlot: Int?,
    val conditions: List<RuleCondition>
) {
    init {
        require(stripCount != null || prependValue != null) {
            "Rule $id must have at least one action: stripCount or prependValue"
        }
        require(stripCount == null || stripCount >= 0) {
            "Rule $id: stripCount must be >= 0, was $stripCount"
        }
        require(prependValue == null || prependValue.isNotEmpty()) {
            "Rule $id: prependValue must not be empty string"
        }
    }
}