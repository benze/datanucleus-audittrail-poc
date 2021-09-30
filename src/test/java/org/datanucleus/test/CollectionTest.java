package org.datanucleus.test;

import com.spotify.hamcrest.pojo.IsPojo;
import mydomain.datatrail.Entity;
import mydomain.datatrail.field.Field;
import mydomain.model.Address;
import mydomain.model.School;
import mydomain.model.Street;
import mydomain.model.Student;
import org.datanucleus.identity.DatastoreIdImplKodo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static mydomain.datatrail.Entity.Action.CREATE;
import static mydomain.datatrail.Entity.Action.DELETE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;

@Execution(ExecutionMode.SAME_THREAD)
public class CollectionTest extends AbstractTest {


    @Test
    public void createCollectionArray() throws IOException {
        executeTx(pm -> {
            Address address = new Address(new Street[]{new Street("Regina")});
            pm.makePersistent(address);
        });

        final IsPojo<Entity> street = getEntity(CREATE, Street.class, "1")
                .withProperty("fields", hasItem(
                        getField(Field.Type.PRIMITIVE, String.class, "name", "Regina")
                ));


        final IsPojo<Entity> address = getEntity(CREATE, Address.class, "1")
                .withProperty("fields", hasItem(
                        getContainerField(Field.Type.COLLECTION, "street")
                                .withProperty("elements", hasItem(
                                        getListElement(Field.Type.REF, Street.class, "1")
                                ))
                ));


        Collection<Entity> entities = audit.getModifications();
        assertThat(entities, containsInAnyOrder(street, address));
    }


    @Test
    public void createCollectionList() {
        executeTx(pm -> {
            Address address = new Address(new Street[]{
                    new Street("Regina"),
                    new Street("Road")
            });

            School school = new School("WADA");
            school.setAddresses(Arrays.asList(address));

            Student charline = new Student("Charline");
            Set<Student> students = new HashSet<>();
            students.add(charline);
            school.setStudents(students);

            pm.makePersistent(charline);
            pm.makePersistent(school);
        });


        final IsPojo<Entity> regina = getEntity(CREATE, Street.class, "1")
                .withProperty("fields", hasItem(
                        getField(Field.Type.PRIMITIVE, String.class, "name", "Regina")
                ));

        final IsPojo<Entity> road = getEntity(CREATE, Street.class, "2")
                .withProperty("fields", hasItem(
                        getField(Field.Type.PRIMITIVE, String.class, "name", "Road")
                ));


        final IsPojo<Entity> address = getEntity(CREATE, Address.class, "1")
                .withProperty("fields", hasItem(
                        getContainerField(Field.Type.COLLECTION, "street")
                                .withProperty("elements", hasItems(
                                        getListElement(Field.Type.REF, Street.class, "1"),
                                        getListElement(Field.Type.REF, Street.class, "2")
                                ))
                ));


        final IsPojo<Entity> student = getEntity(CREATE, Student.class, "1")
                .withProperty("fields", hasItem(
                        getField(Field.Type.PRIMITIVE, String.class, "name", "Charline")
                ));

        final IsPojo<Entity> school = getEntity(CREATE, School.class, "1")
                .withProperty("fields", hasItems(
                        getField(Field.Type.PRIMITIVE, String.class, "name", "WADA"),
                        getContainerField(Field.Type.COLLECTION, "addresses")
                                .withProperty("elements", hasItems(
                                        getListElement(Field.Type.REF, Address.class, "1")
                                )),
                        getContainerField(Field.Type.COLLECTION, "students")
                                .withProperty("elements", hasItems(
                                        getListElement(Field.Type.REF, Student.class, "1")
                                ))
                ));


        assertThat(audit.getModifications(), hasItem(
                school
        ));

        assertThat(audit.getModifications(), containsInAnyOrder(
                regina,
                road,
                address,
                school,
                student
        ));


    }


    @Test
    public void deleteCollectionList() {
        executeTx(pm -> {
            Address address = new Address(new Street[]{
                    new Street("Regina"),
                    new Street("Road")
            });

            School school = new School("WADA");
            school.setAddresses(Arrays.asList(address));

            Student charline = new Student("Charline");
            Set<Student> students = new HashSet<>();
            students.add(charline);
            school.setStudents(students);

            pm.makePersistent(charline);
            pm.makePersistent(school);
        }, false);


        executeTx(pm -> {
            Object id = new DatastoreIdImplKodo(School.class.getName(), 1);
            School p = pm.getObjectById(School.class, id);
            pm.deletePersistent(p);
        });


        final IsPojo<Entity> address = getEntity(DELETE, Address.class, "1")
                .withProperty("fields", hasItem(
                        getContainerField(Field.Type.COLLECTION, "street")
                                .withProperty("elements", hasItems(
                                        getListElement(Field.Type.REF, Street.class, "1"),
                                        getListElement(Field.Type.REF, Street.class, "2")
                                ))
                ));

        final IsPojo<Entity> school = getEntity(DELETE, School.class, "1")
                .withProperty("fields", hasItems(
                        getField(Field.Type.PRIMITIVE, String.class, "name", "WADA"),
                        getContainerField(Field.Type.COLLECTION, "addresses")
                                .withProperty("elements", hasItems(
                                        getListElement(Field.Type.REF, Address.class, "1")
                                )),
                        getContainerField(Field.Type.COLLECTION, "students")
                                .withProperty("elements", hasItems(
                                        getListElement(Field.Type.REF, Student.class, "1")
                                ))
                ));

//        assertThat(audit.getModifications(), hasItem(
//                regina
//        ));

        // only the address is defined as a dependent field of school, so address object will be deleted.
        // individual streets are not dependent fields of address, so they will NOT be cascade deleted
        assertThat(audit.getModifications(), containsInAnyOrder(
                address,
                school
        ));


    }

}
