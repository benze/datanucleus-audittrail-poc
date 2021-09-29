package org.datanucleus.test;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.spotify.hamcrest.pojo.IsPojo;
import h2.H2Server;
import mydomain.audit.AuditListener;
import mydomain.datatrail.Entity;
import mydomain.datatrail.field.Field;
import mydomain.model.ITrailDesc;
import org.datanucleus.api.jdo.JDOTransaction;
import org.datanucleus.util.NucleusLogger;
import org.h2.server.TcpServer;
import org.h2.tools.Server;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Transaction;
import java.io.IOException;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

import static com.spotify.hamcrest.pojo.IsPojo.pojo;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.fail;

abstract public class AbstractTest {

    static NucleusLogger CONSOLE = NucleusLogger.getLoggerInstance("Console");

    @FunctionalInterface
    public interface TransactionContent {
        public void execute(PersistenceManager pm);
    }

    PersistenceManagerFactory pmf;
    protected AuditListener audit = new AuditListener();

    @BeforeAll
    public static void enableH2Webserver(){
        CONSOLE.info(H2Server.getInstance());
    }

    /**
     * Clear the embedded DB before each test execution to ensure that IDs are reset to 1
     */
    @BeforeEach
    protected void resetDatabase() {
        pmf = JDOHelper.getPersistenceManagerFactory("MyTest");
    }

    @AfterEach
    protected void endTransaction() throws IOException {
        pmf.close();
        // check that the datatrail log is correct
        CONSOLE.debug(getJson(audit.getModifications()));

    }

    protected void executeTx(TransactionContent transactionContent) {
        executeTx(transactionContent, true);
    }

    protected void executeTx(TransactionContent transactionContent, boolean attachListener) {
        NucleusLogger.GENERAL.info(">> test START");

        // Create of object
        PersistenceManager pm = pmf.getPersistenceManager();
        Transaction tx = pm.currentTransaction();
        if( attachListener) {
            pm.addInstanceLifecycleListener(audit, null);
            ((JDOTransaction) tx).registerEventListener(audit);
        }
        try {
            tx.begin();
            transactionContent.execute(pm);
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
        pmf.getDataStoreCache().evictAll();
    }

    protected String getJson(Collection<Entity> entities) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JavaTimeModule module = new JavaTimeModule();
        mapper.registerModule(module);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false);

        StringWriter sw = new StringWriter();
        for (Entity entity : entities)
            mapper.writeValue(sw, entity);
        return sw.toString();
    }

    protected IsPojo<Entity> getEntity(Entity.Action action, Class clazz, String id) {
        IsPojo<Entity> entity = pojo(Entity.class)
                .withProperty("className", is(clazz.getName()))
                .withProperty("id", is(id))
                .withProperty("action", hasToString(action.toString()))
                .withProperty("version", any(String.class))
                .withProperty("dateModified", any(Instant.class))
                .withProperty("username", anything());

        if( ITrailDesc.class.isAssignableFrom(clazz)) {
            entity = entity.withProperty("description", anything());
        }

        return entity;

    }

    protected IsPojo<Field> getContainerField(Field.Type type, String name) {
        IsPojo<Field> field = pojo(Field.class)
                .withProperty("name", is(name))
                .withProperty("type", hasToString(type.toString()))
                .withProperty("prev", nullValue());

        return field;

    }

    protected IsPojo<Field> getListElement(Field.Type type, Class clazz, String value) {
        IsPojo<Field> field = pojo(Field.class)
                .withProperty("value", is(value))
                .withProperty("type", hasToString(type.toString()))
                .withProperty("prev", nullValue())
                .withProperty("className", is(clazz.getName()));
        ;

        if( ITrailDesc.class.isAssignableFrom(clazz)) {
            field = field.withProperty("description", anything());
        }

        return field;

    }

    protected IsPojo getMapElement(Field.Type keyType, Class keyClazz, String keyValue, Field.Type valueType, Class valueClazz, String valueValue) {

        IsPojo<Field> key = pojo(Field.class)
                .withProperty("value", is(keyValue))
                .withProperty("type", hasToString(keyType.toString()))
                .withProperty("prev", nullValue())
                .withProperty("className", is(keyClazz.getName()));
        if( ITrailDesc.class.isAssignableFrom(keyClazz)) {
            key = key.withProperty("description", anything());
        }


        IsPojo<Field> value = pojo(Field.class)
                .withProperty("value", is(valueValue))
                .withProperty("type", hasToString(valueType.toString()))
                .withProperty("prev", nullValue())
                .withProperty("className", is(valueClazz.getName()));
        ;
        if( ITrailDesc.class.isAssignableFrom(valueClazz)) {
            value = value.withProperty("description", anything());
        }

        IsPojo mapEntry = pojo(Object.class)
                .withProperty("key", is(key))
                .withProperty("value", is(value));

        return mapEntry;

    }



    protected IsPojo<Field> getField(Field.Type type, Class clazz, String name, String value) {
        IsPojo<Field> field = pojo(Field.class)
                .withProperty("name", is(name))
                .withProperty("value", is(value))
                .withProperty("type", hasToString(type.toString()))
                .withProperty("prev", nullValue())
                .withProperty("className", is(clazz.getName()));
        ;

        if( ITrailDesc.class.isAssignableFrom(clazz)) {
            field = field.withProperty("description", anything());
        }

        return field;

    }
}
