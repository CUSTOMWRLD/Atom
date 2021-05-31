package com.customwrld.atom.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;

public class Logger {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    public static void log(Level level, String log) {
        System.out.println(level.format(log));
    }

    @Getter
    @AllArgsConstructor
    public enum Level {

        INFO("[" + dateFormat.format(System.currentTimeMillis()) + " INFO]: {0}"),
        WARNING("[" + dateFormat.format(System.currentTimeMillis()) + " WARNING]: {0}"),
        DEBUG("[" + dateFormat.format(System.currentTimeMillis()) + " DEBUG]: {0}"),
        ERROR("[" + dateFormat.format(System.currentTimeMillis()) + " ERROR]: {0}");

        private final String string;

        public String format(Object... objects) {
            return new MessageFormat(string).format(objects);
        }

    }

}
