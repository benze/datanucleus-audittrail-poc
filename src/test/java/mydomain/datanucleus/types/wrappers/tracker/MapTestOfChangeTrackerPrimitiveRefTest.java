package mydomain.datanucleus.types.wrappers.tracker;

import com.spotify.hamcrest.pojo.IsPojo;
import mydomain.datanucleus.datatrail.Node;
import mydomain.datanucleus.datatrail.NodeAction;
import mydomain.datanucleus.datatrail.NodeType;
import org.datanucleus.enhancement.Persistable;
import org.datanucleus.test.AbstractTest;
import org.datanucleus.test.model.QStreet;
import org.datanucleus.test.model.Street;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isA;

public class MapTestOfChangeTrackerPrimitiveRefTest extends AbstractTest {


    protected Map<String, String> ids = new HashMap<>();

    @BeforeEach
    public void createStreets() {
        executeTx(pm -> {
            Street victoria = new Street("Victoria");
            Street younge = new Street("Younge");
            Street hastings = new Street("Hastings");
            pm.makePersistentAll(victoria, younge, hastings);

            ids.put("Victoria", getId((Persistable) victoria));
            ids.put("Younge", getId((Persistable) younge));
            ids.put("Hastings", getId((Persistable) hastings));
        }, false);
    }


    @DisplayName("Adds multiple values to the map.  Only the final value should be tracked.")
    @Test
    public void testAddedTracker() {

        executeTx(pm -> {
            Street victoria = pm.newJDOQLTypedQuery(Street.class).filter(QStreet.candidate().name.eq("Victoria")).executeUnique();
            Street younge = pm.newJDOQLTypedQuery(Street.class).filter(QStreet.candidate().name.eq("Younge")).executeUnique();
            Street hastings = pm.newJDOQLTypedQuery(Street.class).filter(QStreet.candidate().name.eq("Hastings")).executeUnique();

            PrimitiveMapClass sut = new PrimitiveMapClass();
            // make persistent and flush in order to trigger the changeTracket
            pm.makePersistent(sut);
            pm.flush();

            assertThat("A persisted object with a map should be a ChangeTrackable map", sut.getPrimitiveMap(), isA(ChangeTrackable.class));
            ChangeTracker changeTracker = ((ChangeTrackable) sut.getPrimitiveRefMap()).getChangeTracker();

            Map<String, Street> map = sut.getPrimitiveRefMap();
            map.put("key", victoria);
            map.put("key", younge);

            assertThat("Should contain zero changed objects", (Collection<Object>) changeTracker.getChanged(), hasSize(0));
            assertThat("Should contain zero removed objects", (Collection<Object>) changeTracker.getRemoved(), hasSize(0));

            assertThat("Should contain one added object", (Collection<Object>) changeTracker.getAdded(), hasSize(1));
            assertThat("Should contain one added object of type SimpleEntry", (Collection<Object>) changeTracker.getAdded(), hasItems(isA(SimpleEntry.class)));

            assertThat("SimpleEntry should be the last value added", (Collection<Object>) changeTracker.getAdded(), hasItems(new SimpleEntry("key", younge)));
        });


        IsPojo<Node> primMapClass = getEntity(NodeAction.CREATE, PrimitiveMapClass.class, ANY)
                .withProperty("fields", hasItem(
                        getContainerField(NodeType.MAP, "primitiveRefMap")
                                .withProperty("added", hasItems(
                                        getMapElement(NodeType.PRIMITIVE, String.class, "key", NodeType.REF, Street.class, ids.get("Younge"), null)
                                ))
                ));

        assertThat(audit.getModifications(), containsInAnyOrder(primMapClass));
    }


