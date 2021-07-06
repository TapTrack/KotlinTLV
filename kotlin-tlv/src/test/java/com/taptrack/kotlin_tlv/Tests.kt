package com.taptrack.kotlin_tlv

import android.annotation.SuppressLint
import com.taptrack.kotlin_tlv.ByteUtils.intToArray
import org.junit.Assert.*
import org.junit.Test


class KotlinTLVSpec{

    // IllegalArgumentException tests
    @Test
    fun tagGreaterThan65279 () {
        val value = byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00)
        val tag = 65280
        assertThrows("Unsupported type amount - type too large", IllegalArgumentException::class.java){
            val tlv = TLV(tag, value)
        }
    }

    @Test
    fun valueLengthGreaterThan65279 () {
        val length = 65280
        val value = ByteArray(length)
        for(i in 0 until length){
            value[i] = 0x00
        }
        val tag = 5
        assertThrows("Unsupported type amount - length too large", IllegalArgumentException::class.java){
            val tlv = TLV(tag, value)
        }
    }

    // MalformedTlvByteArrayException tests
    @Test
    fun dataLengthLessThan2(){
        val data = byteArrayOf(0x00)
        assertThrows("Too short to contain type and length", TLV.MalformedTlvByteArrayException::class.java){
            parseTlvData(data)
        }
    }

    @Test
    fun twoByteTagOneByteLengthError(){
        val tag = 1000
        val length = 5
        val data = byteArrayOf(0xFF.toByte(), 0x03, 0xE8.toByte(), length.toByte(), 0x00, 0x00, 0x00, 0x00)
        assertThrows("Data too short to contain value specified in length header", TLV.MalformedTlvByteArrayException::class.java){
            parseTlvData(data)
        }
    }

    @Test
    fun twoByteTagTwoByteLengthError(){
        val tag = intToArray(1000)
        val length = intToArray(1000)
        val value = ByteArray(999)
        for(i in 0 until 999){
            value[i] = 0x00
        }
        val data = byteArrayOf(0xFF.toByte(), 0x03, 0xE8.toByte(), 0xFF.toByte(), 0x03, 0xE8.toByte()) + value
        assertThrows("Data too short to contain value specified in length header", TLV.MalformedTlvByteArrayException::class.java){
            parseTlvData(data)
        }
    }

    @Test
    fun oneByteTagTwoByteLengthError(){
        val tag = 5
        val length = intToArray(1000)
        val value = ByteArray(999)
        for(i in 0 until 999){
            value[i] = 0x00
        }
        val data = byteArrayOf(tag.toByte(), 0xFF.toByte(), 0x03, 0xE8.toByte()) + value
        assertThrows("Data too short to contain value specified in length header", TLV.MalformedTlvByteArrayException::class.java){
            parseTlvData(data)
        }
    }

    @Test
    fun oneByteTagOneByteLengthError(){
        val tag = 5
        val length = 5
        val value = ByteArray(4)
        for(i in 0 until 4){
            value[i] = 0x00
        }
        val data = byteArrayOf(tag.toByte(), length.toByte()) + value
        assertThrows("Data too short to contain value specified in length header", TLV.MalformedTlvByteArrayException::class.java){
            parseTlvData(data)
        }
    }

    // Converting a TLV to a byte array
    @Test
    fun singleByteSingleByteTag () {
        val length : Int = 5
        val value: ByteArray = byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00)
        val tag = 1
        val expectedByteArray : ByteArray = byteArrayOf(0x01,0x05,0x00,0x00,0x00,0x00,0x00)
        assertArrayEquals(TLV(tag, value).toByteArray(), expectedByteArray)
    }

    @Test
    fun singleByteSingleByteTag2 () {
        val length : Int = 32
        val value = ByteArray(32)
        for(i in 0 until length) {
            value[i] = 0x00
        }
        val tag : Byte = 1
        val expectedByteArray = ByteArray(34)
        for(i in 0..(length+1)) {
            expectedByteArray[i] = 0x00
        }
        expectedByteArray[0] = 0x01
        expectedByteArray[1] = 0x20
        assertArrayEquals((TLV(tag.toInt(), value).toByteArray()), expectedByteArray)
    }

    @Test
    fun twoByteSingleByteTag () {
        val length : Int = 1000
        val value = ByteArray(length)
        for(i in 0 until length) {
            value[i] = 0x00
        }
        val tag : Int = 1
        val expectedByteArray = ByteArray(length+4)
        for(i in 0..(length+3)) {
            expectedByteArray[i] = 0x00
        }
        expectedByteArray[0] = 0x01
        expectedByteArray[1] = 0xFF.toByte()
        expectedByteArray[2] = 0x03
        expectedByteArray[3] = 0xE8.toByte()
        assertArrayEquals(TLV(tag, value).toByteArray(), expectedByteArray)
    }

    @Test
    fun singleByteTwoByteTag () {
        val length : Int = 32
        val value = ByteArray(length)
        for(i in 0 until length) {
            value[i] = 0x00
        }
        val tag : Int = 1000
        val expectedByteArray = ByteArray(length+4)
        for(i in 0..(length+3)) {
            expectedByteArray[i] = 0x00
        }
        expectedByteArray[0] = 0xFF.toByte()
        expectedByteArray[1] = 0x03
        expectedByteArray[2] = 0xE8.toByte()
        expectedByteArray[3] = 0x20
        assertArrayEquals((TLV(tag, value).toByteArray()), expectedByteArray)
    }

    @Test
    fun twoByteTwoByteTag () {
        val length : Int = 1000
        val value = ByteArray(length)
        for(i in 0 until length) {
            value[i] = 0x00
        }
        val tag : Int = 1000
        val expectedByteArray = ByteArray(length+6)
        for(i in 0..(length+5)) {
            expectedByteArray[i] = 0x00
        }
        expectedByteArray[0] = 0xFF.toByte()
        expectedByteArray[1] = 0x03
        expectedByteArray[2] = 0xE8.toByte()
        expectedByteArray[3] = 0xFF.toByte()
        expectedByteArray[4] = 0x03
        expectedByteArray[5] = 0xE8.toByte()
        assertArrayEquals((TLV(tag, value).toByteArray()), expectedByteArray)
    }

    // Converting a list of TLVs to a byte array
    @Test
    fun twoTlvsInList () {
        val length : Int = 5
        val value = ByteArray(length)
        for(i in 0 until length) {
            value[i] = 0x00
        }
        val firstTag : Int = 1
        val secondTag : Int = 2
        val expectedByteArray : ByteArray = byteArrayOf(0x01, 0x05, 0x00,0x00,0x00,0x00,0x00,0x02,0x05,0x00,0x00,0x00,0x00,0x00)
        var listOfTlvs : MutableList<TLV> = arrayListOf()
        listOfTlvs.add(TLV(firstTag, value))
        listOfTlvs.add(TLV(secondTag, value))
        assertArrayEquals(listOfTlvs.writeOutTLVBinary(), expectedByteArray)
    }

    // Parsing a raw TLV list as a byte array into list of TLVs
    @Test
    fun twoTlvsSingleTagLengthZero() {
        val rawTlvs : ByteArray = byteArrayOf(0x01,0x00,0x02,0x00)
        val length : Int = 0
        val value : ByteArray = byteArrayOf()
        for(i in 0 until length) {
            value[i] = 0x00
        }
        val firstTag : Int = 1
        val secondTag : Int = 2

        var listOfTlvs : MutableList<TLV> = mutableListOf()
        listOfTlvs.add(TLV(firstTag, value))
        listOfTlvs.add(TLV(secondTag, value))
        assertArrayEquals(listOfTlvs.toList().toTypedArray(), parseTlvData(rawTlvs).toTypedArray())
    }

    @Test
    fun twoSingleTagByteLengthZeroOneNonZeroLength () {
        val rawTlvs : ByteArray = byteArrayOf(0x01,0x00,0x02,0x00,0x03,0x05,0x00,0x00,0x00,0x00,0x00)
        val zeroLength : Int = 0
        val nonZeroLength : Int = 5
        val value = ByteArray(5)
        for(i in 0 until nonZeroLength) {
            value[i] = 0x00
        }
        val zeroLengthValue = ByteArray(0)
        val tag1 : Int = 1
        val tag2 : Int = 2
        val tag3 : Int = 3

        var listOfTlvs : MutableList<TLV> = mutableListOf()
        listOfTlvs.add(TLV(tag1, zeroLengthValue))
        listOfTlvs.add(TLV(tag2, zeroLengthValue))
        listOfTlvs.add(TLV(tag3, value))
        assertArrayEquals(listOfTlvs.toList().toTypedArray(), parseTlvData(rawTlvs).toTypedArray())
    }

    @Test
    fun twoSingleTagSingleByteLength () {
        val rawTlvs : ByteArray = byteArrayOf(0x01,0x05,0x00,0x00,0x00,0x00,0x00,0x02,0x05,0x00,0x00,0x00,0x00,0x00)
        val length : Int = 5
        val value = ByteArray(5)
        val val2 = byteArrayOf(0x00,0x00,0x00,0x00,0x00)
        for(i in 0 until length) {
            value[i] = 0x00
        }
        val firstTag : Int = 1
        val secondTag : Int = 2

        var listOfTlvs : MutableList<TLV> = mutableListOf()
        listOfTlvs.add(TLV(firstTag,value))
        listOfTlvs.add(TLV(secondTag,value))
        assertEquals(listOfTlvs.toList(), parseTlvData(rawTlvs))
    }

    @SuppressLint("Range")
    @Test
    fun one1ByteTag2ByteLengthOne1ByteTag1ByteLength () {
        val length1 : Int = 1000
        val length2 : Int = 5
        var value1 = ByteArray(1000)
        for(i in 0 until length1) {
            value1[i]  = 0x00
        }

        var value2 = ByteArray(5)
        for(i in 0 until length2) {
            value2[i] = 0x00
        }

        val tag1 : Int = 1
        val tag2 : Int = 2
        var rawTlvs = ByteArray(length1+length2+6)
        for(i in 0..(length1+length2+5)) {
            rawTlvs[i] = 0x00
        }
        rawTlvs[0] = tag1.toByte()
        rawTlvs[1] = 0xFF.toByte()
        rawTlvs[2] = 0x03
        rawTlvs[3] = 0xE8.toByte()
        rawTlvs[4+length1] = tag2.toByte()
        rawTlvs[5+length1] = length2.toByte()
        var listOfTlvs : MutableList<TLV> = mutableListOf()
        listOfTlvs.add(TLV(tag1, value1))
        listOfTlvs.add(TLV(tag2, value2))
        assertEquals(listOfTlvs, parseTlvData(rawTlvs))
    }

    @Test
    fun one1ByteTag1ByteLengthOne1ByteTag2ByteLength () {
        val length1 : Int = 1000
        val length2 : Int = 5
        val value1 = ByteArray(length1)
        for(i in 0 until length1) {
            value1[i] = 0x00
        }

        val value2 = ByteArray(length2)
        for(i in 0 until length2) {
            value2[i] = 0x00
        }

        val tag1 : Int = 1
        val tag2 : Int = 2
        var rawTlvs = ByteArray(length1+length2+6)
        for(i in 0..(length1+length2+5)) {
            rawTlvs[i] = 0x00
        }
        rawTlvs[0] = tag2.toByte()
        rawTlvs[1] = length2.toByte()
        rawTlvs[2+length2] = tag1.toByte()
        rawTlvs[3+length2] = 0xFF.toByte()
        rawTlvs[4+length2] = 0x03
        rawTlvs[5+length2] = 0xE8.toByte()

        var listOfTlvs : MutableList<TLV> = mutableListOf()
        listOfTlvs.add(TLV(tag2, value2))
        listOfTlvs.add(TLV(tag1, value1))
        assertEquals(listOfTlvs, parseTlvData(rawTlvs))
    }

    @Test
    fun one2ByteTag1ByteLenOne2ByteTag2ByteLen () {
        val length1 : Int = 5
        val length2 : Int = 1000
        val value1 = ByteArray(length1)
        for(i in 0 until length1) {
            value1[i] = 0x00
        }

        val value2 = ByteArray(length2)
        for(i in 0 until length2) {
            value2[i] = 0x00
        }

        val tag1 : Int = 1000
        val tag2 : Int = 2000
        var rawTlvs = ByteArray(length1+length2+10)
        for(i in 0..(length1+length2+9)) {
            rawTlvs[i] = 0x00
        }
        rawTlvs[0] = 0xFF.toByte()
        rawTlvs[1] = 0x03
        rawTlvs[2] = 0xE8.toByte()
        rawTlvs[3] = 0x05
        rawTlvs[4+length1] = 0xFF.toByte()
        rawTlvs[5+length1] = 0x07
        rawTlvs[6+length1] = 0xD0.toByte()
        rawTlvs[7+length1] = 0xFF.toByte()
        rawTlvs[8+length1] = 0x03
        rawTlvs[9+length1] = 0xE8.toByte()

        var listOfTlvs : MutableList<TLV> = mutableListOf()
        listOfTlvs.add(TLV(tag1, value1))
        listOfTlvs.add(TLV(tag2, value2))
        assertEquals(listOfTlvs, parseTlvData(rawTlvs))
    }

    @Test
    fun one2ByteTag2ByteLenOne2ByteTag1ByteLen () {
        val length1 : Int = 5
        val length2 : Int = 1000
        val value1 = ByteArray(length1)
        for(i in 0 until length1) {
            value1[i] = 0x00
        }

        val value2 = ByteArray(length2)
        for(i in 0 until length2) {
            value2[i] = 0x00
        }

        val tag1 : Int = 1000
        val tag2 : Int = 2000
        var rawTlvs : ByteArray = byteArrayOf()
        for(i in 0..(length1+length2+9)) {
            rawTlvs += 0x00
        }
        rawTlvs[0] = 0xFF.toByte()
        rawTlvs[1] = 0x07
        rawTlvs[2] = 0xD0.toByte()
        rawTlvs[3] = 0xFF.toByte()
        rawTlvs[4] = 0x03
        rawTlvs[5] = 0xE8.toByte()
        rawTlvs[6+length2] = 0xFF.toByte()
        rawTlvs[7+length2] = 0x03
        rawTlvs[8+length2] = 0xE8.toByte()
        rawTlvs[9+length2] = 0x05

        var listOfTlvs : MutableList<TLV> = mutableListOf()
        listOfTlvs.add(TLV(tag2, value2))
        listOfTlvs.add(TLV(tag1, value1))
        assertEquals(listOfTlvs, parseTlvData(rawTlvs))
    }

    @Test
    fun two2ByteTag2ByteLength () {
        val length1 : Int = 1002
        val length2 : Int = 2002
        val value1 = ByteArray(length1)
        for(i in 0 until length1) {
            value1[i] = 0x00
        }

        val value2 = ByteArray(length2)
        for(i in 0 until length2) {
            value2[i] = 0x00
        }

        val tag1 : Int = 1000
        val tag2 : Int = 2000
        var rawTlvs : ByteArray = byteArrayOf()
        for(i in 0..(length1+length2+11)) {
            rawTlvs += 0x00
        }
        rawTlvs[0] = 0xFF.toByte()
        rawTlvs[1] = 0x03
        rawTlvs[2] = 0xE8.toByte()
        rawTlvs[3] = 0xFF.toByte()
        rawTlvs[4] = 0x03
        rawTlvs[5] = 0xEA.toByte()
        rawTlvs[6+length1] = 0xFF.toByte()
        rawTlvs[7+length1] = 0x07
        rawTlvs[8+length1] = 0xD0.toByte()
        rawTlvs[9+length1] = 0xFF.toByte()
        rawTlvs[10+length1] = 0x07
        rawTlvs[11+length1] = 0xD2.toByte()

        var listOfTlvs : MutableList<TLV> = mutableListOf()
        listOfTlvs.add(TLV(tag1, value1))
        listOfTlvs.add(TLV(tag2, value2))
        assertEquals(listOfTlvs, parseTlvData(rawTlvs))
    }

    @Test
    fun hundred2ByteTag2ByteLen () {
        val length1 : Int = 1002
        val length2 : Int = 2002
        val value1 = ByteArray(length1)
        for(i in 0 until length1) {
            value1[i] = 0x00
        }

        val value2 = ByteArray(length2)
        for(i in 0 until length2) {
            value2[i] = 0x00
        }

        val tag1 : Int = 1000
        val tag2 : Int = 2000
        var rawTlvsBlock : ByteArray = byteArrayOf()

        for(i in 0..(length1+length2+11)) {
            rawTlvsBlock += 0x00
        }

        rawTlvsBlock[0] = 0xFF.toByte()
        rawTlvsBlock[1] = 0x03
        rawTlvsBlock[2] = 0xE8.toByte()
        rawTlvsBlock[3] = 0xFF.toByte()
        rawTlvsBlock[4] = 0x03
        rawTlvsBlock[5] = 0xEA.toByte()
        rawTlvsBlock[6+length1] = 0xFF.toByte()
        rawTlvsBlock[7+length1] = 0x07
        rawTlvsBlock[8+length1] = 0xD0.toByte()
        rawTlvsBlock[9+length1] = 0xFF.toByte()
        rawTlvsBlock[10+length1] = 0x07
        rawTlvsBlock[11+length1] = 0xD2.toByte()

        var rawTlvs : ByteArray = byteArrayOf()
        var listOfTlvs : MutableList<TLV> = mutableListOf()
        var numBlocksAppended = 0

        while(numBlocksAppended < 100){
            rawTlvs += rawTlvsBlock
            listOfTlvs.add(TLV(tag1, value1))
            listOfTlvs.add(TLV(tag2, value2))
            numBlocksAppended++
        }
        assertEquals(listOfTlvs, parseTlvData(rawTlvs))
    }
    @Test
    fun hundred1ByteTag1ByteLen () {
        val length1 : Int = 5
        val length2 : Int = 10
        val value1 = ByteArray(length1)
        for(i in 0 until length1) {
            value1[i] = 0x00
        }
        val value2 = ByteArray(length2)
        for(i in 0 until length2) {
            value2[i] = 0x00
        }

        val tag1 : Int = 1
        val tag2 : Int = 2
        var rawTlvsBlock = ByteArray(length1+length2+4)

        for(i in 0..(length1+length2+3)) {
            rawTlvsBlock[i] = 0x00
        }
        rawTlvsBlock[0] = tag1.toByte()
        rawTlvsBlock[1] = length1.toByte()
        rawTlvsBlock[2+length1] = tag2.toByte()
        rawTlvsBlock[3+length1] = length2.toByte()

        var rawTlvs : ByteArray = byteArrayOf()
        var listOfTlvs : MutableList<TLV> = mutableListOf()
        var numBlocksAppended = 0

        while(numBlocksAppended < 100){
            rawTlvs += rawTlvsBlock
            listOfTlvs.add(TLV(tag1, value1))
            listOfTlvs.add(TLV(tag2, value2))
            numBlocksAppended++
        }
        assertArrayEquals(listOfTlvs.toList().toTypedArray(), parseTlvData(rawTlvs).toTypedArray())
    }

    @Test
    fun hundredAlternating2ByteTag2ByteLenAnd1ByteTag1ByteLen () {
        val length1 : Int = 1002
        val length2 : Int = 5
        val value1 = ByteArray(length1)
        for(i in 0 until length1) {
            value1[i] = 0x00
        }
        val value2 = ByteArray(length2)
        for(i in 0 until length2) {
            value2[i] = 0x00
        }

        val tag1 : Int = 1000
        val tag2 : Int = 1
        var rawTlvsBlock : ByteArray = byteArrayOf()

        for(i in 0..(length1+length2+7)) {
            rawTlvsBlock += 0x00
        }
        rawTlvsBlock[0] = 0xFF.toByte()
        rawTlvsBlock[1] = 0x03
        rawTlvsBlock[2] = 0xE8.toByte()
        rawTlvsBlock[3] = 0xFF.toByte()
        rawTlvsBlock[4] = 0x03
        rawTlvsBlock[5] = 0xEA.toByte()
        rawTlvsBlock[6+length1] = tag2.toByte()
        rawTlvsBlock[7+length1] = length2.toByte()

        var rawTlvs : ByteArray = byteArrayOf()
        var listOfTlvs : MutableList<TLV> = mutableListOf()
        var numBlocksAppended = 0

        while(numBlocksAppended < 100){
            rawTlvs += rawTlvsBlock
            listOfTlvs.add(TLV(tag1, value1))
            listOfTlvs.add(TLV(tag2, value2))
            numBlocksAppended++
        }
        assertArrayEquals(listOfTlvs.toList().toTypedArray(), parseTlvData(rawTlvs).toTypedArray())
    }

    // Fetching a TLV from a list
    @Test
    fun fetchTlvThatIsPresent () {
        val length : Int = 1
        val value = ByteArray(1)
        for(i in 0 until length) {
            value[i] = 0x00
        }
        val tag : Int = 1
        val tlv = TLV(tag, value)
        var tlvList : MutableList<TLV> = mutableListOf()
        tlvList.add(tlv)
        assertEquals(lookUpTlvInList(tlvList, tag), tlv)
    }

    @Test
    fun fetchTlvThatIsntPresent () {
        val length : Int = 1
        val value = ByteArray(1)
        for(i in 0 until length) {
            value[i] = 0x00
        }
        val tag : Int = 1
        val tlv = TLV(tag, value)
        var tlvList : MutableList<TLV> = mutableListOf()
        tlvList.add(tlv)
        assertThrows(TLV.TLVNotFoundException::class.java){
            lookUpTlvInList(tlvList, tag+1)
        }
    }

    @Test
    fun fetchTlvIfPresentAndTlvIsPresent () {
        val length : Int = 1
        var value : ByteArray = byteArrayOf()
        for(i in 0 until length) {
            value += 0x00
        }
        val tag : Int = 1
        val tlv = TLV(tag, value)
        val nonMatchingTlv = TLV(tag+1, value)
        var tlvList : MutableList<TLV> = mutableListOf()
        tlvList.add(nonMatchingTlv)
        tlvList.add(nonMatchingTlv)
        tlvList.add(nonMatchingTlv)
        tlvList.add(tlv)
        tlvList.add(nonMatchingTlv)
        tlvList.add(nonMatchingTlv)
        assertEquals(lookUpTlvInListIfPresent(tlvList, tag), tlv)
    }

    @Test
    fun fetchTlvIfPresentAndTlvIsntPresent () {
        val length : Int = 1
        var value : ByteArray = byteArrayOf()
        for(i in 0 until length) {
            value += 0x00
        }
        val tag : Int = 1
        val nonMatchingTlv = TLV(tag+1, value)
        var tlvList : MutableList<TLV> = mutableListOf()
        tlvList.add(nonMatchingTlv)
        tlvList.add(nonMatchingTlv)
        tlvList.add(nonMatchingTlv)
        tlvList.add(nonMatchingTlv)
        tlvList.add(nonMatchingTlv)
        assertNull(lookUpTlvInListIfPresent(tlvList, tag))
    }

    @Test
    fun fetchTlvValueAndTlvIsPresent () {
        val length : Int = 1
        var value : ByteArray = byteArrayOf()
        for(i in 0 until length) {
            value += 0x00
        }
        val tag : Int = 1
        val tlv = TLV(tag, value)
        val nonMatchingTlv = TLV(tag+1, value)
        var tlvList : MutableList<TLV> = mutableListOf()
        tlvList.add(nonMatchingTlv)
        tlvList.add(nonMatchingTlv)
        tlvList.add(nonMatchingTlv)
        tlvList.add(tlv)
        tlvList.add(nonMatchingTlv)
        tlvList.add(nonMatchingTlv)
        assertEquals(fetchTlvValue(tlvList, tag), tlv.value)
    }

    @Test
    fun fetchTlvValueAndTlvIsntPresent () {
        val length : Int = 1
        var value : ByteArray = byteArrayOf()
        for(i in 0 until length) {
            value += 0x00
        }
        val tag : Int = 1
        val nonMatchingTlv = TLV(tag+1, value)
        var tlvList : MutableList<TLV> = mutableListOf()
        tlvList.add(nonMatchingTlv)
        tlvList.add(nonMatchingTlv)
        tlvList.add(nonMatchingTlv)
        tlvList.add(nonMatchingTlv)
        tlvList.add(nonMatchingTlv)
        assertArrayEquals(fetchTlvValue(tlvList, tag), byteArrayOf())
    }
}