package com.ixeken.nepo.core.mathematics.domain

/**
 * Representation of the outcome of a mathematical evaluation.
 */
sealed interface MathResult {
    /**
     * Successful evaluation.
     *
     * @property value The resulting numerical value of the expression.
     */
    data class Success(val value: Double) : MathResult

    /**
     * Failed evaluation.
     *
     * @property errorType The type of computation error encountered.
     */
    data class Error(val errorType: MathErrorType) : MathResult
}

/**
 * Enumeration of potential math computation errors.
 */
enum class MathErrorType {
    /** Attempted division by zero. */
    DIVISION_BY_ZERO,
    /** The expression could not be parsed or contains syntax errors. */
    INVALID_EXPRESSION,
    /** The result of the expression is undefined (e.g. NaN). */
    UNDEFINED_RESULT,
    /** Mathematical domain violation (e.g. negative root, division by zero, invalid logarithm arguments). */
    DOMAIN_ERROR
}

/**
 * Mathematical evaluation engine interface.
 *
 * Serves as the primary contract for evaluating raw string-based mathematical expressions.
 */
interface MathEngine {
    /**
     * Evaluates a mathematical expression and returns the result.
     *
     * @param expression The mathematical expression to evaluate.
     * @param isDegreeMode Whether to evaluate trigonometric functions in degrees instead of radians.
     * @return [MathResult] containing either the evaluation success or error.
     */
    fun evaluate(expression: String, isDegreeMode: Boolean = false): MathResult
}
