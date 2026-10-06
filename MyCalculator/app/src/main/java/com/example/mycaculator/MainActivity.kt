
package com.example.mycaculator

import com.example.mycaculator.R
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.ComponentActivity
import java.math.BigDecimal
import java.math.RoundingMode

class MainActivity : ComponentActivity() {

    private lateinit var tvInput: TextView
    private lateinit var tvOldInput: TextView
    private lateinit var tvCurrentOperand: TextView

    private lateinit var btnOne: Button
    private lateinit var btnTwo: Button
    private lateinit var btnThree: Button
    private lateinit var btnFour: Button
    private lateinit var btnFive: Button
    private lateinit var btnSix: Button
    private lateinit var btnSeven: Button
    private lateinit var btnEight: Button
    private lateinit var btnNine: Button
    private lateinit var btnZero: Button
    private lateinit var btnDot: Button
    private lateinit var btnPLus: Button
    private lateinit var btnMinus: Button
    private lateinit var btnMultiply: Button
    private lateinit var btnDivide: Button
    private lateinit var btnEqual: Button
    private lateinit var clear: Button
    private lateinit var allClear: Button
    private lateinit var btnBackspace: ImageButton // 修正：若 XML 為 ImageButton 需匹配此型別

    private var currentInput = StringBuilder()
    private var currentOperator = Operator.NONE
    private var operand1: BigDecimal? = null

    enum class Operator {
        NONE, ADD, SUBTRACT, MULTIPLY, DIVIDE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//QQ
        // 初始化元件
        tvInput = findViewById(R.id.tvInput)
        tvOldInput = findViewById(R.id.tvOldInput)
        tvCurrentOperand = findViewById(R.id.tvCurrentOperand)

        btnOne = findViewById(R.id.btnOne)
        btnTwo = findViewById(R.id.btnTwo)
        btnThree = findViewById(R.id.btnThree)
        btnFour = findViewById(R.id.btnFour)
        btnFive = findViewById(R.id.btnFive)
        btnSix = findViewById(R.id.btnSix)
        btnSeven = findViewById(R.id.btnSeven)
        btnEight = findViewById(R.id.btnEight)
        btnNine = findViewById(R.id.btnNine)
        btnZero = findViewById(R.id.btnZero)
        btnDot = findViewById(R.id.btnDot)

        btnPLus = findViewById(R.id.btnPLus)
        btnMinus = findViewById(R.id.btnMinus)
        btnMultiply = findViewById(R.id.btnMultiply)
        btnDivide = findViewById(R.id.btnDivide)
        btnEqual = findViewById(R.id.btnEqual)
        clear = findViewById(R.id.clear)
        allClear = findViewById(R.id.allClear)
        btnBackspace = findViewById(R.id.btnBackspace)

        // 數字鍵監聽器
        btnOne.setOnClickListener { appendNumber("1") }
        btnTwo.setOnClickListener { appendNumber("2") }
        btnThree.setOnClickListener { appendNumber("3") }
        btnFour.setOnClickListener { appendNumber("4") }
        btnFive.setOnClickListener { appendNumber("5") }
        btnSix.setOnClickListener { appendNumber("6") }
        btnSeven.setOnClickListener { appendNumber("7") }
        btnEight.setOnClickListener { appendNumber("8") }
        btnNine.setOnClickListener { appendNumber("9") }
        btnZero.setOnClickListener { appendNumber("0") }
        btnDot.setOnClickListener { appendNumber(".") }

        // 運算鍵監聽器
        btnPLus.setOnClickListener { setOperator(Operator.ADD) }
        btnMinus.setOnClickListener { setOperator(Operator.SUBTRACT) }
        btnMultiply.setOnClickListener { setOperator(Operator.MULTIPLY) }
        btnDivide.setOnClickListener { setOperator(Operator.DIVIDE) }

        // 功能鍵監聽器
        btnEqual.setOnClickListener { calculateResult() }
        clear.setOnClickListener { clearInput() }
        allClear.setOnClickListener { allClearInput() }
        btnBackspace.setOnClickListener { handleBackspace() }
    }

    private fun appendNumber(number: String) {
        if (number == "." && currentInput.contains(".")) return
        if (number == "." && currentInput.isEmpty()) {
            currentInput.append("0")
        }
        currentInput.append(number)
        updateDisplay()
    }

    private fun handleBackspace() {
        if (currentInput.isNotEmpty()) {
            currentInput.deleteCharAt(currentInput.length - 1)
            updateDisplay()
        }
    }

    private fun setOperator(operator: Operator) {
        if (currentInput.isNotEmpty()) {
            operand1 = BigDecimal(currentInput.toString())
            currentInput.clear()
        } else if (operand1 == null) {
            return
        }

        tvOldInput.text = operand1?.stripTrailingZeros()?.toPlainString() ?: ""
        tvInput.text = "0"
        currentOperator = operator
        tvCurrentOperand.text = operatorToString(operator)
    }

    private fun operatorToString(operator: Operator): String {
        return when (operator) {
            Operator.ADD -> "+"
            Operator.SUBTRACT -> "-"
            Operator.MULTIPLY -> "×"
            Operator.DIVIDE -> "÷"
            Operator.NONE -> ""
        }
    }

    private fun calculateResult() {
        if (currentInput.isEmpty() || operand1 == null || currentOperator == Operator.NONE) {
            return
        }

        val operand2 = BigDecimal(currentInput.toString())
        var result: BigDecimal? = null

        try {
            when (currentOperator) {
                Operator.ADD -> result = operand1?.add(operand2)
                Operator.SUBTRACT -> result = operand1?.subtract(operand2)
                Operator.MULTIPLY -> result = operand1?.multiply(operand2)
                Operator.DIVIDE -> {
                    if (operand2.compareTo(BigDecimal.ZERO) != 0) {
                        result = operand1?.divide(operand2, 8, RoundingMode.HALF_UP)
                    } else {
                        tvInput.text = "Error: Div by 0"
                        resetCalculatorState()
                        return
                    }
                }
                Operator.NONE -> result = operand2
            }
        } catch (e: Exception) {
            Log.e("CalculatorApp", "Calculation error", e)
            tvInput.text = "Error"
            resetCalculatorState()
            return
        }

        if (result != null) {
            val formattedResult = result.stripTrailingZeros().toPlainString()
            tvOldInput.text = "${operand1?.stripTrailingZeros()?.toPlainString()} ${operatorToString(currentOperator)} ${operand2.stripTrailingZeros().toPlainString()}"
            tvInput.text = formattedResult

            operand1 = result
            currentInput.clear()
            currentOperator = Operator.NONE
            tvCurrentOperand.text = ""
        }
    }

    private fun allClearInput() {
        resetCalculatorState()
        tvInput.text = "0"
    }

    private fun clearInput() {
        currentInput.clear()
        tvInput.text = "0"
    }

    private fun resetCalculatorState() {
        currentInput.clear()
        operand1 = null
        currentOperator = Operator.NONE
        tvOldInput.text = ""
        tvCurrentOperand.text = ""
    }

    private fun updateDisplay() {
        tvInput.text = if (currentInput.isEmpty()) "0" else currentInput.toString()
    }
}