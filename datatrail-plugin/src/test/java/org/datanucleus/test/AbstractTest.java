package org.datanucleus.test;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.spotify.hamcrest.pojo.IsPojo;
import h2.H2Server;
import org.datanucleus.datatrail.TransactionListener;
import org.datanucleus.datatrail.Node;
import org.datanucleus.datatrail.impl.NodeAction;
import org.datanucleus.datatrail.impl.NodeType;
import org.datanucleus.datatrail.impl.nodes.NodeDefinition;
import org.datanucleus.datatrail.impl.nodes.map.MapEntry;
import org.datanucleus.datatrail.ITrailDesc;
import org.datanucleus.enhancement.Persistable;
import org.datanucleus.identity.DatastoreId;
import org.datanucleus.identity.IdentityUtils;
import org.datanucleus.util.NucleusLogger;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Transaction;
import java.io.IOException;
import java.io.StringWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Stack;

import static com.spotify.hamcrest.pojo.IsPojo.pojo;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.fail;

abstract public class AbstractTest {

    static NucleusLogger CONSOLE = NucleusLogger.getLoggerInstance("Console");

    protected String ANY = "ANY";

    @FunctionalInterface
    public interface TransactionContent {
        public void execute(PersistenceManager pm);
    }

    PersistenceManagerFactory pmf;


    // internal class to avoid refactoring of all existing tests
    public class DataTrailDetails {
        Collection<Node> entities = new ArrayList<>();

        public void setModifications(Collection<Node> entities){
            this.entities = entities;
        }

        public Collection<Node> getModifications(){
            return entities;
        }

        public void clear(){
            entities.clear();
        }
    }

    
    protected DataTrailDetails audit = new DataTrailDetails();
    protected Stack<String> logEntries = new Stack<>();

    @BeforeAll
    public static void enableH2Webserver(){
        CONSOLE.info(H2Server.getInstance());
    }

    /**
     * Clear the embedded DB before each test execution to ensure that IDs are reset to 1
     */
    @BeforeEach
    protected void resetTransaction(TestInfo testInfo) {
        pmf = JDOHelper.getPersistenceManagerFactory("MyTest");
        logEntries.clear();
        audit.clear();
        StringBuffer sb = new StringBuffer();
        testInfo.getTestClass().ifPresent(aClass -> sb.append(aClass.getName()));
        testInfo.getTestMethod().ifPresent(method -> sb.append("." ).append(method.getName()).append("()"));
        CONSOLE.info( "Executing: " + sb.toString());
    }

    @AfterEach
    protected void endTransaction() {
        pmf.close();
        // check that the datatrail log is correct
        logEntries.forEach(CONSOLE::debug);

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
            TransactionListener txListener = new TransactionListener(entities ->{
                logEntries.push(getJson(entities));
                audit.setModifications(entities);
            });
            txListener.attachListener(pm, null);
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

    protected String getJson(Collection<Node> entities) {
        ObjectMapper mapper = new ObjectMapper();
        JavaTimeModule module = new JavaTimeModule();
        mapper.registerModule(module);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false);

        StringWriter sw = new StringWriter();
        for (Node entity : entities) {
            try {
                mapper.writeValue(sw, entity);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return sw.toString();
    }

    protected IsPojo<Node>getEntity(NodeAction action, Class<?> clazz, String id) {
        IsPojo<Node> entity = pojo(Node.class)
                .withProperty("className", is(clazz.getName()))
                .withProperty("value", getValueMatcher(id))
                .withProperty("action", hasToString(action.toString()))
                .withProperty("version", any(String.class))
                .withProperty("dateModified", any(Instant.class))
                .withProperty("username",  anything())
                .withProperty("transactionId", anything());


        if( ITrailDesc.class.isAssignableFrom(clazz)) {
            entity = entity.withProperty("description", anything());
        }

        return entity;
    }

    protected IsPojo<Node>getContainerField(NodeType type, String name) {
        IsPojo<Node> field = pojo(Node.class)
                .withProperty("name", is(name))
                .withProperty("type", hasToString(type.toString()))
                .withProperty("prev", nullValue());

        return field;

    }

    protected IsPojo<Node>getListElement(NodeType type, Class<?> clazz, String value) {
        IsPojo<Node> field = pojo(Node.class)
                .withProperty("value", is(value))
                .withProperty("type", hasToString(type.toString()))
                .withProperty("prev", nullValue())
                .withProperty("className", is(clazz.getName()));

        if( ITrailDesc.class.isAssignableFrom(clazz)) {
            field = field.withProperty("description", anything());
        }

        return field;

    }


    protected IsPojo<MapEntry>getMapElement(NodeType keyType, Class<?> keyClazz, String keyValue, NodeType valueType, Class<?> valueClazz, String valueValue, IsPojo<Node> prevValue) {

        IsPojo<Node> key = pojo(Node.class)
                .withProperty("value", is(keyValue))
                .withProperty("type", hasToString(keyType.toString()))
                .withProperty("prev", nullValue())
                .withProperty("className", is(keyClazz.getName()))
                ;
        if( ITrailDesc.class.isAssignableFrom(keyClazz)) {
            key = key.withProperty("description", anything());
        }


        IsPojo<Node> value = pojo(Node.class)
                .withProperty("value", getValueMatcher(valueValue))
                .withProperty("type", hasToString(valueType.toString()))
                .withProperty("prev", prevValue == null ? nullValue() : is(prevValue))
                .withProperty("className", is(valueClazz.getName()));

        if( ITrailDesc.class.isAssignableFrom(valueClazz)) {
            value = value.withProperty("description", anything());
        }

        IsPojo<MapEntry> mapEntry = pojo(MapEntry.class)
                .withProperty("key", is(key))
                .withProperty("value", is(value));

        return mapEntry;

    }



    protected IsPojo<Node> getField(NodeType type, Class<?> clazz, String name, String value, IsPojo<Node> prevValue) {
        IsPojo<Node> field = pojo(Node.class)
                .withProperty("name", is(name))
                .withProperty("value", getValueMatcher(value))
                .withProperty("type", hasToString(type.toString()))
                .withProperty("prev", prevValue == null ? nullValue() : is(prevValue))
                .withProperty("className", is(clazz.getName()));
        ;

        if( ITrailDesc.class.isAssignableFrom(clazz)) {
            field = field.withProperty("description", anything());
        }

        return field;
    }


    /**
     * Converts the input value to the appropriate matcher.  Supports "special" values defined as constants
     * @param value
     * @param
     * @return
     */
    protected Matcher<String> getValueMatcher(String value) {
        if( value == null )
            return nullValue(String.class);
        else if (ANY.equals(value)){
            return is(any(String.class));
        } else {
            return is(value);
        }
    }


    /**
     * Gets the String representation of the object ID (long)
     * @param pc
     * @return
     */
    protected String getId(Persistable pc) {
        if (pc == null)
            return null;

        Object objectId = pc.dnGetObjectId();

        if (objectId == null) {
            return null;
        } else if (IdentityUtils.isDatastoreIdentity(objectId)) {
            return ((DatastoreId) objectId).getKeyAsObject().toString();
        } else {
            return objectId.toString();
        }
    }


    protected Optional<Node> filterEntity(Collection<Node> collection, Class<?> entityClass, NodeAction action){
        return collection.stream()
                        .filter(node -> node.getType() == NodeType.ENTITY
                                && node.getClassName().equals(entityClass.getName())
                                && Arrays.stream(node.getClass().getAnnotation(NodeDefinition.class).action()).anyMatch(action1 -> action1 == action)
                        ).findFirst();
    }
}
