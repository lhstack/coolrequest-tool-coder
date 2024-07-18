package dev.coolrequest.tool.coder.custom;

import dev.coolrequest.tool.coder.Coder;
import dev.coolrequest.tool.coder.Kind;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class CoderRegistry {

    private final List<Coder> existCoders;

    private final List<Coder> registryCoders = new ArrayList<>();

    private final List<Coder> noRegistryCoders = new ArrayList<>();

    public CoderRegistry(List<Coder> existCoders) {
        this.existCoders = existCoders;
    }

    public void registry(String source, String target, Function<String, String> mapper) {
        Coder coder = new Coder() {
            @Override
            public String transform(String data) {
                return mapper.apply(data);
            }

            @Override
            public Kind kind() {
                return Kind.of(source, target);
            }
        };
        boolean hasRegistry = false;
        for (Coder existCoder : existCoders) {
            if (existCoder.kind().is(coder.kind().source, coder.kind().target)) {
                hasRegistry = true;
            }
        }
        if (!hasRegistry) {
            registryCoders.add(coder);
        } else {
            noRegistryCoders.add(coder);
        }
    }

    public List<Coder> getNoRegistryCoders() {
        return noRegistryCoders;
    }

    public List<Coder> getExistCoders() {
        return existCoders;
    }

    public List<Coder> getRegistryCoders() {
        return registryCoders;
    }
}
