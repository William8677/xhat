package com.williamfq.xhat.utils

object ByteUtil {
    fun combine(vararg arrays: ByteArray): ByteArray {
        var length = 0
        for (array in arrays) {
            length += array.size
        }

        val combined = ByteArray(length)
        var offset = 0

        for (array in arrays) {
            System.arraycopy(array, 0, combined, offset, array.size)
            offset += array.size
        }

        return combined
    }

    fun trim(array: ByteArray, length: Int): ByteArray {
        val result = ByteArray(length)
        System.arraycopy(array, 0, result, 0, result.size)
        return result
    }

    fun split(input: ByteArray, firstLength: Int, secondLength: Int): Array<ByteArray> {
        val first = ByteArray(firstLength)
        val second = ByteArray(secondLength)

        System.arraycopy(input, 0, first, 0, firstLength)
        System.arraycopy(input, firstLength, second, 0, secondLength)

        return arrayOf(first, second)
    }

    fun shortToByteArray(value: Short): ByteArray {
        return byteArrayOf((value.toInt() shr 8).toByte(), value.toByte())
    }

    fun intToByteArray(value: Int): ByteArray {
        return byteArrayOf(
            (value shr 24).toByte(),
            (value shr 16).toByte(),
            (value shr 8).toByte(),
            value.toByte()
        )
    }

    fun longToByteArray(value: Long): ByteArray {
        return byteArrayOf(
            (value shr 56).toByte(),
            (value shr 48).toByte(),
            (value shr 40).toByte(),
            (value shr 32).toByte(),
            (value shr 24).toByte(),
            (value shr 16).toByte(),
            (value shr 8).toByte(),
            value.toByte()
        )
    }

    fun byteArrayToShort(bytes: ByteArray): Short {
        return ((bytes[0].toInt() and 0xff) shl 8 or
                (bytes[1].toInt() and 0xff)).toShort()
    }

    fun byteArrayToInt(bytes: ByteArray): Int {
        return (bytes[0].toInt() and 0xff) shl 24 or
                (bytes[1].toInt() and 0xff) shl 16 or
                (bytes[2].toInt() and 0xff) shl 8 or
                (bytes[3].toInt() and 0xff)
    }

    fun byteArrayToLong(bytes: ByteArray): Long {
        return (bytes[0].toLong() and 0xff) shl 56 or
                (bytes[1].toLong() and 0xff) shl 48 or
                (bytes[2].toLong() and 0xff) shl 40 or
                (bytes[3].toLong() and 0xff) shl 32 or
                (bytes[4].toLong() and 0xff) shl 24 or
                (bytes[5].toLong() and 0xff) shl 16 or
                (bytes[6].toLong() and 0xff) shl 8 or
                (bytes[7].toLong() and 0xff)
    }
}