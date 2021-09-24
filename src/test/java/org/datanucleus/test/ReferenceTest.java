package org.datanucleus.test;

import com.spotify.hamcrest.pojo.IsPojo;
import mydomain.audit.AuditListener;
import mydomain.datatrail.Entity;
import mydomain.datatrail.field.Field;
import mydomain.model.CountryCode;
import mydomain.model.Telephone;
import org.datanucleus.api.jdo.JDOTransaction;
import org.datanucleus.util.NucleusLogger;
import org.junit.jupiter.api.Test;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Transaction;
import java.io.IOException;
import java.time.Instant;
import java.util.List;

import static com.spotify.hamcrest.pojo.IsPojo.pojo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.fail;


public class ReferenceTest extends AbstractTest {
    @Test
    public void createReference() throws IOException {
        NucleusLogger.GENERAL.info(">> test START");
        PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory("MyTest");

        // Create of object
        PersistenceManager pm = pmf.getPersistenceManager();
        AuditListener audit = new AuditListener();
        pm.addInstanceLifecycleListener(audit, null);
        Transaction tx = pm.currentTransaction();
        ((JDOTransaction) tx).registerEventListener(audit);
        try {
            tx.begin();

            CountryCode canada = new CountryCode("Canada", 1);
            Telephone telephone = new Telephone("514-123-1234", canada);
            pm.makePersistent(telephone);
            tx.commit();
        } catch (Throwable thr) {
            NucleusLogger.GENERAL.error(">> Exception in test", thr);
            fail("Failed test : " + thr.getMessage());
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            pm.close();
        }



        List<Entity> entities = audit.getModifications();

        final IsPojo<Entity> countryCode =
                pojo(Entity.class)
                        .withProperty("className", is(CountryCode.class.getName()))
                        .withProperty("id", is("1"))
                        .withProperty("action", hasToString("CREATE"))
                        .withProperty("version", any(String.class))
                        .withProperty("dateModified", any(Instant.class))
                        .withProperty("username", anything())
                        .withProperty("fields", hasItems(
                                        pojo(Field.class)
                                                .withProperty("name", is("code"))
                                                .withProperty("value", is("1"))
                                                .withProperty("type", hasToString("PRIMITIVE"))
                                                .withProperty("className", is( Integer.class.getName()))
                                                .withProperty("prev", nullValue())

                                        ,pojo(Field.class)
                                                .withProperty("name", is("country"))
                                                .withProperty("value", is("Canada"))
                                                .withProperty("type", hasToString("PRIMITIVE"))
                                                .withProperty("className", is( String.class.getName()))
                                                .withProperty("prev", nullValue())

                                )
                        );



        final IsPojo<Entity> telephone =
                pojo(Entity.class)
                        .withProperty("className", is(Telephone.class.getName()))
                        .withProperty("id", is("1"))
                        .withProperty("action", hasToString("CREATE"))
                        .withProperty("version", any(String.class))
                        .withProperty("dateModified", any(Instant.class))
                        .withProperty("username", anything())
                        .withProperty("fields", hasItems(
                                pojo(Field.class)
                                        .withProperty("name", is("number"))
                                        .withProperty("value", is("514-123-1234"))
                                        .withProperty("type", hasToString("PRIMITIVE"))
                                        .withProperty("className", is( String.class.getName()))
                                        .withProperty("prev", nullValue()),
                                pojo(Field.class)
                                        .withProperty("name", is("countryCode"))
                                        .withProperty("value", is("1"))
                                        .withProperty("type", hasToString("REF"))
                                        .withProperty("className", is( CountryCode.class.getName()))
                                        .withProperty("prev", nullValue())

                        ));


//                        .withProperty("action", hasToString("CREATE"));


//        assertThat(entities, hasItem(countryCode));


        assertThat(entities, containsInAnyOrder(telephone, countryCode));

        // get one enti

        // check that the datatrail log is correct
        NucleusLogger.GENERAL.info(getJson(audit.getModifications()));

    }


}
