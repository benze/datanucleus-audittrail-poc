package mydomain.datatrail.field;

import com.fasterxml.jackson.annotation.JsonProperty;
import mydomain.model.ITrailDesc;
import org.datanucleus.enhancement.Persistable;
import org.datanucleus.metadata.FieldMetaData;

public class ReferenceField extends Field {

    protected String description;

    protected ReferenceField(Persistable field, Persistable prevValue, FieldMetaData fmd) {
        super(fmd != null ? fmd.getName() : null, field != null ? field.getClass().getName() : null );
        type = Type.REF;
        setValue(field);
        setPrevValue(prevValue);
        setDescription(field);

    }

    @JsonProperty("desc")
    public String getDescription() {
        return description;
    }


    private void setDescription(Persistable field){
        if( field != null && field instanceof ITrailDesc){
            description = ((ITrailDesc)field).minimalTxtDesc();
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

    private void setPrevValue( Persistable field){
        if( field != null ){
            prev = getObjectId(field.dnGetObjectId());
        }
    }

}
