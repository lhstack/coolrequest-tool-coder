package dev.coolrequest.tool.views.coder.impl;

import dev.coolrequest.tool.views.coder.Coder;
import dev.coolrequest.tool.views.coder.Kind;
import org.apache.commons.codec.digest.DigestUtils;

import java.nio.charset.StandardCharsets;

public class TextToMd2HexCoder implements Coder {
    @Override
    public String transform(String data) {
        return DigestUtils.md2Hex(data.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Kind kind() {
        return Kind.of("text","md2");
    }
}
