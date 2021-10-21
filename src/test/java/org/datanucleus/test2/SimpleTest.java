package org.datanucleus.test2;

import mydomain.audit.AuditListener;
import mydomain.model.Address;
import mydomain.model.School;
import mydomain.model.Street;
import mydomain.model.Student;
import org.datanucleus.api.jdo.JDOTransaction;
import org.datanucleus.identity.DatastoreIdImplKodo;
import org.datanucleus.util.NucleusLogger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Transaction;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.fail;

@Disabled
public class SimpleTest {
        @Test
    public void testSimple1() throws SQLException {
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

            Student p = new Student("First Student");
            p.setAddress(new Address(new Street[]{new Street("Regina")}));
            pm.makePersistent(p);
            p.setAddress(new Address(new Street[]{new Street("Victoria")}));

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


        // Update of field
        pm = pmf.getPersistenceManager();
        audit = new AuditListener();
        pm.addInstanceLifecycleListener(audit, null);
        tx = pm.currentTransaction();
        ((JDOTransaction) tx).registerEventListener(audit);
        try {
            tx.begin();

            Object id = new DatastoreIdImplKodo(Student.class.getName(), 1);

            Student p = pm.getObjectById(Student.class, id);
            p.getName();
            p.setName("Second Student");

            pm.flush();
            pm.deletePersistent(p);
            pm.flush();

            tx.commit();

            NucleusLogger.GENERAL.info(">> non-tx update");
            p.setName("Third Student");
            NucleusLogger.GENERAL.info(">> non-tx update done");
        } catch (Throwable thr) {
            NucleusLogger.GENERAL.error(">> Exception in test", thr);
            fail("Failed test : " + thr.getMessage());
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            pm.close();
        }

        pmf.close();
        NucleusLogger.GENERAL.info(">> test END");
    }

    @Test
    public void testSimple()
    {
        NucleusLogger.GENERAL.info(">> test START");
        PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory("MyTest");

        PersistenceManager pm = pmf.getPersistenceManager();
        Transaction tx = pm.currentTransaction();
        try
        {
            tx.begin();

            Address address = new Address(new Street[]{
                    new Street("Regina"),
                    new Street("Road")
            });

            School school = new School("School");
            school.setAddresses(Arrays.asList(address));

            Student charline = new Student("Charline");
            Set<Student> students = new HashSet<>();
            students.add(charline);
            school.setStudents(students);

            pm.makePersistent(charline);
            pm.makePersistent(school);

            tx.commit();
        }
        catch (Throwable thr)
        {
            NucleusLogger.GENERAL.error(">> Exception in test", thr);
            fail("Failed test : " + thr.getMessage());
        }
        finally
        {
            if (tx.isActive())
            {
                tx.rollback();
            }
            pm.close();
        }
        pmf.getDataStoreCache().evictAll();
        pmf.close();
        NucleusLogger.GENERAL.info(">> test END");
    }
}
