package mydomain.datanucleus.types.wrappers.tracker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.iterableWithSize;

class MapChangeTrackerImplTest {

    protected MapChangeTrackerImpl sut;

    @BeforeEach
    void init(){
        sut = new MapChangeTrackerImpl(new HashMap(), false);
        sut.startTracking();

        sut.added("key1", "value1");
        sut.added("key2", "value2");
        sut.removed( "key1", "value1");
        sut.changed("key3", "old3", "value3" );
    }

    @Test
    void added() {

        Collection<Map.Entry> added = sut.getAdded();
        assertThat( added, iterableWithSize(1));
        assertThat(added, contains(new AbstractMap.SimpleEntry("key2", "value2")));
    }

    @Test
    void removed() {
        Collection<Map.Entry> removed = sut.getRemoved();
        assertThat( removed, iterableWithSize(0));

        sut.removed("key4", "value4");
        removed = sut.getRemoved();
        assertThat( removed, iterableWithSize(1));
        assertThat(removed, contains(new AbstractMap.SimpleEntry("key4", "value4")));
    }

    @Test
    void changed() {
        Collection<Map.Entry> changed = sut.getChanged();
        assertThat( changed, iterableWithSize(1));
        assertThat( changed, contains(new AbstractMap.SimpleEntry("key3", "old3")));
    }

}