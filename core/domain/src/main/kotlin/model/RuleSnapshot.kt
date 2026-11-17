// model/RuleSnapshot.kt
package com.example.dialer.core.domain.model

data class RuleSnapshot(
    val rules: List<Rule>,
    val whitelist: List<RuleTarget>,
    val exceptions: List<Exception>
)