package org.datanucleus.test;

import com.spotify.hamcrest.pojo.IsPojo;
import mydomain.datanucleus.datatrail.Node;
import mydomain.datanucleus.datatrail.NodeAction;
import mydomain.datanucleus.datatrail.NodeType;
import org.datanucleus.test.model.CountryCode;
import org.datanucleus.test.model.QTelephone;
import org.datanucleus.test.model.Telephone;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class DateTest extends AbstractTest{

    @Test
    public void updateDateTest(){
        final Instant testStart = Instant.now();
        executeTx((pm) -> {
            Telephone telephone = new Telephone("514-555-1234", new CountryCode("Canada", 1));
            pm.makePersistent(telephone);

            telephone.setCreated(Date.from(testStart));
        });

        IsPojo<Node> telephone = getEntity(NodeAction.CREATE, Telephone.class, ANY)
                .withProperty("fields", Matchers.hasItem(
                        getField(NodeType.PRIMITIVE, Date.class, "created", ANY, null)
                ));

        assertThat( audit.getModifications(), Matchers.hasItem(telephone));

        final Instant testModified = testStart.plus(10, ChronoUnit.DAYS);
        executeTx((pm) -> {
            Telephone tel = pm.newJDOQLTypedQuery(Telephone.class).filter(QTelephone.candidate().number.endsWith("1234")).executeUnique();
            tel.setCreated(Date.from(testModified));
        });

        telephone = getEntity(NodeAction.UPDATE, Telephone.class, ANY)
                    .withProperty("fields", Matchers.hasItem(
                        getField(NodeType.PRIMITIVE, Date.class, "created", Date.from(testModified).toString(),
                            getField(NodeType.PRIMITIVE, Date.class, "created", Date.from(testStart).toString(), null))
                ));

        assertThat( filterEntity(audit.getModifications(), Telephone.class, NodeAction.UPDATE).get(), is(telephone));
        assertThat( audit.getModifications(), Matchers.hasItem(telephone));


    }
}
