// test/.../engine/DefaultConditionMatcherTest.kt
package com.example.dialer.core.domain.engine

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.example.dialer.core.domain.model.ConditionType
import com.example.dialer.core.domain.model.DialContext
import com.example.dialer.core.domain.model.RuleCondition
import org.junit.jupiter.api.Test

class DefaultConditionMatcherTest {

    private val matcher = DefaultConditionMatcher()
    private val context = DialContext(
        number = "89991234567",
        simSlot = 0,
        contactLabels = setOf("Работа", "VIP")
    )

    @Test
    fun `length matches`() {
        val c = RuleCondition(ConditionType.LENGTH, "11")
        assertThat(matcher.matches(c, context)).isTrue()
    }

    @Test
    fun `length mismatch`() {
        val c = RuleCondition(ConditionType.LENGTH, "10")
        assertThat(matcher.matches(c, context)).isFalse()
    }

    @Test
    fun `starts with matches`() {
        val c = RuleCondition(ConditionType.STARTS_WITH, "899")
        assertThat(matcher.matches(c, context)).isTrue()
    }

    @Test
    fun `label matches`() {
        val c = RuleCondition(ConditionType.LABEL, "Работа")
        assertThat(matcher.matches(c, context)).isTrue()
    }

    @Test
    fun `label mismatch`() {
        val c = RuleCondition(ConditionType.LABEL, "Личное")
        assertThat(matcher.matches(c, context)).isFalse()
    }

    @Test
    fun `invalid length value does not match`() {
        val c = RuleCondition(ConditionType.LENGTH, "abc")
        assertThat(matcher.matches(c, context)).isFalse()
    }

    @Test
    fun `empty conditions match all`() {
        assertThat(matcher.matchesAll(emptyList(), context)).isTrue()
    }

    @Test
    fun `all conditions must match`() {
        val conditions = listOf(
            RuleCondition(ConditionType.LENGTH, "11"),
            RuleCondition(ConditionType.STARTS_WITH, "899"),
            RuleCondition(ConditionType.LABEL, "Работа")
        )
        assertThat(matcher.matchesAll(conditions, context)).isTrue()
    }

    @Test
    fun `one failed condition breaks AND`() {
        val conditions = listOf(
            RuleCondition(ConditionType.LENGTH, "11"),
            RuleCondition(ConditionType.LABEL, "Личное")
        )
        assertThat(matcher.matchesAll(conditions, context)).isFalse()
    }
}