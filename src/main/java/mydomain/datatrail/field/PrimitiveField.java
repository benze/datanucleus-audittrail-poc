package mydomain.datatrail.field;

import org.datanucleus.metadata.FieldMetaData;

public class PrimitiveField extends Field{

    protected PrimitiveField(Object value, Object prevValue, String fieldName) {
        super(fieldName, value.getClass().getName());
        type = Type.PRIMITIVE;
        this.value = value.toString();
        this.prev = prevValue != null ? prevValue.toString() : null;
    }



}
