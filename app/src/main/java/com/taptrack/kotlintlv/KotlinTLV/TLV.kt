package com.taptrack.kotlintlv.KotlinTLV

import android.os.Build
import androidx.annotation.RequiresApi
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

    public class BadCardStateException : Exception {
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

@Throws(TLV.BadCardStateException::class)
fun parseTlvData(data: ByteArray): List<TLV> {
    if (data.size < 2) {
        throw TLV.BadCardStateException("Too short to contain type and length")
    }

    var currentIdx = 0
    val tlvs = ArrayList<TLV>(7)
    while (currentIdx + 2 <= data.size) {
        val type = data[currentIdx]
        val length = data[currentIdx + 1].toInt() and 0xff
        if (data.size < currentIdx + length + 2) {
            throw TLV.BadCardStateException("Data too short to contain value specified in length header")
        }

        if (length > 0) {
            val dataStart = currentIdx + 2
            val dataEnd = dataStart + length
            tlvs.add(TLV(type.toInt(), Arrays.copyOfRange(data, dataStart, dataEnd)))
            currentIdx = dataEnd
        } else {
            tlvs.add(TLV(type.toInt(), ByteArray(0)))
            currentIdx = currentIdx + 2
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
    throw UnsupportedOperationException()
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

