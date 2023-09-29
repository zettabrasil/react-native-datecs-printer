package br.com.zettabrasil.datecsprinter.util;

/**
 * Created by zettabrasil on 12/04/16.
 */
public class Cont {

    private static int value = 0;

    public static int getValue()
    {
        return value;
    }

    public static void increment()
    {
        value++;
    }

    public static void reset()
    {
        value = 0;
    }
}
