package com.eirs.fcm.utils;

import java.time.format.DateTimeFormatter;

public interface DateFormatterConstants {
    DateTimeFormatter macraSuffixFullDateFormat = DateTimeFormatter.ofPattern("yyyyMMdd");
    DateTimeFormatter macraFileHeaderDateFormat = DateTimeFormatter.ofPattern("yyyyMMdd");
    DateTimeFormatter fileSuffixDateFormat = DateTimeFormatter.ofPattern("yyyyMMddHH");
    DateTimeFormatter simpleDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
}
