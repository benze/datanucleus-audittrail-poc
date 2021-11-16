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

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Default {@link MapChangeTracker}.
 *
 * Imported from the OpenJPA project (org.apache.openjpa.util.MapChangeTrackerImpl)
 *
 * @author Abe White
 * @author Eric Benzacar
 */
public class MapChangeTrackerImpl
    extends AbstractChangeTracker
    implements MapChangeTracker {

    private final Map _map;
    private boolean _keys = true;
    private final Map _prevValues = new HashMap();

    /**
     * Constructor; supply delegate map.
     */
    public MapChangeTrackerImpl(final Map map, final boolean autoOff) {
        _map = map;
        setAutoOff(autoOff);
    }

    public boolean getTrackKeys() {
        return _keys;
    }

    public void setTrackKeys(final boolean keys) {
        _keys = keys;
    }

    public void added(final Object key, final Object val) {
        if (_keys)
            added(key);
        else
            added(val);

        _prevValues.put(key, val);
    }

    public void removed(final Object key, final Object val) {
        if (_keys)
            removed(key);
        else
            removed(val);

        _prevValues.putIfAbsent(key, val);

    }

    public void changed(final Object key, final Object oldVal, final Object newVal) {
        if (_keys)
            changed(key);
        else {
            removed(oldVal);
            added(newVal);
        }

        // if this is a new addition, so there is no original value that should be retained
        if( add != null && add.contains(key) ) {
            _prevValues.put(key, newVal);
        } else {
            // this is a change or removal, then only set the value the first time
            _prevValues.putIfAbsent(key, oldVal);
        }
    }


    protected void add(final Object obj) {
        // if the key was previously removed and now added back, mark
        // it as a change; otherwise it's a new addition
        if (rem != null && rem.remove(obj)) {
            if (change == null)
                change = newSet();
            change.add(obj);
        } else {
            // after a point it becomes inefficient to track changes
            if (getAutoOff() && getAdded().size() + getChanged().size()
                + getRemoved().size() >= _map.size())
                stopTracking();
            else {
                if (add == null)
                    add = newSet();
                add.add(obj);
            }
        }
    }

    protected void remove(final Object obj) {
        // no longer a change, if it was before
        if (change != null)
            change.remove(obj);

        // if it was a new addition, just forget it; otherwise remember
        // that it was removed
        if (add == null || !add.remove(obj)) {
            // after a point it becomes inefficient to track changes
            if (getAutoOff() && getAdded().size() + getChanged().size()
                + getRemoved().size() >= _map.size())
                stopTracking();
            else {
                if (rem == null)
                    rem = newSet();
                rem.add(obj);
            }
        }
    }

    protected void change(final Object key) {
        // if the key is already changed or the key is newly added, nothing
        // to do
        if ((change != null && change.contains(key))
            || (add != null && add.contains(key)))
            return;

        // after a point it becomes inefficient to track changes
        if (getAutoOff() && getAdded().size() + getChanged().size()
            + getRemoved().size() >= _map.size())
            stopTracking();
        else {
            // record the change
            if (change == null)
                change = newSet();
            change.add(key);
        }
    }


    @Override
    public Collection getChanged() {
        return (Collection) super.getChanged().stream().map(key -> new AbstractMap.SimpleEntry(key, _prevValues.get(key))).collect(Collectors.toSet());
    }

    @Override
    public Collection getAdded() {
        return (Collection) super.getAdded().stream().map(key -> new AbstractMap.SimpleEntry(key, _prevValues.get(key))).collect(Collectors.toSet());
    }

    @Override
    public Collection getRemoved() {
        return (Collection) super.getRemoved().stream().map(key -> new AbstractMap.SimpleEntry(key, _prevValues.get(key))).collect(Collectors.toSet());
    }
}
