// test/.../engine/DefaultRuleEngineTest.kt
package com.example.dialer.core.domain.engine

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.example.dialer.core.domain.model.ConditionType
import com.example.dialer.core.domain.model.DialContext
import com.example.dialer.core.domain.model.Exception
import com.example.dialer.core.domain.model.Rule
import com.example.dialer.core.domain.model.RuleCondition
import com.example.dialer.core.domain.model.RuleSnapshot
import com.example.dialer.core.domain.model.RuleTarget
import com.example.dialer.core.domain.model.TargetType
import com.example.dialer.core.domain.model.TransformResult
import com.example.dialer.core.domain.transform.DefaultNumberTransformer
import org.junit.jupiter.api.Test

class DefaultRuleEngineTest {

    private val context = DialContext(
        number = "89991234567",
        simSlot = 0,
        contactLabels = setOf("Работа")
    )

    private fun rule(
        id: Long,
        priority: Int,
        strip: Int? = null,
        prepend: String? = null,
        simSlot: Int? = null,
        enabled: Boolean = true,
        conditions: List<RuleCondition> = emptyList()
    ) = Rule(id, "rule$id", enabled, priority, strip, prepend, simSlot, conditions)

    private fun engine(
        rules: List<Rule>,
        whitelist: List<RuleTarget> = emptyList(),
        exceptions: List<Exception> = emptyList()
    ) = DefaultRuleEngine(
        snapshot = RuleSnapshot(rules, whitelist, exceptions),
        transformer = DefaultNumberTransformer()
    )

    @Test
    fun `first match wins by priority`() {
        val rules = listOf(
            rule(id = 1, priority = 2, strip = 1, prepend = "9"),
            rule(id = 2, priority = 1, strip = 1, prepend = "8")
        )
        val result = engine(rules).transform(context)
        assertThat(result).isInstanceOf(TransformResult.Transformed::class)
        assertThat((result as TransformResult.Transformed).appliedRuleId).isEqualTo(2L)
        assertThat(result.dialedNumber).isEqualTo("8991234567")
    }

    @Test
    fun `exception blocks all rules`() {
        val rules = listOf(rule(id = 1, priority = 1, strip = 1, prepend = "9"))
        val exceptions = listOf(
            Exception(1, TargetType.NUMBER, "89991234567", locked = true)
        )
        val result = engine(rules, exceptions = exceptions).transform(context)
        assertThat(result).isEqualTo(TransformResult.Unchanged("89991234567"))
    }

    @Test
    fun `exception by label`() {
        val rules = listOf(rule(id = 1, priority = 1, strip = 1, prepend = "9"))
        val exceptions = listOf(
            Exception(1, TargetType.LABEL, "Работа", locked = false)
        )
        val result = engine(rules, exceptions = exceptions).transform(context)
        assertThat(result).isEqualTo(TransformResult.Unchanged("89991234567"))
    }

    @Test
    fun `whitelist overrides priority`() {
        val rules = listOf(
            rule(id = 1, priority = 1, strip = 1, prepend = "8"),
            rule(id = 2, priority = 2, strip = 1, prepend = "9")
        )
        val whitelist = listOf(RuleTarget(ruleId = 2, TargetType.NUMBER, "89991234567"))
        val result = engine(rules, whitelist = whitelist).transform(context)
        assertThat(result).isInstanceOf(TransformResult.Transformed::class)
        assertThat((result as TransformResult.Transformed).appliedRuleId).isEqualTo(2L)
    }

    @Test
    fun `whitelist respects sim slot`() {
        val rules = listOf(rule(id = 1, priority = 1, strip = 1, prepend = "9", simSlot = 1))
        val whitelist = listOf(RuleTarget(ruleId = 1, TargetType.NUMBER, "89991234567"))
        val result = engine(rules, whitelist = whitelist).transform(context)
        assertThat(result).isEqualTo(TransformResult.Unchanged("89991234567"))
    }

    @Test
    fun `disabled rule skipped`() {
        val rules = listOf(rule(id = 1, priority = 1, strip = 1, prepend = "9", enabled = false))
        val result = engine(rules).transform(context)
        assertThat(result).isEqualTo(TransformResult.Unchanged("89991234567"))
    }

    @Test
    fun `no match returns unchanged`() {
        val rules = listOf(
            rule(id = 1, priority = 1, strip = 1, prepend = "9",
                conditions = listOf(RuleCondition(ConditionType.STARTS_WITH, "7")))
        )
        val result = engine(rules).transform(context)
        assertThat(result).isEqualTo(TransformResult.Unchanged("89991234567"))
    }

    @Test
    fun `all conditions must match`() {
        val rules = listOf(
            rule(id = 1, priority = 1, strip = 1, prepend = "9",
                conditions = listOf(
                    RuleCondition(ConditionType.LENGTH, "11"),
                    RuleCondition(ConditionType.LABEL, "Работа")
                ))
        )
        val result = engine(rules).transform(context)
        assertThat(result).isInstanceOf(TransformResult.Transformed::class)
    }

    @Test
    fun `sim slot null in context fails safe`() {
        val nullSimContext = context.copy(simSlot = null)
        val rules = listOf(rule(id = 1, priority = 1, strip = 1, prepend = "9", simSlot = 0))
        val result = engine(rules).transform(nullSimContext)
        assertThat(result).isEqualTo(TransformResult.Unchanged("89991234567"))
    }

    @Test
    fun `rule with sim slot matching context applies`() {
        val rules = listOf(rule(id = 1, priority = 1, strip = 1, prepend = "9", simSlot = 0))
        val result = engine(rules).transform(context)
        assertThat(result).isInstanceOf(TransformResult.Transformed::class)
    }
}