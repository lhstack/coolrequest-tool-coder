package dev.coolrequest.tool.coder.impl;

import dev.coolrequest.tool.coder.Coder;
import dev.coolrequest.tool.coder.Kind;
import org.apache.commons.codec.digest.DigestUtils;

public class TextToMd5HexCoder implements Coder {
    @Override
    public String transform(String data) {
        return DigestUtils.md5Hex(data);
    }

    @Override
    public Kind kind() {
        return Kind.of("text", "md5");
    }
}
