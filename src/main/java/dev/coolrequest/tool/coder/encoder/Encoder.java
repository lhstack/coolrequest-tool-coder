package dev.coolrequest.tool.coder.encoder;

import dev.coolrequest.tool.coder.Kind;

public interface Encoder {
    public String encode(String data);

    public Kind kind();
}
