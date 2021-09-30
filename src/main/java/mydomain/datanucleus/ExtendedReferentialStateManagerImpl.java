package mydomain.datanucleus;

import org.datanucleus.ExecutionContext;
import org.datanucleus.enhancement.Persistable;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.state.ReferentialStateManagerImpl;
import org.datanucleus.util.ClassUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Extended state manager used to provide access to the SavedImage of the Persistable instance
 * and to update the SavedImage when unloaded fields are loaded
 */


// ObjectProvider.ORIGINAL_FIELD_VALUE_KEY_PREFIX

public class ExtendedReferentialStateManagerImpl extends ReferentialStateManagerImpl {
    public ExtendedReferentialStateManagerImpl(ExecutionContext ec, AbstractClassMetaData cmd) {
        super(ec, cmd);
    }

    public Persistable getSavedImage(){
        return savedImage;
    }

    @Override
    public void loadUnloadedFields() {
        super.loadUnloadedFields();

        // if there is a saved image, ensure that all missing fields are now made available in the saved image as well
        if( savedLoadedFields == null )
            return;

        // identify all the fields left to copy
        int[] fieldsToCopy = ClassUtils.getFlagsSetTo(savedLoadedFields, cmd.getAllMemberPositions(), false);
        if( fieldsToCopy == null || fieldsToCopy.length == 0){
            return;
        }

        savedImage.dnCopyFields(myPC, fieldsToCopy);
        savedLoadedFields = loadedFields.clone();
    }

    public Object provideSavedField(int fieldNumber) {
        return provideField(savedImage, fieldNumber);
    }
}
