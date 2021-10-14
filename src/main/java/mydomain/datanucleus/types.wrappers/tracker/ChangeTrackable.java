package mydomain.datanucleus.type.wrappers.tracker;

import mydomain.datanucleus.type.wrappers.tracker.ChangeTracker;
import mydomain.datanucleus.type.wrappers.tracker.CollectionChangeTrackerImpl;

public interface ChangeTrackable {

    ChangeTracker getChangeTracker();


    // TODO Hack job to inject the add

    /**
     *
     * @param changeTracker
     * @param element either an individual element, or an Iterable collection/list of elements
     * @param added
     * @return
     */
    default boolean add(CollectionChangeTrackerImpl changeTracker, Object element, boolean added){
        if( added ){
            if( element instanceof Iterable){
                ((Iterable)element).forEach(o -> add( changeTracker, o, added));
            } else {
                changeTracker.add(element);
            }
        }

        return added;
    }


    /**
     *
     * @param changeTracker
     * @param element either an individual element, or an Iterable collection/list of elements
     * @param removed
     * @return
     */
    default boolean remove(CollectionChangeTrackerImpl changeTracker,  Object element, boolean removed){
        if( removed ){
            if( element instanceof Iterable){
                ((Iterable)element).forEach(o -> remove( changeTracker, o, removed));
            } else {
                changeTracker.remove(element);
            }
        }

        return removed;
    }
}
