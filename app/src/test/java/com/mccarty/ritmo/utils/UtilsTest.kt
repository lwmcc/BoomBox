package com.mccarty.ritmo.utils

import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals

class UtilsTest {

    private val num = 1000L
    private val divisor = 500L

    @Test
    fun`assert correct answer`() {
        val answer = num.quotientOf(divisor)
        assertEquals(2, answer)
    }

    @Test
    fun`assert correct answer if divide by zero`() {
        val answer = num.quotientOf(0)
        assertEquals(0, answer)
    }

    @Test
    fun`assert correct answer when given number is zero`() {
        val answer = 0L.quotientOf(num)
        assertEquals(0, answer)
    }
}