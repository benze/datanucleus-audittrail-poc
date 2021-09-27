package mydomain.datatrail.field;

import com.fasterxml.jackson.annotation.JsonProperty;
import mydomain.model.ITrailDesc;
import org.datanucleus.enhancement.Persistable;
import org.datanucleus.metadata.FieldMetaData;
import org.datanucleus.state.ObjectProvider;

public class ReferenceField extends Field {

    protected String desc;

    protected ReferenceField(Persistable field, FieldMetaData fmd) {
        super(fmd != null ? fmd.getName() : null, field != null ? field.getClass().getName() : null );
        type = Type.REF;
        setValue(field);
        setDesc(field);

    }

    @JsonProperty("desc")
    public String getDesc() {
        return desc;
    }


    private void setDesc(Persistable field){
        if( field != null && field instanceof ITrailDesc){
            desc = ((ITrailDesc)field).minimalTxtDesc();
        }
    }


    @JsonProperty("id")
    public String getValue() {
        return value;
    }

    private void setValue( Persistable field){
        if( field != null ){
            value = getObjectId(field.dnGetObjectId());
        }
    }

}
