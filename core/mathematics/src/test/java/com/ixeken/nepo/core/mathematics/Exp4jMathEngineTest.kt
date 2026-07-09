package com.ixeken.nepo.core.mathematics

import com.ixeken.nepo.core.mathematics.data.Exp4jMathEngine
import com.ixeken.nepo.core.mathematics.domain.MathErrorType
import com.ixeken.nepo.core.mathematics.domain.MathResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [Exp4jMathEngine] to verify math parsing and error handling.
 */
class Exp4jMathEngineTest {

    private lateinit var sut: Exp4jMathEngine

    /**
     * Initializes the system under test before each test run.
     */
    @Before
    fun setUp() {
        sut = Exp4jMathEngine()
    }

    /**
     * Verifies that standard arithmetic operations (addition, subtraction, multiplication, division)
     * are evaluated successfully with accurate values.
     */
    @Test
    fun evaluateStandardOperationsReturnsSuccess() {
        // Given
        val expressions = listOf("2+2", "10-5", "4×5", "20÷4")
        val expectedValues = listOf(4.0, 5.0, 20.0, 5.0)

        // When & Then
        expressions.forEachIndexed { index, expr ->
            val result = sut.evaluate(expr)
            assertTrue(result is MathResult.Success)
            assertEquals(expectedValues[index], (result as MathResult.Success).value, 0.001)
        }
    }

    /**
     * Verifies that combined operations respect standard mathematical operator precedence (BEDMAS/PEMDAS).
     */
    @Test
    fun evaluateCombinedArithmeticExpressionsRespectsOperatorPrecedence() {
        // Given
        val expression = "2+3×4"
        val expected = 14.0

        // When
        val result = sut.evaluate(expression)

        // Then
        assertTrue(result is MathResult.Success)
        assertEquals(expected, (result as MathResult.Success).value, 0.001)
    }

    /**
     * Verifies that dividing a number by zero triggers a DIVISION_BY_ZERO error type.
     */
    @Test
    fun evaluateDivisionByZeroReturnsDivisionByZeroError() {
        // Given
        val expression = "10÷0"

        // When
        val result = sut.evaluate(expression)

        // Then
        assertTrue(result is MathResult.Error)
        assertEquals(MathErrorType.DIVISION_BY_ZERO, (result as MathResult.Error).errorType)
    }

    /**
     * Verifies that incomplete or syntactically incorrect math inputs trigger an INVALID_EXPRESSION error.
     */
    @Test
    fun evaluateIncompleteOrMalformedExpressionReturnsInvalidExpressionError() {
        // Given
        val expressions = listOf("2+", "((5-3", "5+*2")

        // When & Then
        expressions.forEach { expr ->
            val result = sut.evaluate(expr)
            assertTrue(result is MathResult.Error)
            assertEquals(MathErrorType.INVALID_EXPRESSION, (result as MathResult.Error).errorType)
        }
    }

    /**
     * Verifies that scientific operators (power, root, logs, pi constant) are correctly evaluated.
     */
    @Test
    fun evaluateScientificOperationsReturnsSuccess() {
        // Given
        val expressions = listOf("2^3", "√(16)", "log(100)", "ln(2.718281828459)", "π×2")
        val expectedValues = listOf(8.0, 4.0, 2.0, 1.0, Math.PI * 2)

        // When & Then
        expressions.forEachIndexed { index, expr ->
            val result = sut.evaluate(expr)
            assertTrue("Failed on expression: $expr", result is MathResult.Success)
            assertEquals(expectedValues[index], (result as MathResult.Success).value, 0.001)
        }
    }

    /**
     * Verifies that mathematical domain violations (negative root, non-positive logs) return DOMAIN_ERROR.
     */
    @Test
    fun evaluateDomainViolationsReturnsDomainError() {
        // Given
        val expressions = listOf("√(-4)", "log(-10)", "log(0)", "ln(-1)")

        // When & Then
        expressions.forEach { expr ->
            val result = sut.evaluate(expr)
            assertTrue("Failed on expression: $expr", result is MathResult.Error)
            assertEquals(MathErrorType.DOMAIN_ERROR, (result as MathResult.Error).errorType)
        }
    }

    /**
     * Verifies that percentage operations (both standalone and additive) return successful, accurate values.
     */
    @Test
    fun evaluatePercentageOperationsReturnsSuccess() {
        // Given
        val expressions = listOf("10%", "100-10%", "100+15%", "50×20%", "100-10%-10%", "√(100)%")
        val expectedValues = listOf(0.10, 90.0, 115.0, 10.0, 81.0, 0.10)

        // When & Then
        expressions.forEachIndexed { index, expr ->
            val result = sut.evaluate(expr)
            assertTrue("Failed on expression: $expr", result is MathResult.Success)
            assertEquals(expectedValues[index], (result as MathResult.Success).value, 0.001)
        }
    }

    /**
     * Verifies that the Euler constant 'e' is processed correctly.
     */
    @Test
    fun evaluateEulerConstantReturnsSuccess() {
        // Given
        val expressions = listOf("e", "e^1", "e×2")
        val expectedValues = listOf(Math.E, Math.E, Math.E * 2)

        // When & Then
        expressions.forEachIndexed { index, expr ->
            val result = sut.evaluate(expr)
            assertTrue("Failed on expression: $expr", result is MathResult.Success)
            assertEquals(expectedValues[index], (result as MathResult.Success).value, 0.001)
        }
    }

    /**
     * Verifies that trigonometric operations are evaluated in Degrees when isDegreeMode is true,
     * and in Radians when isDegreeMode is false.
     */
    @Test
    fun evaluateTrigonometricAndInverseOperationsWithAngleModes() {
        // RADIAN Mode (default)
        val radResultSin = sut.evaluate("sin(π÷2)", isDegreeMode = false)
        assertTrue(radResultSin is MathResult.Success)
        assertEquals(1.0, (radResultSin as MathResult.Success).value, 0.001)

        val radResultAsin = sut.evaluate("asin(1)", isDegreeMode = false)
        assertTrue(radResultAsin is MathResult.Success)
        assertEquals(Math.PI / 2, (radResultAsin as MathResult.Success).value, 0.001)

        // DEGREE Mode
        val degResultSin = sut.evaluate("sin(90)", isDegreeMode = true)
        assertTrue(degResultSin is MathResult.Success)
        assertEquals(1.0, (degResultSin as MathResult.Success).value, 0.001)

        val degResultCos = sut.evaluate("cos(180)", isDegreeMode = true)
        assertTrue(degResultCos is MathResult.Success)
        assertEquals(-1.0, (degResultCos as MathResult.Success).value, 0.001)

        val degResultAsin = sut.evaluate("asin(1)", isDegreeMode = true)
        assertTrue(degResultAsin is MathResult.Success)
        assertEquals(90.0, (degResultAsin as MathResult.Success).value, 0.001)
    }
}
