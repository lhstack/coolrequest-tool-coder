package dev.coolrequest.tool.coder.impl;

import dev.coolrequest.tool.coder.Coder;
import dev.coolrequest.tool.coder.Kind;
import org.apache.commons.codec.digest.DigestUtils;

public class TextToSha512HexCoder implements Coder {
    @Override
    public String transform(String data) {
        return DigestUtils.sha512Hex(data);
    }

    @Override
    public Kind kind() {
        return Kind.of("text", "sha512");
    }
}
