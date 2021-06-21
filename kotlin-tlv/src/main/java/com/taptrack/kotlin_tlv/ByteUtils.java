package com.taptrack.kotlin_tlv;

import androidx.annotation.NonNull;
import androidx.annotation.Size;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

public final class ByteUtils {
    private ByteUtils() {

    }

    /**
     * Convert byte array to hex string, from
     * http://stackoverflow.com/questions/9655181
     */
    final private static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    final private static char[] hexArrayLower = "0123456789abcdef".toCharArray();
    public static String bytesToHexLower(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArrayLower[v >>> 4];
            hexChars[j * 2 + 1] = hexArrayLower[v & 0x0F];
        }
        return new String(hexChars);
    }

    @NonNull
    public static UUID uuidFromBinary(@NonNull @Size(value = 16) byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        return new UUID(bb.getLong(),bb.getLong());
    }

    @NonNull
    @Size(value = 16)
    public static byte[] uuidToBinary(@NonNull UUID uuid) {
        ByteBuffer bb = ByteBuffer.allocate(16);
        bb.putLong(uuid.getMostSignificantBits())
                .putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

    @NonNull
    public static byte[] hexToBytes(@NonNull String hex)
    {
        char[] rawChars = hex.toUpperCase().toCharArray();

        int hexChars = 0;
        for (int i = 0; i < rawChars.length; i++) {
            if ((rawChars[i] >= '0' && rawChars[i] <= '9')
                    || (rawChars[i] >= 'A' && rawChars[i] <= 'F')) {
                hexChars++;
            }
        }

        byte[] byteString = new byte[(hexChars + 1) >> 1];
        int pos = hexChars & 1;

        for (int i = 0; i < rawChars.length; i++) {
            if (rawChars[i] >= '0' && rawChars[i] <= '9') {
                byteString[pos >> 1] <<= 4;
                byteString[pos >> 1] |= rawChars[i] - '0';
            } else if (rawChars[i] >= 'A' && rawChars[i] <= 'F') {
                byteString[pos >> 1] <<= 4;
                byteString[pos >> 1] |= rawChars[i] - 'A' + 10;
            } else {
                continue;
            }
            pos++;
        }

        return byteString;
    }


    @NonNull
    @Size(value = 4)
    public static byte[] intToArray(int amount) {
        return ByteBuffer.allocate(4).putInt(amount).array();
    }

    public static int arrayToInt(@NonNull @Size(value = 4) byte[] array) {
        return ByteBuffer.allocate(4).put(array).getInt(0);
    }

    public static byte[] concatenateByteArray(byte[]... args) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            for (byte[] arg : args) {
                outputStream.write(arg);
            }
        } catch (IOException e) {
            //wtf
        }

        return outputStream.toByteArray();
    }

    public static byte[] byteArrayXor(byte[] a, byte[] b) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        for (int i = 0; i < a.length; i++)
            stream.write((byte) ((a[i] ^ b[i]) & 0xff));
        return stream.toByteArray();
    }

}
