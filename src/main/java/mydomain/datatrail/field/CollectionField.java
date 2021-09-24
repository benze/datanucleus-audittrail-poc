package mydomain.datatrail.field;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.datanucleus.metadata.FieldMetaData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

// https://wada-ama.atlassian.net/wiki/spaces/AR/pages/1310949916/Adams+Data+Trail#Collection-fields
public class CollectionField extends Field{

    protected List<Field> elements = new ArrayList<>();


    protected CollectionField(Object field, FieldMetaData fmd) {
        super(fmd.getName(),null);
        type = Type.COLLECTION;

        if( fmd.hasArray()){
            addElements(Arrays.asList((Object []) field));
        }

        if( fmd.hasCollection() ){
            addElements((Collection) field);
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
    @JsonProperty("elements")
    public List<Field> getElements(){
        return elements;
    }


    /**
     * Adds all the elements in the collection
     * @param elements
     */
    private void addElements( Collection elements ){
        for( Object element : elements){
            this.elements.add(Field.newField(element));
        }
    }


    @Override
    public String toString() {
        return "CollectionField{" +
                "elements=" + elements +
                '}';
    }
}
