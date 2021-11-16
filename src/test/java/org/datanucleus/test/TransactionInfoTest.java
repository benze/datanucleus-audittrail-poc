package org.datanucleus.test;

import com.spotify.hamcrest.pojo.IsPojo;
import mydomain.datanucleus.datatrail.Node;
import mydomain.datanucleus.datatrail.NodeAction;
import mydomain.datanucleus.datatrail.NodeType;
import mydomain.datanucleus.datatrail.TransactionInfo;
import mydomain.datanucleus.datatrail.ITrailDesc;
import org.datanucleus.test.model.Street;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;

import static com.spotify.hamcrest.pojo.IsPojo.pojo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

public class TransactionInfoTest extends AbstractTest{

    @Test
    public void testTransactionInfo(){
        // create some transactionInfo
        Instant date = Instant.now().minus(10, ChronoUnit.DAYS);
        TransactionInfo txInfo = new TransactionInfo( date, "eric" );

        executeTx(pm -> {
            // set the transactionInfo
            pm.putUserObject(TransactionInfo.class.getName(), txInfo);

            // create 2 Primitive objects
            Street street = new Street("Regina");
            pm.makePersistent(street);

            street = new Street("Calgary");
            pm.makePersistent(street);
        });

        Collection<Node> entities = audit.getModifications();

        final IsPojo<Node>calgary = getEntity(NodeAction.CREATE, Street.class, ANY)
                .withProperty("fields", hasItem(
                        getField(NodeType.PRIMITIVE, String.class, "name", "Calgary", null)
                ))
                .withProperty("dateModified", is(txInfo.getDateModified()))
                .withProperty("username",  is(txInfo.getUsername()))
                .withProperty("transactionId", is(txInfo.getTxId()));

        final IsPojo<Node>regina = getEntity(NodeAction.CREATE, Street.class, ANY)
                .withProperty("fields", hasItem(
                        getField(NodeType.PRIMITIVE, String.class, "name", "Regina", null)
                ))
                .withProperty("dateModified", is(txInfo.getDateModified()))
                .withProperty("username",  is(txInfo.getUsername()))
                .withProperty("transactionId", is(txInfo.getTxId()));

        assertThat(entities, containsInAnyOrder(calgary, regina));
    }

    @Test
    public void testTransactionInfoMissing(){
        executeTx(pm -> {
            // create 2 Primitive objects
            Street street = new Street("Regina");
            pm.makePersistent(street);

            street = new Street("Calgary");
            pm.makePersistent(street);
            pm.getUserObject(TransactionInfo.class);
        });

        Collection<Node> entities = audit.getModifications();


        final IsPojo<Node>calgary = getEntity(NodeAction.CREATE, Street.class, ANY)
                .withProperty("fields", hasItem(
                        getField(NodeType.PRIMITIVE, String.class, "name", "Calgary", null)
                ))
                .withProperty("username", is(TransactionInfo.NO_USERNAME))
                .withProperty("dateModified", instanceOf(Instant.class))
                .withProperty("transactionId", anything());



        final IsPojo<Node>regina = getEntity(NodeAction.CREATE, Street.class, ANY)
                .withProperty("fields", hasItem(
                        getField(NodeType.PRIMITIVE, String.class, "name", "Regina", null)
                ))
                .withProperty("username", is(TransactionInfo.NO_USERNAME))
                .withProperty("dateModified", instanceOf(Instant.class))
                .withProperty("transactionId", anything());



        assertThat(entities, containsInAnyOrder(calgary, regina));
    }



    @Override
    protected IsPojo<Node>getEntity(NodeAction action, Class clazz, String id) {
            IsPojo<Node>entity = pojo(Node.class)
                    .withProperty("className", is(clazz.getName()))
                    .withProperty("value", getValueMatcher(id))
                    .withProperty("action", hasToString(action.toString()))
                    .withProperty("version", any(String.class))
                    ;

            if( ITrailDesc.class.isAssignableFrom(clazz)) {
                entity = entity.withProperty("description", anything());
            }

            return entity;
        }
}
