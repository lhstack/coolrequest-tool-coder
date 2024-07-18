package dev.coolrequest.tool.views.coder;

public interface Coder {

    String transform(String data);

    Kind kind();

    default int ordered() {
        return 0;
    }
}
