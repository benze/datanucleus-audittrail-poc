package mydomain.datatrail.field;

import com.fasterxml.jackson.annotation.JsonProperty;
import mydomain.model.ITrailDesc;
import org.datanucleus.enhancement.Persistable;
import org.datanucleus.metadata.FieldMetaData;

import java.lang.ref.WeakReference;

public class ReferenceField extends Field {

    protected String description;

    private WeakReference<Persistable> source = new WeakReference<>(null);


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
        if( field == null ){
            // nothing to do
            return;
        }

        // TODO JDOHelper.getObjectId()
        value = getObjectId(field.dnGetObjectId());

        // retain a reference to the original Persistable field
        setSource( field );
    }

    private void setPrevValue( Persistable field){
        if( field != null ){
            // TODO JDOHelper.getObjectId()
            prev = getObjectId(field.dnGetObjectId());
        }
    }


    private void setSource( Persistable pc){
        source = new WeakReference<>(pc);
    }

    private Persistable getSource(){
        return source != null ? source.get() : null;
    }

    @Override
    public void updateValue() {
        setValue(source.get());
    }
}
