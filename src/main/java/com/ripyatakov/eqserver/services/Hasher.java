package com.ripyatakov.eqserver.services;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

public class Hasher {
    private static int base = 36;
    private static int bitCoef = 9;
    private static int offset = 1023;
    private static int multCoef = 3;
    public static String sha256(String toHash) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] encodedhash = digest.digest(
                toHash.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(encodedhash);
    }
    public static String getQueueCode(int qid){
        qid = (qid<<bitCoef) * multCoef + offset;
        return Integer.toString(qid, base);
    }
    public static int getQId(String code){
        code = code.toLowerCase(Locale.ROOT);
        int qid = Integer.parseInt(code, base);
        qid = ((qid - offset) / multCoef) >> bitCoef;
        return qid;
    }
    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
