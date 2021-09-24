package mydomain.datatrail.field;

import org.datanucleus.enhancement.Persistable;
import org.datanucleus.state.ObjectProvider;

public class ReferenceField extends Field {

    protected String desc;

    protected ReferenceField(Persistable field, String fieldName) {
        super(fieldName, ((ObjectProvider)field.dnGetStateManager()).getClassMetaData().getFullClassName());
        type = Type.REF;
        value = getObjectId(field.dnGetObjectId());
    }

    public String getDesc() {
        return desc;
    }


    // @JsonProperty("id")
    public String getValue() {
        return value;
    }

}
