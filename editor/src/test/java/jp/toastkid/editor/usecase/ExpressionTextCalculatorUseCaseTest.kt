package jp.toastkid.editor.usecase

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ExpressionTextCalculatorUseCaseTest {
    private lateinit var subject: ExpressionTextCalculatorUseCase

    @Before
    fun setUp() {
        subject = ExpressionTextCalculatorUseCase()
    }

    @Test
    fun appendLineBreakIfNeed() {
        assertEquals("4.4682\n", subject.invoke("2.2341*2\n"))
    }

    @Test
    fun testInvoke() {
        arrayOf(
            "2.2341*2, 4.4682",
            "1+1, 2",
            "1500*0.03+6, 51",
            "1500/3 + 25, 525",
            "1_500/3 + 25, 525",
            "Good+day, Good+day",
            "11+(2, 11+(2"
        ).map {
            val split = it.split(", ")
            split[0] to split[1]
        }
            .forEach { pair ->
                assertEquals(pair.second, subject.invoke(pair.first.replace("_", ",")))
            }
    }

}