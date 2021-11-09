package org.datanucleus.test;

import com.spotify.hamcrest.pojo.IsPojo;
import mydomain.datanucleus.datatrail.Node;
import mydomain.datanucleus.datatrail.NodeType;
import mydomain.model.CountryCode;
import mydomain.model.Telephone;
import org.datanucleus.identity.DatastoreIdImplKodo;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;


public class ReferenceTest extends AbstractTest {
    @Test
    public void createReference() throws IOException {
        executeTx(pm -> {
            CountryCode canada = new CountryCode("Canada", 1);
            Telephone telephone = new Telephone("514-123-1234", canada);
            pm.makePersistent(telephone);
        });

        Collection<Node> entities = audit.getModifications();

        final IsPojo<Node> countryCode = getEntity(Node.Action.CREATE, CountryCode.class, "1")
                .withProperty("fields", hasItems(
                        getField(NodeType.PRIMITIVE, String.class, "country", "Canada", null),
                        getField(NodeType.PRIMITIVE, Integer.class, "code", "1", null)
                ));

        assertThat(entities, hasItem(countryCode));

        final IsPojo<Node> telephone = getEntity(Node.Action.CREATE, Telephone.class, "1")
                .withProperty( "fields", hasItems(
                        getField(NodeType.PRIMITIVE, String.class, "number", "514-123-1234", null),
                        getField(NodeType.REF, CountryCode.class, "countryCode", "1", null)
                ));

        assertThat(entities, hasItem(telephone));

        // final check
        assertThat(entities, containsInAnyOrder(telephone, countryCode));
    }



    @Test
    public void deleteReference() throws IOException {
        executeTx(pm -> {
            CountryCode canada = new CountryCode("Canada", 1);
            Telephone telephone = new Telephone("514-123-1234", canada);

            CountryCode usa = new CountryCode( "USA", 1);
            pm.makePersistent(telephone);
            pm.makePersistent(usa);
        }, false);


        executeTx(pm -> {
            Object id = new DatastoreIdImplKodo(CountryCode.class.getName(), 2);
            CountryCode usa = pm.getObjectById(CountryCode.class, id);
            pm.deletePersistent(usa);
        });

        Collection<Node> entities = audit.getModifications();

        final IsPojo<Node> countryCode = getEntity(Node.Action.DELETE, CountryCode.class, "2")
                .withProperty("fields", hasItems(
                        getField(NodeType.PRIMITIVE, String.class, "country", "USA", null),
                        getField(NodeType.PRIMITIVE, Integer.class, "code", "1", null)
                ));

        assertThat(entities, hasItem(countryCode));

        assertThat(entities, containsInAnyOrder(countryCode));
    }


    @Test
    public void updateReference() throws IOException {
        executeTx(pm -> {
            CountryCode canada = new CountryCode("Canada", 1);
            Telephone telephone = new Telephone("514-123-1234", canada);

            CountryCode usa = new CountryCode( "USA", 1);
            pm.makePersistent(telephone);
            pm.makePersistent(usa);
        }, false);


        executeTx(pm -> {
            Object id = new DatastoreIdImplKodo(CountryCode.class.getName(), 2);
            CountryCode usa = pm.getObjectById(CountryCode.class, id);

            id = new DatastoreIdImplKodo(Telephone.class.getName(), 1);
            Telephone telephone = pm.getObjectById(Telephone.class, id);
            telephone.setCountryCode(usa);
        });

        Collection<Node> entities = audit.getModifications();

        final IsPojo<Node> telephone = getEntity(Node.Action.UPDATE, Telephone.class, "1")
                .withProperty( "fields", hasItems(
                        getField(NodeType.REF, CountryCode.class, "countryCode", "2", "1")
                ));

        assertThat(entities, containsInAnyOrder(telephone));
    }


}
