package com.alaka_ala.florafilm.ui.util.coreTorrent.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MagnetLinkParser {

    public static String extractBtih(String magnetLink) {
        if (magnetLink == null || magnetLink.isEmpty()) {
            return "";
        }
        // Регулярное выражение для поиска btih
        Pattern pattern = Pattern.compile("btih:([a-fA-F0-9]+)");
        Matcher matcher = pattern.matcher(magnetLink);

        if (matcher.find()) {
            return matcher.group(1).toLowerCase();
        }

        return "";
    }

}
