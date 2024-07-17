package dev.coolrequest.tool.coder.impl;

import dev.coolrequest.tool.coder.Coder;
import dev.coolrequest.tool.coder.Kind;

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
    public Kind kind() {
        return Kind.of("dec", "hex");
    }
}
