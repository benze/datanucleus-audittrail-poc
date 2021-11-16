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

import org.apache.commons.collections.set.MapBackedSet;

import javax.jdo.JDOHelper;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Set;

/**
 * Base class that provides utilities to change trackers.
 * Imported from OpenJPA project (org.apache.openjpa.util.AbstractChangeTracker)
 *
 * @author Abe White
 */
public abstract class AbstractChangeTracker
    implements ChangeTracker {

    /**
     * Collection of added items. May be null.
     */
    protected Collection add;

    /**
     * Collection of removed items. May be null.
     */
    protected Collection rem;

    /**
     * Collection of changed items. May be null.
     */
    protected Collection change;

    private boolean _autoOff = true;
    private boolean _track;
    private Boolean _identity;
    private int _seq = -1;


    /**
     * Whether to automatically stop tracking when the number of changes
     * exceeds the container size. Defaults to true.
     */
    public boolean getAutoOff() {
        return _autoOff;
    }

    /**
     * Whether to automatically stop tracking when the number of changes
     * exceeds the container size. Defaults to true.
     */
    public void setAutoOff(final boolean autoOff) {
        _autoOff = autoOff;
    }

    public boolean isTracking() {
        return _track;
    }

    public void startTracking() {
        _track = true;
        if (_seq == -1)
            _seq = initialSequence();
        reset();
    }

    /**
     * Return the initial sequence value for this proxy. Typically this is
     * the container size. Assumes an unordered collection by default,
     * returning 0.
     */
    protected int initialSequence() {
        return 0;
    }

    public void stopTracking() {
        _track = false;
        _seq = -1;
        reset();
    }

    /**
     * Reset the state of the tracker.
     */
    protected void reset() {
        if (add != null)
            add.clear();
        if (rem != null)
            rem.clear();
        if (change != null)
            change.clear();
        _identity = null;
    }

    public Collection getAdded() {
        return (add == null) ? Collections.emptyList() : add;
    }

    public Collection getRemoved() {
        return (rem == null) ? Collections.emptyList() : rem;
    }

    public Collection getChanged() {
        return (change == null) ? Collections.emptyList() : change;
    }

    /**
     * Notify the tracker that the given object was added.
     */
    protected void added(final Object val) {
        if (!_track)
            return;
        setIdentity(val);
        add(val);
    }

    /**
     * Mark the given value as added.
     */
    protected abstract void add(Object val);

    /**
     * Notify the tracker that the given object was removed.
     */
    protected void removed(final Object val) {
        if (!_track)
            return;
        setIdentity(val);
        remove(val);
    }

    /**
     * Mark the given value as removed.
     */
    protected abstract void remove(Object val);

    /**
     * Notify the tracker that the given object was changed.
     */
    protected void changed(final Object val) {
        if (!_track)
            return;
        setIdentity(val);
        change(val);
    }

    /**
     * Mark the given value as changed.
     */
    protected abstract void change(Object val);

    public int getNextSequence() {
        return _seq;
    }

    public void setNextSequence(final int seq) {
        _seq = seq;
    }

    /**
     * Create a new set for storing adds/removes/changes. Takes into account
     * whether we need to use an identity set or standard set.
     */
    protected Set newSet() {
        if (_identity == Boolean.TRUE)
            return MapBackedSet.decorate(new IdentityHashMap());
        return new HashSet();
    }

    /**
     * Set whether to use identity-based datastructures, and switch our current
     * datastructures appropriately if needed. We use identity structures for
     * PC types in case the user has coded them such that two objects with
     * different identities can compare equals().
     */
    private void setIdentity(final Object val) {
        if (val == null || _identity != null)
            return;

        if (JDOHelper.isPersistent(val))
            _identity = Boolean.TRUE;
        else
            _identity = Boolean.FALSE;

        add = AbstractChangeTracker.switchStructure(add, _identity.booleanValue());
        rem = AbstractChangeTracker.switchStructure(rem, _identity.booleanValue());
        change = AbstractChangeTracker.switchStructure(change, _identity.booleanValue());
    }

    /**
     * Switch from an identity structure to a standard one, or vice versa.
     */
    private static Collection switchStructure(final Collection cur,
                                              final boolean identity) {
        if (cur == null)
            return null;
        if (identity && cur instanceof HashSet) {
            if (cur.isEmpty())
                return null;
            final Set replace = MapBackedSet.decorate(new IdentityHashMap());
            replace.addAll(cur);
            return replace;
        }
        if (!identity && !(cur instanceof HashSet) && cur instanceof Set) {
            if (cur.isEmpty())
                return null;
            return new HashSet(cur);
		}
		return cur;
	}
}

