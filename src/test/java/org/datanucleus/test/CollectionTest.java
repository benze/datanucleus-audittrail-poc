package org.datanucleus.test;

import com.spotify.hamcrest.pojo.IsPojo;
import mydomain.audit.AuditListener;
import mydomain.datatrail.Entity;
import mydomain.datatrail.field.Field;
import mydomain.model.Address;
import mydomain.model.Street;
import org.datanucleus.api.jdo.JDOTransaction;
import org.datanucleus.util.NucleusLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Transaction;
import java.io.IOException;
import java.util.List;

import static mydomain.datatrail.Entity.Action.CREATE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.fail;

@Execution(ExecutionMode.SAME_THREAD)
public class CollectionTest extends AbstractTest {


    @Test
    public void createCollectionArray() throws IOException {
        try {
            tx.begin();

            Address address = new Address(new Street[]{new Street("Regina")});
            pm.makePersistent(address);
            tx.commit();
        } catch (Throwable thr) {
            NucleusLogger.GENERAL.error(">> Exception in test", thr);
            fail("Failed test : " + thr.getMessage());
        }



        final IsPojo<Entity> street = getEntity(CREATE, Street.class, "1")
                .withProperty("fields", hasItem(
                        getField(Field.Type.PRIMITIVE, String.class, "name", "Regina")
                ));


        final IsPojo<Entity> address = getEntity(CREATE, Address.class, "1")
                .withProperty("fields", hasItem(
                        getContainerField(Field.Type.COLLECTION, "street")
                                .withProperty("elements", hasItem(
                                        getListElement(Field.Type.REF, Street.class, "1")
                                ))
                ));


        List<Entity> entities = audit.getModifications();
        assertThat(entities, containsInAnyOrder(street, address));
    }


}
