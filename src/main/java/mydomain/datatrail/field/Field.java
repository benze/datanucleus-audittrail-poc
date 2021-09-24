package mydomain.datatrail.field;

import org.datanucleus.enhancement.Persistable;
import org.datanucleus.identity.DatastoreId;
import org.datanucleus.identity.IdentityUtils;
import org.datanucleus.metadata.FieldMetaData;

abstract public class Field {
    enum Type{
        REF,
        MAP,
        COLLECTION,
        PRIMITIVE;
    }

    protected String name;
    protected Type type;
    protected String className;
    protected String value;
    protected String prev;


    protected Field(String fieldName, String className){
       this.name = fieldName;
       this.className = className;
    }


    /**
     * No metadata associated to the field, so can only store reference and primitive values
     * @param field
     * @return
     */
    static public Field newField(Object field) {
        if (field instanceof Persistable) {
            return new ReferenceField((Persistable) field, null);
        }

        // default case, treat as primitive field
        return new PrimitiveField(field, null);
    }


    /**
     *
     * @param field
     * @param fmd
     * @return
     */
    static public Field newField(Object field, FieldMetaData fmd){
        if( fmd == null )
            return newField(field);

        if( fmd.hasMap()) {
            return new MapField(field, fmd);
        } else if( fmd.hasArray() || fmd.hasCollection()) {
            return new CollectionField(field, fmd);
        } else if (field instanceof Persistable) {
            return new ReferenceField((Persistable) field, fmd.getName());
        }

        // default case, treat as primitive field
        return new PrimitiveField(field, fmd.getName());
    }


    protected String getObjectId(Object objectId){
        if(IdentityUtils.isDatastoreIdentity( objectId ) ) {
            return ((DatastoreId) objectId).getKeyAsObject().toString();
        } else {
            return objectId.toString();
        }
    }

    @Override
    public String toString() {
        return "Field{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", className='" + className + '\'' +
                ", value='" + value + '\'' +
                ", prev='" + prev + '\'' +
                '}';
    }



    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public String getClassName() {
        return className;
    }

    public String getValue() {
        return value;
    }

    public String getPrev() {
        return prev;
    }
}
