package mydomain.datatrail;

abstract public class Field {
    enum Type{
        REF,
        PRIMITIVE;
    }

    protected String name;
    protected Type type;
}
