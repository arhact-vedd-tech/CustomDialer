// engine/DefaultRuleEngine.kt
package com.example.dialer.core.domain.engine

import com.example.dialer.core.domain.model.DialContext
import com.example.dialer.core.domain.model.Exception
import com.example.dialer.core.domain.model.Rule
import com.example.dialer.core.domain.model.RuleSnapshot
import com.example.dialer.core.domain.model.RuleTarget
import com.example.dialer.core.domain.model.TargetType
import com.example.dialer.core.domain.model.TransformResult
import com.example.dialer.core.domain.transform.NumberTransformer

class DefaultRuleEngine(
    snapshot: RuleSnapshot,
    private val transformer: NumberTransformer = DefaultNumberTransformer(),
    private val conditionMatcher: ConditionMatcher = DefaultConditionMatcher()
) : RuleEngine {

    private val rules: List<Rule> = snapshot.rules
        .filter { it.enabled }
        .sortedBy { it.priority }

    private val whitelist: List<RuleTarget> = snapshot.whitelist
    private val exceptions: List<Exception> = snapshot.exceptions

    override fun transform(context: DialContext): TransformResult {
        if (isExcepted(context)) {
            return TransformResult.Unchanged(context.number)
        }

        val forcedRule = findWhitelistedRule(context)
        val rule = forcedRule ?: findFirstMatchingRule(context)

        if (rule == null || !simSlotCompatible(rule, context)) {
            return TransformResult.Unchanged(context.number)
        }

        val dialed = transformer.apply(context.number, rule.stripCount, rule.prependValue)
        return TransformResult.Transformed(dialedNumber = dialed, appliedRuleId = rule.id)
    }

    private fun isExcepted(context: DialContext): Boolean =
        exceptions.any { matchesTarget(it.type, it.value, context) }

    private fun findWhitelistedRule(context: DialContext): Rule? {
        val matchedTarget = whitelist.firstOrNull { matchesTarget(it.type, it.value, context) }
            ?: return null
        return rules.firstOrNull { it.id == matchedTarget.ruleId }
    }

    private fun findFirstMatchingRule(context: DialContext): Rule? =
        rules.firstOrNull { rule ->
            simSlotCompatible(rule, context) &&
                conditionMatcher.matchesAll(rule.conditions, context)
        }

    private fun simSlotCompatible(rule: Rule, context: DialContext): Boolean {
        if (rule.simSlot == null) return true
        return rule.simSlot == context.simSlot
    }

    private fun matchesTarget(type: TargetType, value: String, context: DialContext): Boolean =
        when (type) {
            TargetType.NUMBER -> context.number == value
            TargetType.LABEL -> value in context.contactLabels
        }
}