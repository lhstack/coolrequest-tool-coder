package dev.coolrequest.tool.views.coder.impl;

import dev.coolrequest.tool.views.coder.Coder;
import dev.coolrequest.tool.views.coder.Kind;

public class DecToHexCoder implements Coder {
    @Override
    public String transform(String data) {
        try {
            return Long.toHexString(Long.parseLong(data)).toUpperCase();
        } catch (Exception e) {
            return "dec to hex fail,error: " + e.getMessage();
        }
    }

    @Override
    public int ordered() {
        return 2;
    }

    @Override
    public Kind kind() {
        return Kind.of("dec", "hex");
    }
}
