package mydomain.datatrail.field;

import mydomain.datatrail.Node;
import org.datanucleus.enhancement.Persistable;
import org.datanucleus.identity.DatastoreId;
import org.datanucleus.identity.IdentityUtils;
import org.datanucleus.metadata.FieldMetaData;

import java.lang.ref.WeakReference;

abstract public class Field extends Node {
    public enum Type{
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
       this.nodeType = Node.Type.FIELD;
    }


    // TODO temporary to be deleted.  Used to make code compilable during refactor process
    @Deprecated
    static public Field newField(Object field) {
        return newField(field, null);
    }

        /**
         * No metadata associated to the field, so can only store reference and primitive values
         * @param field
         * @return
         */
    static public Field newField(Object field, Object prevValue) {
        if (field instanceof Persistable) {
            return new ReferenceField((Persistable) field,  (Persistable) prevValue, null);
        }

        // default case, treat as primitive field
        return new PrimitiveField(field != null ? field : null, prevValue,null);
    }


    /**
     *
     * @param currentValue
     * @param fmd
     * @return
     */
    static public Field newField(Object currentValue, Object prevValue, FieldMetaData fmd){
        if( fmd == null )
            return newField(currentValue, prevValue);

        if( fmd.hasMap()) {
            return new MapField(currentValue, fmd);
        } else if( fmd.hasArray() || fmd.hasCollection()) {
            return new CollectionField(currentValue, fmd);
        } else if (fmd.hasMap()){
            return new MapField(currentValue, fmd);

            // TODO replace with JDOHelper.isPersitable()
        } else if (Persistable.class.isAssignableFrom(fmd.getType())) {
            return new ReferenceField((Persistable) currentValue, (Persistable) prevValue, fmd);
        }

        // default case, treat as primitive field
        return new PrimitiveField(currentValue, prevValue, fmd.getName());
    }


    // TODO rename to getObjectIdAsString()
    protected String getObjectId(Object objectId){
        if( objectId == null )
            return null;
        else if(IdentityUtils.isDatastoreIdentity( objectId ) ) {
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



    /**
     * Method to update the value with the latest information from the referenced source object
     */
    public void updateValue(){
        // empty default method does nothing
    };

}
