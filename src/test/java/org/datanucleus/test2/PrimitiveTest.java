package org.datanucleus.test2;

import com.spotify.hamcrest.pojo.IsPojo;
import mydomain.datanucleus.datatrail2.Node;
import mydomain.datanucleus.datatrail2.NodeType;
import mydomain.model.Street;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;

public class PrimitiveTest extends AbstractTest {
    @Test
    public void createPrimitive() throws IOException {
        executeTx((pm) -> {
            Street street = new Street("Regina");
            pm.makePersistent(street);
            street.setName("Calgary");
        });


        final IsPojo<Node> calgary = getEntity(Node.Action.CREATE, Street.class, "1")
                .withProperty("fields", hasItem(
                        getField(NodeType.PRIMITIVE, String.class, "name", "Calgary", null)
                ));


        Collection<Node> entities = audit.getModifications();
        assertThat(entities, hasItem(calgary));
        assertThat(entities, containsInAnyOrder(calgary));
    }

//
//    @Test
//    public void deletePrimitive() throws IOException {
//        executeTx((pm) -> {
//            Street street = new Street("Regina");
//            pm.makePersistent(street);
//        }, false);
//
//        executeTx(pm -> {
//            Object id = new DatastoreIdImplKodo(Street.class.getName(), 1);
//            Street p = pm.getObjectById(Street.class, id);
//            p.setName("aaaa");
//
//            pm.deletePersistent(p);
//        });
//
//
//        Collection<Entity> entities = audit.getModifications();
//        assertThat(entities, hasSize(1));
//        Entity entity = entities.stream().findFirst().get();
//
//        assertThat(entity, allOf(
//                hasProperty("action", hasToString("DELETE")),
//                hasProperty("id", is("1")),
//                hasProperty("dateModified", notNullValue()),
//                hasProperty("fields", hasSize(1))
//        ));
//
//
//        assertThat(entity.getFields().get(0), allOf(
//                hasProperty("name", is("name")),
//                hasProperty("type", hasToString("PRIMITIVE")),
//                hasProperty("value", is("Regina")),
//                hasProperty("className", is(String.class.getName()))
//        ));
//
//
//        // check that the datatrail log is correct
//        NucleusLogger.GENERAL.info(getJson(audit.getModifications()));
//    }
//
//
//
//    @Test
//    public void updatePrimitive() throws IOException {
//        executeTx((pm) -> {
//            Street street = new Street("Regina");
//            pm.makePersistent(street);
//        }, false);
//
//        executeTx(pm -> {
//            Object id = new DatastoreIdImplKodo(Street.class.getName(), 1);
//            Street p = pm.getObjectById(Street.class, id);
//            p.setName("Calgary");
//
//            pm.flush();
//
//            p.setName("Montreal");
//        });
//
//        final IsPojo<Entity> street = getEntity(UPDATE, Street.class, "1")
//                .withProperty("fields", hasItem(
//                        getField(Field.Type.PRIMITIVE, String.class, "name", "Montreal", "Regina")
//                ));
//
//
//        assertThat(audit.getModifications(), containsInAnyOrder(street));
//
//    }

}
