package org.datanucleus.test;

import mydomain.audit.AuditListener;
import mydomain.model.Address;
import mydomain.model.Street;
import mydomain.model.Student;
import org.datanucleus.api.jdo.JDOTransaction;
import org.datanucleus.identity.DatastoreIdImplKodo;
import org.datanucleus.util.NucleusLogger;
import org.junit.Test;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Transaction;

import static org.junit.Assert.fail;

public class CollectionTest
{
    @Test
    public void testCollection() {
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

            Address address = new Address(new Street[]{new Street("Regina")});
            pm.makePersistent(address);
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
    }



}
