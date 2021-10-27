package mydomain.datanucleus.types.wrappers.backed;

import mydomain.datanucleus.types.wrappers.tracker.ChangeTrackable;
import mydomain.datanucleus.types.wrappers.tracker.ChangeTracker;
import mydomain.datanucleus.types.wrappers.tracker.MapChangeTrackerImpl;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.state.ObjectProvider;

public class Map<K,V> extends org.datanucleus.store.types.wrappers.backed.Map<K,V> implements ChangeTrackable {

    private MapChangeTrackerImpl changeTracker;

    /**
     * Constructor, using the ObjectProvider of the "owner" and the field name.
     *
     * @param op  The owner ObjectProvider
     * @param mmd Metadata for the member
     */
    public Map(ObjectProvider op, AbstractMemberMetaData mmd) {
        super(op, mmd);
        changeTracker = new MapChangeTrackerImpl(this, false);
        changeTracker.startTracking();
    }

    @Override
    public V put(K key, V value) {
        V old = super.put(key, value);
        if( old == null ){
            // no previous value, so this is a new addition
            changeTracker.added(key, value);
        } else {
            // was already a value with this key
            changeTracker.changed(key, old, value);
        }

        return old;
    }

    @Override
    public void putAll(java.util.Map m) {
        m.entrySet().stream().forEach( o  -> {
            Entry kvEntry = ((Map.Entry)o);
            if( this.containsKey(kvEntry.getKey())){
                // map already contains the value, so the value is changing
                changeTracker.changed(kvEntry.getKey(), this.get(kvEntry.getKey()), kvEntry.getValue());
            } else {
                changeTracker.added(kvEntry.getKey(), kvEntry.getValue());
            }
        });
        super.putAll(m);
    }

    @Override
    public V remove(Object key) {
        V old = super.remove(key);
        changeTracker.removed(key, old);
        return old;
    }

    @Override
    public void clear() {
        // loop through all the values in the delegate set and add them to the tracker before they are cleared
        delegate.entrySet().stream().forEach(kvEntry -> changeTracker.removed(kvEntry.getKey(), kvEntry.getValue()));
        super.clear();
    }

    @Override
    public void updateEmbeddedKey(K key, int fieldNumber, Object newValue, boolean makeDirty) {
        super.updateEmbeddedKey(key, fieldNumber, newValue, makeDirty);
    }

    @Override
    public void updateEmbeddedValue(V value, int fieldNumber, Object newValue, boolean makeDirty) {
        super.updateEmbeddedValue(value, fieldNumber, newValue, makeDirty);
    }

    @Override
    public ChangeTracker getChangeTracker() {
        return changeTracker;
    }


}