    @DisplayName("Put and remove values in the map in the same tx.  Nothing should be recorded.")
    @Test
    public void testRemovedTracker() {

        executeTx(pm -> {
            Street victoria = pm.newJDOQLTypedQuery(Street.class).filter(QStreet.candidate().name.eq("Victoria")).executeUnique();
            Street younge = pm.newJDOQLTypedQuery(Street.class).filter(QStreet.candidate().name.eq("Younge")).executeUnique();
            Street hastings = pm.newJDOQLTypedQuery(Street.class).filter(QStreet.candidate().name.eq("Hastings")).executeUnique();

            PrimitiveMapClass sut = new PrimitiveMapClass();
            // make persistent and flush in order to trigger the changeTracket
            pm.makePersistent(sut);
            pm.flush();

            assertThat("A persisted object with a map should be a ChangeTrackable map", sut.getPrimitiveMap(), isA(ChangeTrackable.class));
            ChangeTracker changeTracker = ((ChangeTrackable) sut.getPrimitiveMap()).getChangeTracker();


            Map<String, Street> map = sut.getPrimitiveRefMap();

            map.put("key", victoria);
            map.remove("key");


            assertThat("Should contain zero changed objects", (Collection<Object>) changeTracker.getChanged(), hasSize(0));
            assertThat("Should contain zero removed objects", (Collection<Object>) changeTracker.getRemoved(), hasSize(0));
            assertThat("Should contain zero added objects", (Collection<Object>) changeTracker.getAdded(), hasSize(0));
        });


        IsPojo<Node> primMapClass = getEntity(NodeAction.CREATE, PrimitiveMapClass.class, ANY)
                .withProperty("fields", hasItem(
                        getContainerField(NodeType.MAP, "primitiveRefMap")
                ));

        assertThat(audit.getModifications(), contains(primMapClass));
    }


    @DisplayName("Change a value in the map multiple times.  The initial value prior to the first change should be recorded.")
    @Test
    public void testChangedTracker() {

        executeTx(pm -> {
            Street victoria = pm.newJDOQLTypedQuery(Street.class).filter(QStreet.candidate().name.eq("Victoria")).executeUnique();

            PrimitiveMapClass sut = new PrimitiveMapClass();
            // make persistent and flush in order to trigger the changeTracket
            sut.getPrimitiveRefMap().put("key", victoria);
            pm.makePersistent(sut);
        }, false);


        executeTx(pm -> {
            Street victoria = pm.newJDOQLTypedQuery(Street.class).filter(QStreet.candidate().name.eq("Victoria")).executeUnique();
            Street younge = pm.newJDOQLTypedQuery(Street.class).filter(QStreet.candidate().name.eq("Younge")).executeUnique();
            Street hastings = pm.newJDOQLTypedQuery(Street.class).filter(QStreet.candidate().name.eq("Hastings")).executeUnique();

            PrimitiveMapClass sut = pm.newJDOQLTypedQuery(PrimitiveMapClass.class).executeUnique();
            Map<String, Street> map = sut.getPrimitiveRefMap();

            assertThat("A persisted object with a map should be a ChangeTrackable map", map, isA(ChangeTrackable.class));
            ChangeTracker changeTracker = ((ChangeTrackable) map).getChangeTracker();

            map.put("key", victoria);
            map.put("key", younge);

            assertThat("Should contain 1 changed objects", (Collection<Object>) changeTracker.getChanged(), hasSize(1));
            assertThat("Should contain zero removed objects", (Collection<Object>) changeTracker.getRemoved(), hasSize(0));
            assertThat("Should contain zero added object", (Collection<Object>) changeTracker.getAdded(), hasSize(0));

            assertThat("Should contain one changed object of type SimpleEntry", (Collection<Object>) changeTracker.getChanged(), hasItems(isA(SimpleEntry.class)));

            assertThat("SimpleEntry should be the initial value of the map", (Collection<Object>) changeTracker.getChanged(), hasItems(new SimpleEntry("key", victoria)));
        });


        IsPojo<Node> primMapClass = getEntity(NodeAction.UPDATE, PrimitiveMapClass.class, ANY)
                .withProperty("fields", hasItem(
                        getContainerField(NodeType.MAP, "primitiveRefMap")
                                .withProperty("changed", hasItems(
                                        getMapElement(NodeType.PRIMITIVE, String.class, "key", NodeType.REF, Street.class, ids.get("Younge"),
                                                getField(NodeType.REF, Street.class, null,ids.get("Victoria"), null))
                                ))
                ));


        assertThat(audit.getModifications(), contains(primMapClass));
    }
}
