// engine/DefaultConditionMatcher.kt
package com.example.dialer.core.domain.engine

import com.example.dialer.core.domain.model.ConditionType
import com.example.dialer.core.domain.model.DialContext
import com.example.dialer.core.domain.model.RuleCondition

class DefaultConditionMatcher : ConditionMatcher {

    override fun matchesAll(conditions: List<RuleCondition>, context: DialContext): Boolean {
        if (conditions.isEmpty()) return true
        return conditions.all { matches(it, context) }
    }

    override fun matches(condition: RuleCondition, context: DialContext): Boolean =
        when (condition.type) {
            ConditionType.LENGTH -> context.number.length == condition.value.toIntOrNull()
            ConditionType.STARTS_WITH -> context.number.startsWith(condition.value)
            ConditionType.LABEL -> condition.value in context.contactLabels
        }
}