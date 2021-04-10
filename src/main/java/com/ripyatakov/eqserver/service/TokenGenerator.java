package com.ripyatakov.eqserver.service;

import java.text.SimpleDateFormat;

public class TokenGenerator {
    private static String alphabet = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ";
    public static String getToken(String a, String b){

        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
        return Hasher.sha256(a + b + timeStamp);
    }
    public static String getRussiaCode(int id){
        String answ = alphabet.charAt(id/1000%33) + "";
        id = id % 1000;
        if (new Integer(id).toString().length() == 1){
            return answ + "00" + id;
        } else if (new Integer(id).toString().length() == 2){
            return answ + "0" + id;
        } else
            return answ + id;
    }
    public static int getRussianNumber(String code){
        int k = alphabet.indexOf(code.charAt(0));
        return k*1000 + Integer.parseInt(code.substring(1));
    }
}
