package com.ixeken.nepo.features.calculator.presentation

import androidx.compose.ui.text.input.TextFieldValue

/**
 * UI State machine definitions for the calculator feature.
 */
sealed interface CalculatorUiState {
    /**
     * Initial/empty calculator state.
     */
    object Empty : CalculatorUiState
    
    /**
     * State while the user is actively entering an expression.
     *
     * @property expression The current formula entered by the user.
     * @property partialResult The real-time preview result of the formula.
     */
    data class WhileTyping(
        val expression: TextFieldValue,
        val partialResult: String
    ) : CalculatorUiState
    
    /**
     * State after the user submits the expression for final evaluation.
     *
     * @property finalResult The calculated final value or error response.
     * @property originalExpression The expression before evaluation.
     */
    data class AfterEqual(
        val finalResult: String,
        val originalExpression: TextFieldValue
    ) : CalculatorUiState
}

/**
 * User actions and events in the calculator interface.
 */
sealed interface CalculatorUserEvent {
    /**
     * User pressed a standard key (number, operator, parenthesis, etc.).
     *
     * @property key The string representation of the key pressed.
     */
    data class OnKeyPress(val key: String) : CalculatorUserEvent

    /**
     * User requested clearing the whole calculation workspace.
     */
    object OnClearAll : CalculatorUserEvent

    /**
     * User requested erasing the last character.
     */
    object OnDeleteSingle : CalculatorUserEvent

    /**
     * User requested evaluation of the final expression.
     */
    object OnEvaluate : CalculatorUserEvent

    /**
     * User changed the expression text or selection.
     */
    data class OnExpressionValueChanged(val newValue: TextFieldValue) : CalculatorUserEvent

    /**
     * User requested to toggle between Radian and Degree mode.
     */
    object OnToggleDegreeMode : CalculatorUserEvent

    /**
     * User requested to toggle Inverse functions mode.
     */
    object OnToggleInversedMode : CalculatorUserEvent
}
