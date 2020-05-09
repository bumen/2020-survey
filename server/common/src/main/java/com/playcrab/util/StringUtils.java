package com.playcrab.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import com.sun.xml.internal.ws.util.UtilException;

/**
 * @date 2020-05-09
 * @author zhangyuqiang02@playcrab.com
 */
public abstract class StringUtils {

    public static String convert(String source, String srcCharset, String destCharset) {
        return convert(source, Charset.forName(srcCharset), Charset.forName(destCharset));
    }

    public static String convert(String source, Charset srcCharset, Charset destCharset) {
        if (null == srcCharset) {
            srcCharset = StandardCharsets.ISO_8859_1;
        }

        if (null == destCharset) {
            srcCharset = StandardCharsets.UTF_8;
        }

        return !isNullOrEmpty(source) && !srcCharset.equals(destCharset) ? new String(source.getBytes(srcCharset), destCharset) : source;
    }


    public static String getPath(String uriStr) {
        URI uri = null;

        try {
            uri = new URI(uriStr);
        } catch (URISyntaxException var3) {
            throw new UtilException(var3);
        }

        return uri == null ? null : uri.getPath();
    }

    public static String getMultistageReverseProxyIp(String ip) {
        if (ip != null && ip.indexOf(",") > 0) {
            String[] ips = ip.trim().split(",");
            String[] arr$ = ips;
            int len$ = ips.length;

            for(int i$ = 0; i$ < len$; ++i$) {
                String subIp = arr$[i$];
                if (!isUnknow(subIp)) {
                    ip = subIp;
                    break;
                }
            }
        }

        return ip;
    }

    public static boolean isUnknow(String checkString) {
        return isNullOrEmpty(checkString) || "unknown".equalsIgnoreCase(checkString);
    }


    public static boolean isMatch(Pattern pattern, String content) {
        return content != null && pattern != null ? pattern.matcher(content).matches() : false;
    }

    public static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }
}
