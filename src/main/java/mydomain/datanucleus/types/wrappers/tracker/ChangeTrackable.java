package mydomain.datanucleus.types.wrappers.tracker;

public interface ChangeTrackable {

    ChangeTracker getChangeTracker();


    /**
     *
     * @param changeTracker
     * @param element either an individual element, or an Iterable collection/list of elements
     * @param added
     * @return
     */
    default boolean add(final CollectionChangeTrackerImpl changeTracker, final Object element, final boolean added){
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
    default boolean remove(final CollectionChangeTrackerImpl changeTracker, final Object element, final boolean removed){
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
