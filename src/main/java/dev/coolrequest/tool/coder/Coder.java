package dev.coolrequest.tool.coder;

public interface Coder {

    String transform(String data);

    Kind kind();

    default int ordered() {
        return 0;
    }
}
