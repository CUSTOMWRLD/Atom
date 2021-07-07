package com.customwrld.atom.util;

import org.apache.commons.lang3.RandomStringUtils;

public class Util {

    public static int getRandomCode() {
        return Integer.parseInt(RandomStringUtils.randomNumeric(8));
    }

}
