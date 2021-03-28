package com.ripyatakov.eqserver.service;

import java.text.SimpleDateFormat;

public class TokenGenerator {
    public static String getToken(String a, String b){
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
        return Hasher.sha256(a + b + timeStamp);
    }
}
