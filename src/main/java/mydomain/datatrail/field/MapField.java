package mydomain.datatrail.field;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.datanucleus.metadata.FieldMetaData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.List;
import java.util.Map;
import java.util.Set;

// https://wada-ama.atlassian.net/wiki/spaces/AR/pages/1310949916/Adams+Data+Trail#Collection-fields
public class MapField extends Field{


    /**
     * Class to represent the key/value information found in the map.  Must not implement a Map.Entry as Jackson will automatically serialize those
     * differently
     */
    class Entry {
        Field key;
        Field value;

        public Entry(Field key, Field value) {
            this.key = key;
            this.value = value;
        }

        // TODO change return type to Field
        @JsonProperty("key")
        public Object getKey() {
            return key;
        }

        // TODO change return type to Field
        @JsonProperty("value")
        public Object getValue() {
            return value;
        }
    }

    private List<Entry> contents = new ArrayList<>();

    protected MapField(String fieldName, String className) {
        super(fieldName, className);
    }

    protected MapField(Object field, FieldMetaData fmd) {
        super(fmd.getName(),null);
        type = Type.MAP;

        if( field != null ) {
            addElements( ((Map)field).entrySet());
        }

    }

    /**
     * Adds all the elements in the collection
     * @param elements
     */
    private void addElements( Set<Map.Entry> elements ){
        for( Map.Entry element : elements){
            Field key = Field.newField(element.getKey());
            Field value = Field.newField(element.getValue());

            contents.add( new Entry(key, value));
        }
    }


    protected String desc;

    public String getDesc() {
        return desc;
    }


    @JsonIgnore
    public String getValue(){
        return value;
    }

    public List<Entry> getContents() {
        return contents;
    }

    @Override
    public void updateValue() {
        contents.stream().forEach(entry -> {
            entry.key.updateValue();
            entry.value.updateValue();
        });
    }
}
