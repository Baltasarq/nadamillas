package com.devbaltasarq.nadamillas.core;

public class AppInfo {
    public static final String AUTHOR = "baltasarq@gmail.com";
    public static final String VERSION = "1.0.2 20190315";
    public static final String LICENSE = "MIT license";
    public static final String COPYRIGHT = "(c) 2019";

    public static String getAuthoringMessage()
    {
        return AUTHOR + " " + VERSION;
    }

    public static String getCompleteAuthoringMessage()
    {
        return COPYRIGHT + " " + LICENSE + "\n" + AUTHOR + " v" + VERSION;
    }
}
