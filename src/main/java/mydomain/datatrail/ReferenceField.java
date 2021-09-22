package mydomain.datatrail;

public class ReferenceField extends Field{
    public ReferenceField() {
        type = Type.REF;
    }

    protected String className;
    protected Long id;
    protected String desc;
}
