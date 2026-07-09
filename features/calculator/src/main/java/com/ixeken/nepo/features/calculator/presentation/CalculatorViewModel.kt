package com.ixeken.nepo.features.calculator.presentation

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ixeken.nepo.core.mathematics.domain.MathEngine
import com.ixeken.nepo.core.mathematics.domain.MathResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Presentation coordinator for the calculator screen.
 *
 * Implements MVI unidirectional data flow by receiving [CalculatorUserEvent] and producing [CalculatorUiState].
 *
 * @property mathEngine The [MathEngine] used to compute mathematical expressions.
 */
class CalculatorViewModel(
    private val mathEngine: MathEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow<CalculatorUiState>(CalculatorUiState.Empty)
    /** State flow containing the active UI state. */
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    private val _isDegreeMode = MutableStateFlow(false)
    /** State flow representing whether degree mode (DEG) is active. */
    val isDegreeMode: StateFlow<Boolean> = _isDegreeMode.asStateFlow()

    private val _isInversedMode = MutableStateFlow(false)
    /** State flow representing whether inverse functions mode (INV) is active. */
    val isInversedMode: StateFlow<Boolean> = _isInversedMode.asStateFlow()

    private var currentExpression = TextFieldValue("")

    /**
     * Dispatcher method to process actions initiated by the user.
     *
     * @param event The [CalculatorUserEvent] representing user interaction.
     */
    fun onEvent(event: CalculatorUserEvent) {
        when (event) {
            is CalculatorUserEvent.OnKeyPress -> handleKeyPress(event.key)
            is CalculatorUserEvent.OnClearAll -> handleClearAll()
            is CalculatorUserEvent.OnDeleteSingle -> handleDeleteSingle()
            is CalculatorUserEvent.OnEvaluate -> handleEvaluate()
            is CalculatorUserEvent.OnExpressionValueChanged -> handleExpressionValueChanged(event.newValue)
            is CalculatorUserEvent.OnToggleDegreeMode -> handleToggleDegreeMode()
            is CalculatorUserEvent.OnToggleInversedMode -> handleToggleInversedMode()
        }
    }

    private fun handleToggleDegreeMode() {
        _isDegreeMode.value = !_isDegreeMode.value
        updateWhileTypingState()
    }

    private fun handleToggleInversedMode() {
        _isInversedMode.value = !_isInversedMode.value
    }

    private fun handleExpressionValueChanged(newValue: TextFieldValue) {
        currentExpression = newValue
        updateWhileTypingState()
    }

    private fun handleKeyPress(key: String) {
        if (key == "±") {
            val toggledText = toggleLastNumberSign(currentExpression.text)
            currentExpression = TextFieldValue(toggledText, TextRange(toggledText.length))
            if (currentExpression.text.isEmpty() || currentExpression.text == "-") {
                _uiState.value = CalculatorUiState.WhileTyping(
                    expression = currentExpression,
                    partialResult = ""
                )
            } else {
                updateWhileTypingState()
            }
        } else if (key == "()") {
            val selection = currentExpression.selection
            val textBeforeCursor = currentExpression.text.substring(0, selection.start)
            val openCount = textBeforeCursor.count { it == '(' }
            val closeCount = textBeforeCursor.count { it == ')' }
            val unclosed = openCount - closeCount
            val lastChar = textBeforeCursor.lastOrNull()
            
            val shouldClose = unclosed > 0 && lastChar != null && lastChar != '(' && lastChar !in listOf('+', '-', '×', '*', '/', '÷', '^', '%', '√', '²')
            
            val insertKey = if (shouldClose) ")" else "("
            currentExpression = currentExpression.insertText(insertKey)
            updateWhileTypingState()
        } else {
            currentExpression = currentExpression.insertText(key)
            updateWhileTypingState()
        }
    }

    private fun TextFieldValue.insertText(textToInsert: String): TextFieldValue {
        val selection = this.selection
        val text = this.text
        val newText = text.substring(0, selection.start) + textToInsert + text.substring(selection.end)
        val newSelectionIndex = selection.start + textToInsert.length
        return TextFieldValue(
            text = newText,
            selection = TextRange(newSelectionIndex)
        )
    }

    private fun toggleLastNumberSign(expression: String): String {
        if (expression.isEmpty()) return "-"
        
        // Find the end index of the last number or pi
        var end = expression.length - 1
        while (end >= 0 && !expression[end].isDigit() && expression[end] != '.' && expression[end] != 'π') {
            end--
        }
        
        if (end < 0) {
            return expression + "-"
        }
        
        // Find the start of this number
        var start = end
        while (start > 0 && (expression[start - 1].isDigit() || expression[start - 1] == '.' || expression[start - 1] == 'π')) {
            start--
        }
        
        // Check if there is a sign before this number
        if (start > 0) {
            val prevChar = expression[start - 1]
            if (prevChar == '-') {
                // Is it unary minus or subtraction?
                val isUnary = start - 1 == 0 || (expression[start - 2] in listOf('+', '-', '×', '*', '/', '÷', '(', '^', '%', '√', '²'))
                if (isUnary) {
                    return expression.substring(0, start - 1) + expression.substring(start)
                } else {
                    return expression.substring(0, start - 1) + "+" + expression.substring(start)
                }
            } else if (prevChar == '+') {
                val isUnary = start - 1 == 0 || (expression[start - 2] in listOf('+', '-', '×', '*', '/', '÷', '(', '^', '%', '√', '²'))
                if (isUnary) {
                    return expression.substring(0, start - 1) + "-" + expression.substring(start)
                } else {
                    return expression.substring(0, start - 1) + "-" + expression.substring(start)
                }
            } else {
                return expression.substring(0, start) + "-" + expression.substring(start)
            }
        } else {
            return "-" + expression
        }
    }

    private fun handleClearAll() {
        currentExpression = TextFieldValue("")
        _uiState.value = CalculatorUiState.Empty
    }

    private fun handleDeleteSingle() {
        if (currentExpression.text.isNotEmpty()) {
            currentExpression = currentExpression.deleteSingle()
            if (currentExpression.text.isEmpty()) {
                _uiState.value = CalculatorUiState.Empty
            } else {
                updateWhileTypingState()
            }
        }
    }

    private fun TextFieldValue.deleteSingle(): TextFieldValue {
        val selection = this.selection
        val text = this.text
        if (selection.start != selection.end) {
            val newText = text.substring(0, selection.start) + text.substring(selection.end)
            return TextFieldValue(
                text = newText,
                selection = TextRange(selection.start)
            )
        } else if (selection.start > 0) {
            val newText = text.substring(0, selection.start - 1) + text.substring(selection.start)
            return TextFieldValue(
                text = newText,
                selection = TextRange(selection.start - 1)
            )
        }
        return this
    }

    private fun handleEvaluate() {
        if (currentExpression.text.isEmpty()) return
        viewModelScope.launch {
            when (val result = mathEngine.evaluate(currentExpression.text, _isDegreeMode.value)) {
                is MathResult.Success -> {
                    val resultText = formatOutput(result.value)
                    _uiState.value = CalculatorUiState.AfterEqual(
                        finalResult = resultText,
                        originalExpression = currentExpression
                    )
                    currentExpression = TextFieldValue(resultText, TextRange(resultText.length))
                }
                is MathResult.Error -> {
                    _uiState.value = CalculatorUiState.AfterEqual(
                        finalResult = "Error",
                        originalExpression = currentExpression
                    )
                }
            }
        }
    }

    private fun updateWhileTypingState() {
        viewModelScope.launch {
            val partial = when (val result = mathEngine.evaluate(currentExpression.text, _isDegreeMode.value)) {
                is MathResult.Success -> formatOutput(result.value)
                is MathResult.Error -> ""
            }
            _uiState.value = CalculatorUiState.WhileTyping(
                expression = currentExpression,
                partialResult = partial
            )
        }
    }

    /**
     * Formats decimal results to avoid displaying trailing ".0" on integers.
     *
     * @param value Double value resulting from math computation.
     * @return String representation of the formatted number.
     */
    private fun formatOutput(value: Double): String {
        return if (value % 1 == 0.0) value.toLong().toString() else value.toString()
    }
}
