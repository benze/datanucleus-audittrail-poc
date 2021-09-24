package mydomain.datatrail.field;

import org.datanucleus.metadata.FieldMetaData;

public class PrimitiveField extends Field{

    protected PrimitiveField(Object primitive, String fieldName) {
        super(fieldName, primitive.getClass().getName());
        type = Type.PRIMITIVE;
        value = primitive.toString();
    }

}
