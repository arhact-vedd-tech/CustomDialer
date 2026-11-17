// transform/DefaultNumberTransformer.kt
package com.example.dialer.core.domain.transform

class DefaultNumberTransformer : NumberTransformer {
    override fun apply(number: String, stripCount: Int?, prependValue: String?): String {
        val stripped = stripCount?.let { number.drop(it) } ?: number
        return (prependValue ?: "") + stripped
    }
}