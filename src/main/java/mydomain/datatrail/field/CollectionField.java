package mydomain.datatrail.field;

import org.datanucleus.metadata.FieldMetaData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// https://wada-ama.atlassian.net/wiki/spaces/AR/pages/1310949916/Adams+Data+Trail#Collection-fields
public class CollectionField extends Field{

    protected List<Field> elements = new ArrayList<>();

    protected CollectionField(String fieldName, String className) {
        super(fieldName, className);
    }

    protected CollectionField(Object field, FieldMetaData fmd) {
//        super(fmd);
        super(fmd.getName(),null);
        type = Type.COLLECTION;

        if( fmd.hasArray() &&  fmd.getContainer().getMemberMetaData() instanceof FieldMetaData){
            addElements(Arrays.asList((Object []) field));
        }

        value = field != null ? field.toString() : null ;

        // fmd
    }


    protected String desc;

    public String getDesc() {
        return desc;
    }


    // @JsonProperty("id")
    public String getValue(){
        return value;
    }

    private void addElements( List elements ){
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
