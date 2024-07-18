package dev.coolrequest.tool.views.coder.impl;

import dev.coolrequest.tool.views.coder.Coder;
import dev.coolrequest.tool.views.coder.Kind;

public class DecToBinaryCoder implements Coder {
    @Override
    public String transform(String data) {
        try {
            return Long.toBinaryString(Long.parseLong(data));
        } catch (Exception e) {
            return "dec to binary fail,error: " + e.getMessage();
        }
    }

    @Override
    public Kind kind() {
        return Kind.of("dec", "binary");
    }
}
