// engine/ConditionMatcher.kt
package com.example.dialer.core.domain.engine

import com.example.dialer.core.domain.model.DialContext
import com.example.dialer.core.domain.model.RuleCondition

interface ConditionMatcher {
    fun matches(condition: RuleCondition, context: DialContext): Boolean
    fun matchesAll(conditions: List<RuleCondition>, context: DialContext): Boolean
}