package org.datanucleus.test;

import com.spotify.hamcrest.pojo.IsPojo;
import mydomain.datanucleus.datatrail.NodeAction;
import org.datanucleus.test.model.CountryCode;
import org.datanucleus.test.model.QCountryCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class InTransactionTest extends AbstractTest{

    // Update the same value multiple times in a tx

    // Create + update + delete in same tx
    // Update + delete in same tx = DELETE only

    // Create a new object; add it to collection and delete object/remove from Collection in same tx

    // what happens with a flush ???



    @DisplayName("Create and Delete in same Tx should not appear in the DataTrail")
    @Test
    public void createAndDeleteSameTx(){
        executeTx(pm -> {
            CountryCode countryCode = new CountryCode("Canada", 1);
            pm.makePersistent(countryCode);
            pm.deletePersistent(countryCode);
        });

        assertThat("DataTrail should be empty", audit.getModifications(), hasSize(0));
    }



    @DisplayName("Delete and persist of same object in same tx.  DN only deletes the object")
    @Test
    public void createAndDeleteSameTx2(){
        executeTx(pm -> {
            CountryCode countryCode = new CountryCode("Canada", 1);
            pm.makePersistent(countryCode);
        }, false);

        executeTx(pm -> {
            CountryCode countryCode = pm.newJDOQLTypedQuery(CountryCode.class).filter(QCountryCode.candidate().country.eq("Canada")).executeUnique();
            pm.deletePersistent(countryCode);
            pm.makePersistent(countryCode);
        });

        IsPojo countryCode = getEntity(NodeAction.DELETE, CountryCode.class, "1");
        assertThat("CountryCode should be deleted", audit.getModifications(), contains(countryCode));

        executeTx(pm -> {
            CountryCode cc = pm.newJDOQLTypedQuery(CountryCode.class).filter(QCountryCode.candidate().country.eq("Canada")).executeUnique();
            assertThat(cc, is(nullValue()));
        }, false);

    }

}
