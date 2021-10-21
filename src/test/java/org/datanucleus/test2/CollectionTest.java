package org.datanucleus.test2;

import com.spotify.hamcrest.pojo.IsPojo;
import mydomain.datanucleus.datatrail2.Node;
import mydomain.datanucleus.datatrail2.NodeType;
import mydomain.datatrail.Entity;
import mydomain.datatrail.field.Field;
import mydomain.model.Address;
import mydomain.model.CountryCode;
import mydomain.model.QCountryCode;
import mydomain.model.QTelephone;
import mydomain.model.QTelephoneBook;
import mydomain.model.School;
import mydomain.model.Street;
import mydomain.model.Student;
import mydomain.model.Telephone;
import mydomain.model.TelephoneBook;
import org.datanucleus.identity.DatastoreIdImplKodo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static mydomain.datatrail.Entity.Action.CREATE;
import static mydomain.datatrail.Entity.Action.DELETE;
import static mydomain.datatrail.Entity.Action.UPDATE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;

@Execution(ExecutionMode.SAME_THREAD)
public class CollectionTest extends AbstractTest {

//
//    @Test
//    public void createCollectionArray() throws IOException {
//        executeTx(pm -> {
//            Address address = new Address(new Street[]{new Street("Regina")});
//            pm.makePersistent(address);
//        });
//
//        final IsPojo<Entity> street = getEntity(CREATE, Street.class, "1")
//                .withProperty("fields", hasItem(
//                        getField(Field.Type.PRIMITIVE, String.class, "name", "Regina", null)
//                ));
//
//
//        final IsPojo<Entity> address = getEntity(CREATE, Address.class, "1")
//                .withProperty("fields", hasItem(
//                        getContainerField(Field.Type.COLLECTION, "street")
//                                .withProperty("added", hasItem(
//                                        getListElement(Field.Type.REF, Street.class, "1")
//                                ))
//                ));
//
//
//        Collection<Entity> entities = audit.getModifications();
//        assertThat(entities, containsInAnyOrder(street, address));
//    }
//
//
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


        final IsPojo<Node> regina = getEntity(Node.Action.CREATE, Street.class, "1")
                .withProperty("fields", hasItem(
                        getField(NodeType.PRIMITIVE, String.class, "name", "Regina", null)
                ));


        final IsPojo<Node> road = getEntity(Node.Action.CREATE, Street.class, "2")
                .withProperty("fields", hasItem(
                        getField(NodeType.PRIMITIVE, String.class, "name", "Road", null)
                ));


        final IsPojo<Node> address = getEntity(Node.Action.CREATE, Address.class, "1")
                .withProperty("fields", hasItem(
                        getContainerField(NodeType.ARRAY, "street")
                                .withProperty("added", hasItems(
                                        getListElement(NodeType.REF, Street.class, "1"),
                                        getListElement(NodeType.REF, Street.class, "2")
                                ))
                ));


        final IsPojo<Node> student = getEntity(Node.Action.CREATE, Student.class, "1")
                .withProperty("fields", hasItem(
                        getField(NodeType.PRIMITIVE, String.class, "name", "Charline", null)
                ));

        final IsPojo<Node> school = getEntity(Node.Action.CREATE, School.class, "1")
                .withProperty("fields", hasItems(
                        getField(NodeType.PRIMITIVE, String.class, "name", "WADA", null),
                        getContainerField(NodeType.COLLECTION, "addresses")
                                .withProperty("added", hasItems(
                                        getListElement(NodeType.REF, Address.class, "1")
                                )),
                        getContainerField(NodeType.COLLECTION, "students")
                                .withProperty("added", hasItems(
                                        getListElement(NodeType.REF, Student.class, "1")
                                ))
                ));

        assertThat(audit.getModifications(), hasItem( regina ));
        assertThat(audit.getModifications(), hasItem( road ));
        assertThat(audit.getModifications(), hasItem( student ));
        assertThat(audit.getModifications(), hasItem( school ));
        assertThat(audit.getModifications(), hasItem( address ));

