package com.taptrack.kotlin_tlv

import android.os.Build
import androidx.annotation.RequiresApi
import com.taptrack.kotlin_tlv.ByteUtils.arrayToInt
import com.taptrack.kotlin_tlv.ByteUtils.intToArray
import okio.Buffer
import java.util.*

data class TLV (
    val typeVal: Int,
    val value: ByteArray
) {
    init {
        when {
            typeVal > 65279 -> throw IllegalArgumentException("Unsupported type amount - type too large")
        }

        val length = value.size
        when {
            length > 65279 -> throw IllegalArgumentException("Unsupported type amount - length too large")
        }
    }

    public class MalformedTlvByteArrayException : Exception {
        constructor() {}

        constructor(message: String) : super(message) {}

        constructor(message: String, cause: Throwable) : super(message, cause) {}

        constructor(cause: Throwable) : super(cause) {}

        @RequiresApi(api = Build.VERSION_CODES.N)
        constructor(message: String,
                    cause: Throwable,
                    enableSuppression: Boolean,
                    writableStackTrace: Boolean) : super(message, cause, enableSuppression, writableStackTrace) {
        }
    }

    public class TLVNotFoundException : Exception {
        constructor() {}

        constructor(message: String) : super(message) {}

        constructor(message: String, cause: Throwable) : super(message, cause) {}

        constructor(cause: Throwable) : super(cause) {}
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TLV

        if (typeVal != other.typeVal) return false
        if (!Arrays.equals(value, other.value)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = typeVal
        result = 31 * result + Arrays.hashCode(value)
        return result
    }

    fun writeToBuffer(buf: Buffer) {
        when {
            typeVal > 65279 -> throw IllegalArgumentException("Unsupported type amount - type too large")
            typeVal > 254 -> {
                buf.writeByte(0xff)
                buf.writeShort(typeVal)
            }
            else -> buf.writeByte(typeVal)
        }

        val length = value.size
        when {
            length > 65279 -> throw IllegalArgumentException("Unsupported type amount - length too large")
            length > 254 -> {
                buf.writeByte(0xff)
                buf.writeShort(length)
            }
            else -> buf.writeByte(length)
        }

        buf.write(value)
    }
}


fun (List<TLV>).writeToBuffer(buf: Buffer) {
    for (tlv in this) {
        tlv.writeToBuffer(buf)
    }
}

fun (List<TLV>).writeOutTLVBinary(): ByteArray {
    val buf = Buffer()
    this.writeToBuffer(buf)
    return buf.readByteArray()
}

fun (TLV).toByteArray(): ByteArray {
    if(this.typeVal > 65279){
        throw IllegalArgumentException("Unsupported type amount - type too large")
    }
    if (this.value.size > 65279){
        throw IllegalArgumentException("Unsupported type amount - length too large")
    }
    var tlvAsByteArray: ByteArray = byteArrayOf()

    if(this.typeVal > 254) {
        tlvAsByteArray += 0xFF.toByte()
        tlvAsByteArray += Arrays.copyOfRange(intToArray(this.typeVal), 2, intToArray(this.typeVal).size)
    } else {
        tlvAsByteArray += this.typeVal.toByte()
    }

    if(this.value.size > 254){
        tlvAsByteArray += 0xFF.toByte()
        tlvAsByteArray += Arrays.copyOfRange(intToArray(this.value.size), 2, intToArray(this.value.size).size)
    }else{
        tlvAsByteArray += this.value.size.toByte()
    }

    tlvAsByteArray += this.value

    return tlvAsByteArray
}

//@android.support.annotation.RequiresApi(Build.VERSION_CODES.GINGERBREAD)
@Throws(TLV.MalformedTlvByteArrayException::class)
fun parseTlvData(data: ByteArray): List<TLV> {
    if (data.size < 2) {
        throw TLV.MalformedTlvByteArrayException("Too short to contain type and length")
    }

    var currentIdx = 0
    val tlvs = ArrayList<TLV>(7)
    var isTwoByteTag : Boolean
    var isTwoByteLength : Boolean
    var tag : Int = 0
    var length : Int = 0
    var value : ByteArray = byteArrayOf()

    while (currentIdx + 2 <= data.size) {
        isTwoByteTag = false
        isTwoByteLength = false
        var value : ByteArray = byteArrayOf()
        if(data[currentIdx] == 0xFF.toByte()) { // Two byte tag
            if(currentIdx+2 < data.size) {
                isTwoByteTag = true
                tag = arrayToInt(byteArrayOf(0, 0) + data[currentIdx+1] + data[currentIdx+2])
            } else{
                throw TLV.MalformedTlvByteArrayException("Data too short to contain value specified in length header")
            }
        } else {
            tag = data[currentIdx].toInt()
        }

        if(isTwoByteTag){
            if(currentIdx + 3 < data.size) {
                if(data[currentIdx+3] == 0xFF.toByte()){ // Two byte length with two byte tag
                    isTwoByteLength = true
                    if(currentIdx + 5 < data.size){
                        length = arrayToInt(byteArrayOf(0,0) + data[currentIdx+4] + data[currentIdx+5])
                    }
                    else{
                        throw TLV.MalformedTlvByteArrayException("Data too short to contain value specified in length header")
                    }
                }else{ // One byte length with two byte tag
                    if(currentIdx + 3 < data.size){
                        length = data[currentIdx+3].toInt()
                    }else{
                        throw TLV.MalformedTlvByteArrayException("Data too short to contain value specified in length header")
                    }
                }
            }
        }else{
            if(data[currentIdx+1] == 0xFF.toByte()){ //two byte length with one byte tag
                isTwoByteLength = true
                if(currentIdx + 3 < data.size){
                    length = arrayToInt(byteArrayOf(0, 0) + data[currentIdx+2] + data[currentIdx+3])
                }else{
                    throw TLV.MalformedTlvByteArrayException("Data too short to contain value specified in length header")
                }
            }else{ //one byte length with one byte tag
                length = data[currentIdx+1].toInt()
            }
        }

//        val type = data[currentIdx]
//        val length = data[currentIdx + 1].toInt() and 0xff

        if (data.size < (currentIdx + length + 2)) {
            throw TLV.MalformedTlvByteArrayException("Data too short to contain value specified in length header")
        }

        var valueStart: Int

        if (isTwoByteTag && isTwoByteLength){
            valueStart = currentIdx + 6
        }else if (isTwoByteTag && !isTwoByteLength){
            valueStart = currentIdx + 4
        }else if (!isTwoByteTag && isTwoByteLength){
            valueStart = currentIdx + 4
        }else if(!isTwoByteTag && !isTwoByteLength){
            valueStart = currentIdx + 2
        }else{
            throw UnknownError()
        }

        if(valueStart + length - 1 < data.size && length != 0){
            value += Arrays.copyOfRange(data, valueStart, valueStart+length)
        }else if (length != 0){
            throw TLV.MalformedTlvByteArrayException("Data too short to contain value specified in length header")
        }
        try{
            tlvs.add(TLV(tag, value))
        }catch (e: Exception){
            throw UnknownError()
        }
        currentIdx += length
        if(isTwoByteLength){
            currentIdx += 3
        }else{
            currentIdx += 1
        }
        if(isTwoByteTag){
            currentIdx += 3
        }else{
            currentIdx += 1
        }
    }


    return tlvs
}

fun lookUpTlvInList (tlvList: List<TLV>, tlvType: Int) : TLV {
    for(tlv in tlvList) {
        if(tlvType == tlv.typeVal){
            return tlv
        }
    }
    throw TLV.TLVNotFoundException("The tag specified by $tlvType could not be found in the TLV list")
}

fun lookUpTlvInListIfPresent (tlvList: List<TLV>, tlvType: Int) : TLV? {
    for(tlv in tlvList) {
        if(tlvType == tlv.typeVal){
            return tlv
        }
    }
    return null
}

fun fetchTlvValue (tlvList: List<TLV>, tlvType: Int) : ByteArray {
    for(tlv in tlvList) {
        if(tlvType == tlv.typeVal){
            return tlv.value
        }
    }
    return byteArrayOf()
}

