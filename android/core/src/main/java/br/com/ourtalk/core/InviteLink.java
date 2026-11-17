package br.com.ourtalk.core;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/** Strict parser for user-pasted invitation codes and official Play links. */
public final class InviteLink {
    private InviteLink() {}
    public static String token(String input) {
        if (input == null || input.length() > 2048) throw new IllegalArgumentException("Invalid invitation");
        String value = input.trim();
        if (value.matches("[0-9a-f]{48}")) return value;
        try {
            URI uri = URI.create(value);
            if (!"https".equals(uri.getScheme()) || !"play.google.com".equals(uri.getHost())
                    || !"/store/apps/details".equals(uri.getPath()) || uri.getUserInfo() != null
                    || uri.getPort() != -1 || uri.getFragment() != null) throw new IllegalArgumentException();
            var params = parameters(uri.getRawQuery());
            if (!"br.com.ourtalk.app".equals(params.get("id"))) throw new IllegalArgumentException();
            String code = parameters(params.get("referrer")).get("ourtalk_invite");
            if (code == null || !code.matches("[0-9a-f]{48}")) throw new IllegalArgumentException();
            return code;
        } catch (RuntimeException error) {
            throw new IllegalArgumentException("Invalid invitation");
        }
    }
    private static HashMap<String, String> parameters(String raw) {
        if (raw == null) throw new IllegalArgumentException();
        var result = new HashMap<String, String>();
        for (String part : raw.split("&")) {
            String[] pair = part.split("=", 2);
            if (pair.length != 2) throw new IllegalArgumentException();
            String key = decode(pair[0]);
            if (result.putIfAbsent(key, decode(pair[1])) != null)
                throw new IllegalArgumentException();
        }
        return result;
    }
    private static String decode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
        } catch (java.io.UnsupportedEncodingException impossible) {
            throw new IllegalArgumentException(impossible);
        }
    }
}
