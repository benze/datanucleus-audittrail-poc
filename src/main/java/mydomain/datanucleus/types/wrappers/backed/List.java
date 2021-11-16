package mydomain.datanucleus.types.wrappers.backed;

import mydomain.datanucleus.types.wrappers.tracker.ChangeTrackable;
import mydomain.datanucleus.types.wrappers.tracker.ChangeTracker;
import mydomain.datanucleus.types.wrappers.tracker.CollectionChangeTrackerImpl;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.state.ObjectProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.UnaryOperator;

public class List<E> extends org.datanucleus.store.types.wrappers.backed.List<E> implements ChangeTrackable {
    private final transient CollectionChangeTrackerImpl changeTracker;

    public List(final ObjectProvider ownerOP, final AbstractMemberMetaData mmd) {
        super(ownerOP, mmd);
        changeTracker = new CollectionChangeTrackerImpl( this, true, true, false);
        changeTracker.startTracking();
    }

    @Override
    public ChangeTracker getChangeTracker() {
        return changeTracker;
    }

    @Override
    public void add(final int index, final E element) {
        super.add(index, element);
        add(changeTracker, element, true);
    }

    @Override
    public boolean add(final E element) {
        final boolean result = super.add(element);
        return add( changeTracker, element, result);
    }

    @Override
    public boolean addAll(final Collection elements) {
        final boolean result = super.addAll(elements);
        return add( changeTracker, elements,  result);
    }

    @Override
    public boolean addAll(final int index, final Collection elements) {
        final boolean result = super.addAll(index, elements);
        return add( changeTracker, elements, result);
    }

    @Override
    public void clear() {
        final java.util.List copy = new ArrayList(this);
        super.clear();
        remove(changeTracker, copy, true);
    }

    @Override
    public boolean remove(final Object element, final boolean allowCascadeDelete) {
        final boolean result = super.remove(element, allowCascadeDelete);
        return remove( changeTracker, element, result);
    }

    @Override
    public E remove(final int index) {
        final E element = super.remove(index);
        remove( changeTracker, element, true);
        return element;
    }

    @Override
    public E set(final int index, final E element, final boolean allowDependentField) {
        final E orig = super.set(index, element, allowDependentField);
        if( orig != element ) {
            remove(changeTracker, orig, true);
            add(changeTracker, element, true);
        }
        return orig;
    }

    @Override
    public E set(final int index, final E element) {
        final E orig = super.set(index, element);
        if( orig != element ) {
            remove(changeTracker, orig, true);
            add(changeTracker, element, true);
        }
        return orig;
    }

    @Override
    public boolean removeAll(final Collection elements) {
        final boolean result = super.removeAll(elements);
        return remove( changeTracker, elements, result);
    }
}
