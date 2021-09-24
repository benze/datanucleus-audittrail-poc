package org.datanucleus.test;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.spotify.hamcrest.pojo.IsPojo;
import mydomain.audit.AuditListener;
import mydomain.datatrail.Entity;
import mydomain.model.Address;
import mydomain.model.Street;
import org.datanucleus.api.jdo.JDOTransaction;
import org.datanucleus.util.NucleusLogger;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Transaction;
import javax.swing.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.Stack;

import static com.spotify.hamcrest.pojo.IsPojo.pojo;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.fail;

public class PrimitiveTest extends AbstractTest
{
    @Test
    public void createPrimitive() throws IOException {
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

            Street street = new Street("Regina");
            pm.makePersistent(street);
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
        assertThat(entities, hasSize(1));
        Entity entity = entities.get(0);

        assertThat( entity, allOf(
                hasProperty("action", hasToString("CREATE") ),
                hasProperty("id", is("1")),
                hasProperty( "dateModified", notNullValue()),
                hasProperty("fields", hasSize(1))
        ));



        assertThat(entity.getFields().get(0), allOf(
                hasProperty("name", is("name")),
                hasProperty("type", hasToString("PRIMITIVE")),
                hasProperty( "value", is("Regina")),
                hasProperty("className", is( String.class.getName()))
        ));


        // check that the datatrail log is correct
        NucleusLogger.GENERAL.info(getJson(audit.getModifications()));
    }
}
