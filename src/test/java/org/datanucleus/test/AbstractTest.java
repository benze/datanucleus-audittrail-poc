package org.datanucleus.test;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.spotify.hamcrest.pojo.IsPojo;
import mydomain.audit.AuditListener;
import mydomain.datatrail.Entity;
import mydomain.datatrail.field.Field;
import org.datanucleus.api.jdo.JDOTransaction;
import org.datanucleus.util.NucleusLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Transaction;
import java.io.IOException;
import java.io.StringWriter;
import java.time.Instant;
import java.util.List;

import static com.spotify.hamcrest.pojo.IsPojo.pojo;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

abstract public class AbstractTest {

    protected AuditListener audit;
    protected PersistenceManager pm;
    protected Transaction tx;


    @BeforeEach
    protected void setupTransaction(){
        NucleusLogger.GENERAL.info(">> test START");
        PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory("MyTest");

        // Create of object
        pm = pmf.getPersistenceManager();
        audit = new AuditListener();
        pm.addInstanceLifecycleListener(audit, null);
        tx = pm.currentTransaction();
        ((JDOTransaction) tx).registerEventListener(audit);
    }

    @AfterEach
    protected void endTransaction() throws IOException {
        if( tx.isActive())
            tx.rollback();
        pm.close();

        // check that the datatrail log is correct
        NucleusLogger.GENERAL.debug(getJson(audit.getModifications()));

    }

    protected String getJson(List<Entity> entities) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JavaTimeModule module = new JavaTimeModule();
        mapper.registerModule(module);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        StringWriter sw = new StringWriter();
        for( Entity entity : entities)
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

        return entity;

    }

    protected IsPojo<Field> getContainerField(Field.Type type, String name) {
        IsPojo<Field> field = pojo(Field.class)
                .withProperty("name", is(name))
                .withProperty("type", hasToString(type.toString()))
                .withProperty("prev", nullValue())
        ;

        return field;

    }

    protected IsPojo<Field> getListElement(Field.Type type, Class clazz, String value) {
        IsPojo<Field> field = pojo(Field.class)
                .withProperty("value", is(value))
                .withProperty("type", hasToString(type.toString()))
                .withProperty("prev", nullValue())
                .withProperty("className", is(clazz.getName()));
        ;

        return field;

    }

    protected IsPojo<Field> getField(Field.Type type, Class clazz, String name, String value) {
        IsPojo<Field> field = pojo(Field.class)
                .withProperty("name", is(name))
                .withProperty("value", is(value))
                .withProperty("type", hasToString(type.toString()))
                .withProperty("prev", nullValue())
                .withProperty("className", is(clazz.getName()));
                ;

        return field;

    }
}
