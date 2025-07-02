// NadaMillas (c) 2019-2023/24 Baltasar MIT License <baltasarq@gmail.com>


package com.devbaltasarq.nadamillas.core;


public class AppInfo {
    public static final String AUTHOR = "dev::baltasarq";
    public static final String EMAIL = "baltasarq@gmail.com";
    public static final String VERSION = "1.3.9";
    public static final String SERIAL = "20250702";
    public static final String LICENSE = "MIT license";
    public static final String COPYRIGHT = "(c) 2019-2025";

    public static String getAuthoringMessage()
    {
        return COPYRIGHT + " " + AUTHOR + " v" + VERSION;
    }

    public static String getCompleteAuthoringMessage()
    {
        return COPYRIGHT + " " + LICENSE + "\n"
               + AUTHOR + " (" + EMAIL + ") v" + VERSION + " " + SERIAL;
    }
}
