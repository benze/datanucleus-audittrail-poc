/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package mydomain.datanucleus.types.wrappers.tracker;

import org.datanucleus.exceptions.NucleusException;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Default {@link mydomain.datanucleus.type.wrappers.tracker.CollectionChangeTracker}.
 *
 * Imported from the OpenJPA project (org.apache.openjpa.util.CollectionChangeTrackerImpl)
 *
 * @author Abe White
 */
public class CollectionChangeTrackerImpl
    extends AbstractChangeTracker
    implements CollectionChangeTracker {

    protected final Collection _coll;
    protected final boolean _dups;
    protected final boolean _order;

    /**
     * Constructor.
     *
     * @param coll the collection to delegate to
     * @param dups true if the collection allows duplicates, false otherwise
     * @param order true if the collection is ordered, false otherwise
     */
    public CollectionChangeTrackerImpl(final Collection coll, final boolean dups,
                                       final boolean order, final boolean autoOff) {
        _coll = coll;
        _dups = dups;
        _order = order;
        setAutoOff(autoOff);
    }

    /**
     * Whether the underlying collection allows duplicates.
     */
    public boolean allowsDuplicates() {
        return _dups;
    }

    /**
     * Whether the underlying collection is ordered.
     */
    public boolean isOrdered() {
        return _order;
    } 

    @Override
    public void added(final Object elem) {
        super.added(elem);
    }

    @Override
    public void removed(final Object elem) {
        super.removed(elem);
    }

    @Override
    protected int initialSequence() {
        if (_order)
            return _coll.size();
        return super.initialSequence();
    }

    protected void add(final Object elem) {
        if (rem == null || !rem.remove(elem)) {
            // after a point it's inefficient to keep tracking
            if (getAutoOff()
                && getAdded().size() + getRemoved().size() >= _coll.size())
                stopTracking();
            else {
                if (add == null) {
                    if (_dups || _order)
                        add = new ArrayList();
                    else
                        add = newSet();
                }
                add.add(elem);
            }
        } else if (_order)
            stopTracking();
        else {
            if (change == null)
                change = newSet();
            change.add(elem);
        }
    }

    protected void remove(final Object elem) {
        // if the collection contains multiple copies of the elem, we can't
        // use change tracking because some back-ends can't just delete a
        // single copy of a elem
        if (_dups && getAutoOff() && _coll.contains(elem))
            stopTracking();
        else if (add == null || !add.remove(elem)) {
            // after a point it's inefficient to keep tracking
            if (getAutoOff()
                && getRemoved().size() + getAdded().size() >= _coll.size())
                stopTracking();
            else {
                if (rem == null)
                    rem = newSet();
                rem.add(elem);
            }
        }
    }

    protected void change(final Object elem) {
        throw new NucleusException("Cannot change an element of a collection; only add or remove");
    }
}