        assertThat(audit.getModifications(), containsInAnyOrder(
                regina,
                road,
                address,
                school,
                student
        ));


    }
//
//
//    @Test
//    public void deleteCollectionList() {
//        executeTx(pm -> {
//            Address address = new Address(new Street[]{
//                    new Street("Regina"),
//                    new Street("Road")
//            });
//
//            School school = new School("WADA");
//            school.setAddresses(Arrays.asList(address));
//
//            Student charline = new Student("Charline");
//            Set<Student> students = new HashSet<>();
//            students.add(charline);
//            school.setStudents(students);
//
//            pm.makePersistent(charline);
//            pm.makePersistent(school);
//        }, false);
//
//
//        executeTx(pm -> {
//            Object id = new DatastoreIdImplKodo(School.class.getName(), 1);
//            School p = pm.getObjectById(School.class, id);
//            pm.deletePersistent(p);
//        });
//
//
//        final IsPojo<Entity> address = getEntity(DELETE, Address.class, "1")
//                .withProperty("fields", hasItem(
//                        getContainerField(Field.Type.COLLECTION, "street")
//                                .withProperty("removed", hasItems(
//                                        getListElement(Field.Type.REF, Street.class, "1"),
//                                        getListElement(Field.Type.REF, Street.class, "2")
//                                ))
//                ));
//
//        final IsPojo<Entity> school = getEntity(DELETE, School.class, "1")
//                .withProperty("fields", hasItems(
//                        getField(Field.Type.PRIMITIVE, String.class, "name", "WADA", null),
//                        getContainerField(Field.Type.COLLECTION, "addresses")
//                                .withProperty("removed", hasItems(
//                                        getListElement(Field.Type.REF, Address.class, "1")
//                                )),
//                        getContainerField(Field.Type.COLLECTION, "students")
//                                .withProperty("removed", hasItems(
//                                        getListElement(Field.Type.REF, Student.class, "1")
//                                ))
//                ));
//
//        assertThat(audit.getModifications(), hasItem(
//                address
//        ));
//
//        assertThat(audit.getModifications(), hasItem(
//                school
//        ));
//
//        // only the address is defined as a dependent field of school, so address object will be deleted.
//        // individual streets are not dependent fields of address, so they will NOT be cascade deleted
//        assertThat(audit.getModifications(), containsInAnyOrder(
//                address,
//                school
//        ));
//
//
//    }
//
//
//    @Test
//    public void updateCollectionArray() throws IOException {
//        executeTx(pm -> {
//            Address address = new Address(new Street[]{new Street("Regina")});
//            pm.makePersistent(address);
//        }, false);
//
//        executeTx(pm -> {
//            Street street = new Street("Calgary");
//            Object id = new DatastoreIdImplKodo(Address.class.getName(), 1);
//            Address p = pm.getObjectById(Address.class, id);
//            p.replaceStreet(0, street);
//        });
//
//
//
//        final IsPojo<Entity> street = getEntity(CREATE, Street.class, "1")
//                .withProperty("fields", hasItem(
//                        getField(Field.Type.PRIMITIVE, String.class, "name", "Regina", null)
//                ));
//
//
//        final IsPojo<Entity> address = getEntity(CREATE, Address.class, "1")
//                .withProperty("fields", hasItem(
//                        getContainerField(Field.Type.COLLECTION, "street")
//                                .withProperty("added", hasItem(
//                                        getListElement(Field.Type.REF, Street.class, "1")
//                                ))
//                ));
//
//
//        Collection<Entity> entities = audit.getModifications();
//        assertThat(entities, containsInAnyOrder(street, address));
//    }
//
//
//
////
////    @Test
////    public void updateCollectionList() {
////        executeTx(pm -> {
////                    Address address = new Address(new Street[]{
////                            new Street("Regina"),
////                            new Street("Road")
////                    });
////
////                    School school = new School("WADA");
////                    school.setAddresses(Arrays.asList(address));
////
////                    Student charline = new Student("Charline");
////                    Set<Student> students = new HashSet<>();
////                    students.add(charline);
////                    school.setStudents(students);
////
////                    pm.makePersistent(charline);
////                    pm.makePersistent(school);
////                }, false);
////
////
////        executeTx(pm -> {;
////
////            School school = pm.getObjectById(School.class, new DatastoreIdImplKodo(School.class.getName(), 1));
////            List<Address> addresses = school.getAddresses();
////
////            Address calgary = new Address(new Street[]{
////                    new Street("Calgary")
////            });
////
////            // delete all addresss
////            addresses.clear();
////
////            // add the new address
////            addresses.add(calgary);
////
////        }, true);
////    }
//
//    @Test
//    public void updateListTest(){
//        executeTx(pm -> {
//            TelephoneBook book = new TelephoneBook("Montreal");
//            CountryCode canada = new CountryCode("Canada", 1);
//            for( int i = 0; i< 10; i++){
//                Telephone telephone = new Telephone( "514-555-111" + i, canada);
//                book.addTelephoneNumber(telephone);
//            }
//            pm.makePersistent( book);
//        }, false);
//
//
//
//        executeTx(pm -> {
//
//            TelephoneBook telephoneBook = pm.newJDOQLTypedQuery(TelephoneBook.class).filter(QTelephoneBook.candidate().name.eq("Montreal")).executeUnique();
//            CountryCode canada = pm.newJDOQLTypedQuery(CountryCode.class).filter( QCountryCode.candidate().code.eq(1)).executeUnique();
//
//            // add a new telephone number to the book
//            telephoneBook.addTelephoneNumber(new Telephone("514-555-5555", canada));
//
//            Telephone tel3 = pm.newJDOQLTypedQuery(Telephone.class).filter(QTelephone.candidate().number.endsWith("3")).executeUnique();
//            // delete a telephone number.  Since it is only the telephone Entity being deleted, no changes should be seen in the TelephoneBook
//            pm.deletePersistent(tel3);
//        });
//
//
////        executeTx(pm -> {
////            JDOQLTypedQuery<TelephoneBook> tqTb = pm.newJDOQLTypedQuery(TelephoneBook.class);
////            QTelephoneBook cand = QTelephoneBook.candidate();
////            TelephoneBook telephoneBook = tqTb.filter(cand.name.eq("Montreal")).executeUnique();
////            telephoneBook.getTelephoneNumbers();
////        });
//
//
//        final IsPojo<Entity> five555 = getEntity(CREATE, Telephone.class, ANY)
//                .withProperty( "fields", hasItems(
//                        getField(Field.Type.PRIMITIVE, String.class, "number", "514-555-5555", null),
//                        getField(Field.Type.REF, CountryCode.class, "countryCode", "1", null)
//                        )
//                );
//
//        final IsPojo<Entity> one113 = getEntity(DELETE, Telephone.class, ANY)
//                .withProperty( "fields", hasItems(
//                        getField(Field.Type.PRIMITIVE, String.class, "number", "514-555-1113", null),
//                        getField(Field.Type.REF, CountryCode.class, "countryCode", "1", null)
//                        )
//                );
//
//
//        final IsPojo<Entity> telephoneBook = getEntity(UPDATE, TelephoneBook.class, "1")
//                .withProperty("fields", hasItem(
//                        getContainerField(Field.Type.COLLECTION, "telephoneNumbers")
//                                .withProperty("added", hasItem(
//                                        getListElement(Field.Type.REF, Telephone.class, "11")
//                                ))
//                ));
//
//        Collection<Entity> entities = audit.getModifications();
//        assertThat(entities, hasItem( telephoneBook));
//
//
//
//
//        final IsPojo<Entity> street = getEntity(CREATE, Street.class, "1")
//                .withProperty("fields", hasItem(
//                        getField(Field.Type.PRIMITIVE, String.class, "name", "Regina", null)
//                ));
//
//
//        final IsPojo<Entity> address = getEntity(CREATE, Address.class, "1")
//                .withProperty("fields", hasItem(
//                        getContainerField(Field.Type.COLLECTION, "street")
//                                .withProperty("added", hasItem(
//                                        getListElement(Field.Type.REF, Street.class, "1")
//                                ))
//                ));
//
//
//    }
}
