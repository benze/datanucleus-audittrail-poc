package mydomain.datatrail.field;

import org.datanucleus.metadata.FieldMetaData;

// https://wada-ama.atlassian.net/wiki/spaces/AR/pages/1310949916/Adams+Data+Trail#Collection-fields
public class MapField extends Field{

    protected MapField(String fieldName, String className) {
        super(fieldName, className);
    }

    protected MapField(Object field, FieldMetaData fmd) {
        super(null,null);
//        super(fmd);

        type = Type.MAP;
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

}
