package org.datanucleus.test;

import com.spotify.hamcrest.pojo.IsPojo;
import mydomain.datanucleus.datatrail.Node;
import mydomain.datanucleus.datatrail.NodeAction;
import mydomain.datanucleus.datatrail.NodeType;
import org.datanucleus.test.model.Street;
import org.datanucleus.identity.DatastoreIdImplKodo;
import org.junit.jupiter.api.DisplayName;
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


        final IsPojo<Node> calgary = getEntity(NodeAction.CREATE, Street.class, "1")
                .withProperty("fields", hasItem(
                        getField(NodeType.PRIMITIVE, String.class, "name", "Calgary", null)
                ));


        Collection<Node>  entities = audit.getModifications();
        assertThat(entities, hasItem(calgary));
        assertThat(entities, containsInAnyOrder(calgary));
    }


    @Test
    public void deletePrimitive() throws IOException {
        executeTx((pm) -> {
            Street street = new Street("Regina");
            pm.makePersistent(street);
        }, false);

        executeTx(pm -> {
            Object id = new DatastoreIdImplKodo(Street.class.getName(), 1);
            Street p = pm.getObjectById(Street.class, id);
            p.setName("aaaa");

            pm.deletePersistent(p);
        });


        Collection<Node>  entities = audit.getModifications();

        final IsPojo<Node> regina = getEntity(NodeAction.DELETE, Street.class, "1")
                .withProperty("fields", hasItem(
                        getField(NodeType.PRIMITIVE, String.class, "name", "Regina", null)
                ));

        assertThat(entities, containsInAnyOrder(regina));
    }




    @DisplayName("Update multiple times in a transaction shows last value only")
    @Test
    public void updatePrimitive() throws IOException {
        executeTx((pm) -> {
            Street street = new Street("Regina");
            pm.makePersistent(street);
        }, false);

        executeTx(pm -> {
            Object id = new DatastoreIdImplKodo(Street.class.getName(), 1);
            Street p = pm.getObjectById(Street.class, id);
            p.setName("Calgary");

            pm.flush();

            p.setName("Montreal");
        });

        final IsPojo<Node> street = getEntity(NodeAction.UPDATE, Street.class, "1")
                .withProperty("fields", contains(
                        getField(NodeType.PRIMITIVE, String.class, "name", "Montreal",
                            getField(NodeType.PRIMITIVE, String.class, "name", "Regina", null ))
                ));


        assertThat(audit.getModifications(), containsInAnyOrder(street));

    }

}
