package com.ixeken.nepo.core.mathematics.di

import com.ixeken.nepo.core.mathematics.data.Exp4jMathEngine
import com.ixeken.nepo.core.mathematics.domain.MathEngine

/**
 * Dependency injection module and factory for mathematics-related instances.
 */
object MathModule {
    /**
     * Provides the active implementation of the [MathEngine].
     *
     * @return An instance of [MathEngine] (specifically [Exp4jMathEngine]).
     */
    fun provideMathEngine(): MathEngine {
        return Exp4jMathEngine()
    }
}
