package com.customwrld.atom.util;

import com.customwrld.atom.network.Profile;
import com.customwrld.atom.network.Rank;

import java.util.Random;

public class Util {

    public static final String CHAT_BAR = "&7&m                                                                              ";

    public static int getRandomCode() {
        return new Random().nextInt(999999);
    }

    public static String getChatFormat(Profile profile) {
        return profile.getActiveRank().getPrefix() + " " + getColoredName(profile);
    }

    public static String getColoredName(Profile profile) {
        Rank rank = profile.getActiveRank();
        return rank.getColor().getCode() + profile.getName();
    }

}
