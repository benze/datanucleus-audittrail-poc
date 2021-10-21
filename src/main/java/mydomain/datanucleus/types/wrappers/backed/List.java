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
    private transient CollectionChangeTrackerImpl changeTracker;

    public List(ObjectProvider ownerOP, AbstractMemberMetaData mmd) {
        super(ownerOP, mmd);
        changeTracker = new CollectionChangeTrackerImpl( this, true, true, false);
        changeTracker.startTracking();
    }

    @Override
    public ChangeTracker getChangeTracker() {
        return changeTracker;
    }

    @Override
    public void add(int index, E element) {
        super.add(index, element);
        add(changeTracker, element, true);
    }

    @Override
    public boolean add(E element) {
        boolean result = super.add(element);
        return add( changeTracker, element, result);
    }

    @Override
    public boolean addAll(Collection elements) {
        boolean result = super.addAll(elements);
        return add( changeTracker, elements,  result);
    }

    @Override
    public boolean addAll(int index, Collection elements) {
        boolean result = super.addAll(index, elements);
        return add( changeTracker, elements, result);
    }

    @Override
    public void clear() {
        java.util.List copy = new ArrayList(delegate);
        super.clear();
        remove(changeTracker, copy, true);
    }

    @Override
    public boolean remove(Object element, boolean allowCascadeDelete) {
        boolean result = super.remove(element, allowCascadeDelete);
        return remove( changeTracker, element, result);
    }

    @Override
    public E remove(int index) {
        E element = super.remove(index);
        remove( changeTracker, element, true);
        return element;
    }

    @Override
    public E set(int index, E element, boolean allowDependentField) {
        E orig = super.set(index, element, allowDependentField);
        if( orig != element ) {
            remove(changeTracker, orig, true);
            add(changeTracker, element, true);
        }
        return orig;
    }

    @Override
    public E set(int index, E element) {
        E orig = super.set(index, element);
        if( orig != element ) {
            remove(changeTracker, orig, true);
            add(changeTracker, element, true);
        }
        return orig;
    }

    @Override
    public boolean removeAll(Collection elements) {
        boolean result = super.removeAll(elements);
        return remove( changeTracker, elements, result);
    }

    @Override
    public void replaceAll(UnaryOperator<E> operator) {
        super.replaceAll(operator);
    }
}
