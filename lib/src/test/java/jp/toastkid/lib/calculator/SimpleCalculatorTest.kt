package jp.toastkid.lib.calculator

import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class SimpleCalculatorTest {

    @InjectMockKs
    private lateinit var calculator: SimpleCalculator

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInvoke() {
        assertEquals(2.0, calculator.invoke("1+1"))
        assertEquals(4.4682, calculator.invoke("2.2341*2"))
        assertEquals(51.0, calculator.invoke("1500*0.03+6"))
        assertEquals(525.00, calculator.invoke("1500/3 + 25"))
        assertEquals(525.00, calculator.invoke("1,500/3 + 25"))
        assertEquals(9.0, calculator.invoke("+11-2"))
        assertEquals(-9.0, calculator.invoke("-11+2"))
        assertEquals(Double.NEGATIVE_INFINITY, calculator.invoke("-11/0"))
        assertEquals(3.0, calculator.invoke("1++2"))
        assertEquals(4.0, calculator.invoke("(4/2) + 2"))
    }

    @Test
    fun testIncorrectInputCase() {
        assertNull(calculator.invoke("Good+day"))
        assertNull(calculator.invoke("Good-day"))
        assertNull(calculator.invoke("1+*+2"))
        assertNull(calculator.invoke("*1+2"))
        assertNull(calculator.invoke("/1+2"))
        assertNull(calculator.invoke("1+2/"))
        assertNull(calculator.invoke("1+2*"))
        assertNull(calculator.invoke("good"))
        assertNull(calculator.invoke("11+(2"))
        assertNull(calculator.invoke("11+)2"))
        assertNull(calculator.invoke("11++++++"))
        assertNull(calculator.invoke("++++++"))
        assertNull(calculator.invoke("3 @ 3 1"))
    }

}