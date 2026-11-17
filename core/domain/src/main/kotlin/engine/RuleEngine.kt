// engine/RuleEngine.kt
package com.example.dialer.core.domain.engine

import com.example.dialer.core.domain.model.DialContext
import com.example.dialer.core.domain.model.TransformResult

interface RuleEngine {
    fun transform(context: DialContext): TransformResult
}