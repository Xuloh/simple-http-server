package fr.insa.http.util;

public final class Util {
    public static byte[] concatenateArrays(byte[] a1, int offset1, int length1, byte[] a2, int offset2, int length2) {
        byte[] all = new byte[length1 + length2];
        System.arraycopy(a1, offset1, all, 0, length1);
        System.arraycopy(a2, offset2, all, length1, length2);
        return all;
    }

    public static byte[] concatenateArrays(byte[] a1, byte[] a2) {
        return concatenateArrays(a1, 0, a1.length, a2, 0, a2.length);
    }
}
