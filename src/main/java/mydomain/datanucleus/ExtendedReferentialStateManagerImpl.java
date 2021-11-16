package mydomain.datanucleus;

import org.datanucleus.ExecutionContext;
import org.datanucleus.enhancement.Persistable;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.state.ReferentialStateManagerImpl;
import org.datanucleus.util.ClassUtils;

/**
 * Extended state manager used to provide access to the SavedImage of the Persistable instance
 * and to update the SavedImage when unloaded fields are loaded
 */


// ObjectProvider.ORIGINAL_FIELD_VALUE_KEY_PREFIX

public class ExtendedReferentialStateManagerImpl extends ReferentialStateManagerImpl {
    public ExtendedReferentialStateManagerImpl(final ExecutionContext ec, final AbstractClassMetaData cmd) {
        super(ec, cmd);
    }

    /**
     * Ensure that loaded fields are always pushed into the savedImage
     * @param fieldNumbers
     */
    @Override
    protected void loadFieldsFromDatastore(final int[] fieldNumbers) {
        super.loadFieldsFromDatastore(fieldNumbers);
        updateSavedFields();
    }

    /**
     * Ensure that loaded fields from the L2 cache are pushed into the savedImage
     * @param fieldNumbers
     * @return
     */
    @Override
    protected int[] loadFieldsFromLevel2Cache(final int[] fieldNumbers) {
        final int[] unloadedFields = super.loadFieldsFromLevel2Cache(fieldNumbers);
        // if unloadedFields != null, then normally the call will then call the loadFieldsFromDatastore() method, so
        // could potentially skip the savedImage update here and only make the call in loadFieldsFromDatastore().  But this is a safer approach
        // to explicitly call it here in all circumstances as well.
        updateSavedFields();

        return unloadedFields;
    }

    /**
     * Whenever a field is updated, make sure that the old value exists in the saved image first
     * @param pc
     * @param fieldNumber
     * @param value
     */
    @Override
    protected void updateField(final Persistable pc, final int fieldNumber, final Object value) {
        // check to see if the field has already been loaded and saved in the backup image
        if( savedLoadedFields == null || !savedLoadedFields[fieldNumber]){
            // field has not already been loaded, so retrieve it
            loadFieldsFromDatastore( new int[]{fieldNumber});
        }

        super.updateField(pc, fieldNumber, value);
    }

    /**
     * Update the saved image of the PC whenever new fields are loaded
     */
    protected void updateSavedFields(){
        // if there is not a saved image, ensure that it is triggered
        if( savedLoadedFields == null ){
            saveFields();
        }

        // identify all the fields left to copy
        final int[] fieldsToCopy = ClassUtils.getFlagsSetTo(savedLoadedFields, ClassUtils.getFlagsSetTo(loadedFields,true), false);
        if( fieldsToCopy == null || fieldsToCopy.length == 0){
            return;
        }

        savedImage.dnCopyFields(myPC, fieldsToCopy);
        savedLoadedFields = loadedFields.clone();
    }


    /**
     * Retrieve a field from the saved image
     * @param fieldNumber
     * @return
     */
    public Object provideSavedField(final int fieldNumber) {
        return provideField(savedImage, fieldNumber);
    }
}
