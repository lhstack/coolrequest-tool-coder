package dev.coolrequest.tool.views.coder;

public class Kind {
    public String source;
    public String target;

    public static Kind of(String source, String target) {
        Kind kind = new Kind();
        kind.source = source;
        kind.target = target;
        return kind;
    }

    public boolean is(String source, String target) {
        return source.equals(this.source) && target.equals(this.target);
    }
}
