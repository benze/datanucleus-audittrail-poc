package mydomain.datatrail.field;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.datanucleus.metadata.FieldMetaData;
import mydomain.datanucleus.type.wrappers.tracker.ChangeTrackable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

// https://wada-ama.atlassian.net/wiki/spaces/AR/pages/1310949916/Adams+Data+Trail#Collection-fields
public class CollectionField extends Field{

    protected Collection<Field> added = new ArrayList<>();
    protected Collection<Field> removed = new ArrayList<>();



    protected CollectionField(Object field, FieldMetaData fmd) {
        super(fmd.getName(),null);
        type = Type.COLLECTION;

        if( field != null ) {
            if (fmd.hasArray()) {
                addElements(Arrays.asList((Object[]) field));
            }

            if (fmd.hasCollection()) {
                addElements((Collection) field);
            }
        }
    }


    /**
     * Ignore the value property for this field.  The values are stored in the Elements list instead
     * @return
     */
    @JsonIgnore
    public String getValue(){
        return value;
    }


    /**
     * Elements which are part of this collection
     * @return
     */
    @JsonProperty("added")
    public Collection<Field> getAdded(){
        return added;
    }

    @JsonProperty("removed")
    public Collection<Field> getRemoved() {
        return removed;
    }

    /**
     * Adds all the elements in the collection
     * @param elements
     */
    private void addElements( Collection elements ){
        if( elements instanceof ChangeTrackable){
            mydomain.datanucleus.type.wrappers.tracker.ChangeTracker changeTracker = ((ChangeTrackable)elements).getChangeTracker();
            added = (Collection<Field>) changeTracker.getAdded().stream().map(o -> Field.newField(o, null)).collect(Collectors.toList());
            removed = (Collection<Field>) changeTracker.getRemoved().stream().map(o -> Field.newField(o, null)).collect(Collectors.toList());
        } else {
            for (Object element : elements) {
                this.added.add(Field.newField(element));
            }
        }
    }


    @Override
    public String toString() {
        return "CollectionField{" +
                "elements=" + added +
                '}';
    }

    @Override
    public void updateValue() {
        added.stream().forEach(field -> field.updateValue());
    }
}
