package com.customwrld.atom.util;

import java.util.Random;

public class Util {

    public static int getRandomCode() {
        return new Random().nextInt(999999);
    }

}
