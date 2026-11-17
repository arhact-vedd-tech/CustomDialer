// transform/NumberTransformer.kt
package com.example.dialer.core.domain.transform

interface NumberTransformer {
    fun apply(number: String, stripCount: Int?, prependValue: String?): String
}