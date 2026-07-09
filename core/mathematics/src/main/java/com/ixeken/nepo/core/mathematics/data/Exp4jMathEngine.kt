package com.ixeken.nepo.core.mathematics.data

import com.ixeken.nepo.core.mathematics.domain.MathEngine
import com.ixeken.nepo.core.mathematics.domain.MathErrorType
import com.ixeken.nepo.core.mathematics.domain.MathResult
import net.objecthunter.exp4j.ExpressionBuilder
import net.objecthunter.exp4j.function.Function
import java.lang.ArithmeticException

private val degSin = object : Function("sin", 1) {
    override fun apply(vararg args: Double): Double = Math.sin(Math.toRadians(args[0]))
}
private val degCos = object : Function("cos", 1) {
    override fun apply(vararg args: Double): Double = Math.cos(Math.toRadians(args[0]))
}
private val degTan = object : Function("tan", 1) {
    override fun apply(vararg args: Double): Double = Math.tan(Math.toRadians(args[0]))
}
private val degAsin = object : Function("asin", 1) {
    override fun apply(vararg args: Double): Double = Math.toDegrees(Math.asin(args[0]))
}
private val degAcos = object : Function("acos", 1) {
    override fun apply(vararg args: Double): Double = Math.toDegrees(Math.acos(args[0]))
}
private val degAtan = object : Function("atan", 1) {
    override fun apply(vararg args: Double): Double = Math.toDegrees(Math.atan(args[0]))
}

/**
 * Concrete implementation of [MathEngine] using the third-party exp4j library.
 *
 * This class has internal visibility to hide implementation details from consumer modules.
 */
internal class Exp4jMathEngine : MathEngine {
    
    override fun evaluate(expression: String, isDegreeMode: Boolean): MathResult {
        return try {
            val sanitizedExpression = sanitize(expression)
            
            if (sanitizedExpression.isEmpty()) {
                return MathResult.Success(0.0)
            }

            val builder = ExpressionBuilder(sanitizedExpression)
            if (isDegreeMode) {
                builder.functions(degSin, degCos, degTan, degAsin, degAcos, degAtan)
            }
            val exp4jExpression = builder.build()
            val evaluationResult = exp4jExpression.evaluate()

            when {
                evaluationResult.isInfinite() -> {
                    if (sanitizedExpression.contains("log") || sanitizedExpression.contains("sqrt")) {
                        MathResult.Error(MathErrorType.DOMAIN_ERROR)
                    } else {
                        MathResult.Error(MathErrorType.DIVISION_BY_ZERO)
                    }
                }
                evaluationResult.isNaN() -> {
                    if (sanitizedExpression.contains("log") || sanitizedExpression.contains("sqrt")) {
                        MathResult.Error(MathErrorType.DOMAIN_ERROR)
                    } else {
                        MathResult.Error(MathErrorType.UNDEFINED_RESULT)
                    }
                }
                else -> MathResult.Success(evaluationResult)
            }
        } catch (e: ArithmeticException) {
            if (expression.contains("log") || expression.contains("ln") || expression.contains("√")) {
                MathResult.Error(MathErrorType.DOMAIN_ERROR)
            } else {
                MathResult.Error(MathErrorType.DIVISION_BY_ZERO)
            }
        } catch (e: IllegalArgumentException) {
            if (expression.contains("log") || expression.contains("ln") || expression.contains("√")) {
                MathResult.Error(MathErrorType.DOMAIN_ERROR)
            } else {
                MathResult.Error(MathErrorType.INVALID_EXPRESSION)
            }
        } catch (e: Exception) {
            MathResult.Error(MathErrorType.INVALID_EXPRESSION)
        }
    }

    /**
     * Maps visual user interface characters to standard arithmetic operators.
     *
     * @param expression The raw user input expression.
     * @return Sanitized mathematical expression string.
     */
    private fun sanitize(expression: String): String {
        val basicSanitized = expression
            .replace("×", "*")
            .replace("÷", "/")
            .replace("π", "pi")
            .replace("²√(", "sqrt(")
            .replace("√(", "sqrt(")
            .replace("^", "^")
            .replace("log(", "log10(")
            .replace("ln(", "log(")
        
        val withEuler = basicSanitized.replace(Regex("(?<![a-zA-Z])e(?![a-zA-Z])"), "2.718281828459045")
        
        return resolvePercentages(withEuler)
    }

    /**
     * Pre-processes percentage expressions (e.g., '100 - 10%') into standard math equivalents.
     */
    private fun resolvePercentages(expr: String): String {
        var s = expr
        while (s.contains("%")) {
            val index = s.indexOf("%")
            val bStart = findOperandStart(s, index - 1)
            if (bStart < 0) {
                s = s.replaceFirst("%", "/100.0")
                continue
            }
            val b = s.substring(bStart, index)
            
            var opIndex = bStart - 1
            while (opIndex >= 0 && s[opIndex].isWhitespace()) {
                opIndex--
            }
            
            if (opIndex >= 0 && (s[opIndex] == '+' || s[opIndex] == '-')) {
                val op = s[opIndex]
                val aStart = 0
                val a = s.substring(aStart, opIndex).trim()
                if (a.isNotEmpty()) {
                    // additive percentage: A + B% -> A + (A * B / 100.0)
                    val replacement = "($a) * ($b) / 100.0"
                    s = s.substring(0, opIndex + 1) + "($replacement)" + s.substring(index + 1)
                } else {
                    // fallback: single value percentage
                    s = s.substring(0, bStart) + "(($b) / 100.0)" + s.substring(index + 1)
                }
            } else {
                // multiplicative/divisive/single value percentage: B% -> B / 100.0
                s = s.substring(0, bStart) + "(($b) / 100.0)" + s.substring(index + 1)
            }
        }
        return s
    }

    /**
     * Backtracks to find the beginning of a numeric operand or grouped expression.
     */
    private fun findOperandStart(s: String, endIndex: Int): Int {
        var i = endIndex
        while (i >= 0 && s[i].isWhitespace()) {
            i--
        }
        if (i < 0) return -1
        
        if (s[i] == ')') {
            var balance = 1
            i--
            while (i >= 0 && balance > 0) {
                if (s[i] == ')') balance++
                else if (s[i] == '(') balance--
                i--
            }
            while (i >= 0 && s[i].isLetter()) {
                i--
            }
            return i + 1
        }
        
        while (i >= 0 && (s[i].isLetterOrDigit() || s[i] == '.')) {
            i--
        }
        return i + 1
    }
}
