package mydomain.datatrail.field;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.datanucleus.enhancement.Persistable;
import org.datanucleus.metadata.FieldMetaData;
import org.datanucleus.state.ObjectProvider;

public class ReferenceField extends Field {

    protected String desc;

    protected ReferenceField(Persistable field, FieldMetaData fmd) {
        super(fmd != null ? fmd.getName() : null, field != null ? field.getClass().getName() : null );
        type = Type.REF;
        value = field != null ? getObjectId(field.dnGetObjectId()) : null;
    }

    public String getDesc() {
        return desc;
    }


    @JsonProperty("id")
    public String getValue() {
        return value;
    }

}
