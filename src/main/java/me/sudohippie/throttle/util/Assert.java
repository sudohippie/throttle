package me.sudohippie.throttle.util;

/**
 * Raghav Sidhanti
 * Date: 9/25/13
 */
public class Assert {
    public static void isTrue(boolean bool, String message){
        if(!bool) throw new IllegalArgumentException(message);
    }
}
