package com.taptrack.kotlintlv

import com.taptrack.kotlintlv.KotlinTLV.TLV
import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    // Composed TLV

    @Test
    fun lengthGreaterThan65279 () {
        val length : Int = 65280
//        val value : ByteArray = byteArrayOf()
        val value = ByteArray(length)
        for(i in 0 until length){
            value[i] = 0x00
        }
        val tag : Int = 1
        val exception = assertThrows(IllegalArgumentException::class.java) {
            TLV(tag, value)
        }
        assertEquals("Unsupported type amount - length too large", exception.message)
    }

    @Test
    fun tagGreaterThan65279 () {
        val length : Int = 65279
        var value = ByteArray(length)
        for(i in 0 until length){
            value[i] = 0x00
        }
        val tag : Int = 65280
        val exception = assertThrows(IllegalArgumentException::class.java) {
            TLV(tag, value)
        }
        assertEquals("Unsupported type amount - type too large", exception.message)
    }

    // Converting a TLV to a byte array
    @Test
    fun singleByteSingleByteTag () {
        val length : Int = 5
        val value: ByteArray = byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00)
        val tag = 1
        val expectedByteArray : ByteArray = byteArrayOf(0x01,0x05,0x00,0x00,0x00,0x00,0x00)
        assertArrayEquals(TLV(tag, value), expectedByteArray)
    }

}