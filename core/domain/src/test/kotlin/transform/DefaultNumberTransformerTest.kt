// test/.../transform/DefaultNumberTransformerTest.kt
package com.example.dialer.core.domain.transform

import assertk.assertThat
import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test

class DefaultNumberTransformerTest {

    private val transformer = DefaultNumberTransformer()

    @Test
    fun `strip only`() {
        assertThat(transformer.apply("89991234567", 1, null))
            .isEqualTo("9991234567")
    }

    @Test
    fun `prepend only`() {
        assertThat(transformer.apply("9991234567", null, "8"))
            .isEqualTo("89991234567")
    }

    @Test
    fun `strip then prepend`() {
        assertThat(transformer.apply("89991234567", 1, "9"))
            .isEqualTo("99991234567")
    }

    @Test
    fun `strip more than length returns empty then prepend`() {
        assertThat(transformer.apply("123", 5, "9"))
            .isEqualTo("9")
    }
}