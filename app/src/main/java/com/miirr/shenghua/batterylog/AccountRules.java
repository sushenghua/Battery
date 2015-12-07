package com.miirr.shenghua.batterylog;

/**
 * Created by shenghua on 12/7/15.
 */
public class AccountRules {

    public static boolean isValidUsername(String username) {
//        return username.contains("@");
        return true;
    }

    public static boolean isValidPassword(String password) {
        return password.length() >= 4;
    }
}
