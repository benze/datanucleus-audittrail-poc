package org.datanucleus.test;

import com.spotify.hamcrest.pojo.IsPojo;
import mydomain.datanucleus.datatrail.Node;
import mydomain.datanucleus.datatrail.NodeType;
import org.datanucleus.test.model.Address;
import org.datanucleus.test.model.CountryCode;
import org.datanucleus.test.model.QCountryCode;
import org.datanucleus.test.model.QSchool;
import org.datanucleus.test.model.QTelephone;
import org.datanucleus.test.model.QTelephoneBook;
import org.datanucleus.test.model.School;
import org.datanucleus.test.model.Street;
import org.datanucleus.test.model.Student;
import org.datanucleus.test.model.Telephone;
import org.datanucleus.test.model.TelephoneBook;
import org.datanucleus.identity.DatastoreIdImplKodo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static mydomain.datanucleus.datatrail.NodeAction.CREATE;
import static mydomain.datanucleus.datatrail.NodeAction.DELETE;
import static mydomain.datanucleus.datatrail.NodeAction.UPDATE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

@Execution(ExecutionMode.SAME_THREAD)
public class CollectionTest extends AbstractTest {


    @Test
    public void createCollectionArray() {
        executeTx(pm -> {
            Address address = new Address(new Street[]{new Street("Regina")});
            pm.makePersistent(address);
        });

        final IsPojo<Node> street = getEntity(CREATE, Street.class, "1")
                .withProperty("fields", hasItem(
                        getField(NodeType.PRIMITIVE, String.class, "name", "Regina", null)
                ));


        final IsPojo<Node> address = getEntity(CREATE, Address.class, "1")
                .withProperty("fields", hasItem(
                        getContainerField(NodeType.ARRAY, "street")
                                .withProperty("contents", hasItem(
                                        getListElement(NodeType.REF, Street.class, "1")
                                ))
                ));


        Collection<Node> entities = audit.getModifications();
        assertThat(entities, hasItem(street));
        assertThat(entities, hasItem(address));
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


        final IsPojo<Node> regina = getEntity(CREATE, Street.class, "1")
                .withProperty("fields", hasItem(
                        getField(NodeType.PRIMITIVE, String.class, "name", "Regina", null)
                ));


        final IsPojo<Node> road = getEntity(CREATE, Street.class, "2")
                .withProperty("fields", hasItem(
                        getField(NodeType.PRIMITIVE, String.class, "name", "Road", null)
                ));


        final IsPojo<Node> address = getEntity(CREATE, Address.class, "1")
                .withProperty("fields", hasItem(
                        getContainerField(NodeType.ARRAY, "street")
                                .withProperty("contents", hasItems(
                                        getListElement(NodeType.REF, Street.class, "1"),
                                        getListElement(NodeType.REF, Street.class, "2")
                                ))
                ));


        final IsPojo<Node> student = getEntity(CREATE, Student.class, "1")
                .withProperty("fields", hasItem(
                        getField(NodeType.PRIMITIVE, String.class, "name", "Charline", null)
                ));

        final IsPojo<Node> school = getEntity(CREATE, School.class, "1")
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


        final IsPojo<Node> address = getEntity(DELETE, Address.class, "1")
                .withProperty("fields", hasItem(
                        getContainerField(NodeType.ARRAY, "street")
                                .withProperty("contents", hasItems(
                                        getListElement(NodeType.REF, Street.class, "1"),
                                        getListElement(NodeType.REF, Street.class, "2")
                                ))
                ));

        final IsPojo<Node> school = getEntity(DELETE, School.class, "1")
                .withProperty("fields", hasItems(
                        getField(NodeType.PRIMITIVE, String.class, "name", "WADA", null),
                        getContainerField(NodeType.COLLECTION, "addresses")
                                .withProperty("removed", hasItems(
                                        getListElement(NodeType.REF, Address.class, "1")
                                )),
                        getContainerField(NodeType.COLLECTION, "students")
                                .withProperty("removed", hasItems(
                                        getListElement(NodeType.REF, Student.class, "1")
                                ))
                ));

        assertThat(audit.getModifications(), hasItem(
                address
        ));

        assertThat(audit.getModifications(), hasItem(
                school
        ));

        // only the address is defined as a dependent field of school, so address object will be deleted.
        // individual streets are not dependent fields of address, so they will NOT be cascade deleted
        assertThat(audit.getModifications(), containsInAnyOrder(
                address,
                school
        ));
    }


    /**
     * With an array object, need to dump the entire array contents as there is no way to track
     * changes to the individual elements
     */
    @Test
    public void updateCollectionArray(){
        executeTx(pm -> {
            Address address = new Address(new Street[]{new Street("Regina"), new Street("Montreal")});
            pm.makePersistent(address);
        }, false);

        executeTx(pm -> {
            Street street = new Street("Calgary");
            Object id = new DatastoreIdImplKodo(Address.class.getName(), 1);
            Address p = pm.getObjectById(Address.class, id);
            p.replaceStreet(0, street);
        });



        final IsPojo<Node> calgary = getEntity(CREATE, Street.class, "3")
                .withProperty("fields", hasItem(
                        getField(NodeType.PRIMITIVE, String.class, "name", "Calgary", null)
                ));

        final IsPojo<Node> address = getEntity(UPDATE, Address.class, "1")
                .withProperty("fields", hasItem(
                        getContainerField(NodeType.ARRAY, "street")
                                .withProperty("contents", hasItems(
                                        getListElement(NodeType.REF, Street.class, "2"),
                                        getListElement(NodeType.REF, Street.class, "3")
                                ))
                ));


        Collection<Node> entities = audit.getModifications();
        assertThat(filterEntity(entities, Street.class, CREATE).get(), is(calgary));
        assertThat(filterEntity(entities, Address.class, UPDATE).get(), is(address));
        assertThat(entities, containsInAnyOrder(calgary, address));
    }


    @Test
    public void updateCollectionList() {
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


        executeTx(pm -> {;
            School school = pm.newJDOQLTypedQuery(School.class).filter(QSchool.candidate().name.eq("WADA")).executeUnique();
            List<Address> addresses = school.getAddresses();

            Address calgary = new Address(new Street[]{
                    new Street("Calgary")
            });

            // delete all addresss
            addresses.clear();

            // add the new address
            addresses.add(calgary);

        });

        IsPojo<Node> calgarySt = getEntity(CREATE, Street.class, "3")
                .withProperty("fields", hasItem(
                        getField(NodeType.PRIMITIVE, String.class, "name", "Calgary", null)
                ));


        IsPojo<Node> calgaryAddr = getEntity(CREATE, Address.class, "2")
                .withProperty("fields", hasItem(
                        getContainerField(NodeType.ARRAY, "street")
                                .withProperty("contents", hasItem(
                                                getListElement(NodeType.REF, Street.class, "3")
                                ))
                ));

        IsPojo<Node> school = getEntity(UPDATE, School.class, "1")
                .withProperty("fields", hasItems(
                        getContainerField(NodeType.COLLECTION, "addresses")
                                .withProperty("added", hasItem(
                                        getListElement(NodeType.REF, Address.class, "2")
                                ))
                                .withProperty("removed", hasItem(
                                        getListElement(NodeType.REF, Address.class, "1" )
                                ))
                ));

        assertThat(filterEntity(audit.getModifications(), Street.class, CREATE).get(), is(calgarySt));
        assertThat(filterEntity(audit.getModifications(), Address.class, CREATE).get(), is(calgaryAddr));
        assertThat(filterEntity(audit.getModifications(), School.class, UPDATE).get(), is(school));


        assertThat(audit.getModifications(), hasItem(school));


    }

    @Test
    public void updateListTest(){
        executeTx(pm -> {
            TelephoneBook book = new TelephoneBook("Montreal");
            CountryCode canada = new CountryCode("Canada", 1);
            for( int i = 0; i< 10; i++){
                Telephone telephone = new Telephone( "514-555-111" + i, canada);
                book.addTelephoneNumber(telephone);
            }
            pm.makePersistent( book);
        }, false);



        executeTx(pm -> {
            TelephoneBook telephoneBook = pm.newJDOQLTypedQuery(TelephoneBook.class).filter(QTelephoneBook.candidate().name.eq("Montreal")).executeUnique();
            CountryCode canada = pm.newJDOQLTypedQuery(CountryCode.class).filter( QCountryCode.candidate().code.eq(1)).executeUnique();

            // add a new telephone number to the book
            telephoneBook.addTelephoneNumber(new Telephone("514-555-5555", canada));

            Telephone tel3 = pm.newJDOQLTypedQuery(Telephone.class).filter(QTelephone.candidate().number.endsWith("3")).executeUnique();
            // delete a telephone number.  Since it is only the telephone Entity being deleted, no changes should be seen in the TelephoneBook
            pm.deletePersistent(tel3);
        });


        final IsPojo<Node> five555 = getEntity(CREATE, Telephone.class, ANY)
                .withProperty( "fields", hasItems(
                        getField(NodeType.PRIMITIVE, String.class, "number", "514-555-5555", null),
                        getField(NodeType.REF, CountryCode.class, "countryCode", "1", null)
                        )
                );


        final IsPojo<Node> one113 = getEntity(DELETE, Telephone.class, ANY)
                .withProperty( "fields", hasItems(
                        getField(NodeType.PRIMITIVE, String.class, "number", "514-555-1113", null),
                        getField(NodeType.REF, CountryCode.class, "countryCode", "1", null)
                        )
                );


        final IsPojo<Node> telephoneBook = getEntity(UPDATE, TelephoneBook.class, "1")
                .withProperty("fields", hasItem(
                        getContainerField(NodeType.COLLECTION, "telephoneNumbers")
                                .withProperty("added", hasItem(
                                        getListElement(NodeType.REF, Telephone.class, "11")
                                ))
                ));

        Collection<Node> entities = audit.getModifications();
        assertThat(filterEntity(entities, Telephone.class, CREATE).get(), is( five555));
        assertThat(filterEntity(entities, Telephone.class, DELETE).get(), is( one113));
        assertThat(filterEntity(entities, TelephoneBook.class, UPDATE).get(), is(telephoneBook));
        assertThat(entities, containsInAnyOrder(five555, one113, telephoneBook));
    }


    @DisplayName("Changes the order of a items in a list")
    @Test
    public void updateListOrderTest() {
        executeTx(pm -> {
            TelephoneBook book = new TelephoneBook("Montreal");
            CountryCode canada = new CountryCode("Canada", 1);
            for( int i = 0; i< 3; i++){
                Telephone telephone = new Telephone( "514-555-111" + i, canada);
                book.addTelephoneNumber(telephone);
            }
            pm.makePersistent( book);
        }, false);


        // move the first telephone number to the last one in the list
        executeTx(pm -> {
            TelephoneBook telephoneBook = pm.newJDOQLTypedQuery(TelephoneBook.class).filter(QTelephoneBook.candidate().name.eq("Montreal")).executeUnique();

            List<Telephone> telephones = telephoneBook.getTelephoneNumbers();
            telephones.add(telephones.remove(0));
        });

        final IsPojo<Node> telephoneBook = getEntity(UPDATE, TelephoneBook.class, "1")
                .withProperty("fields", hasItem(
                        getContainerField(NodeType.COLLECTION, "telephoneNumbers")
                                .withProperty("contents", contains(
                                        getListElement(NodeType.REF, Telephone.class, "2"),
                                        getListElement(NodeType.REF, Telephone.class, "3"),
                                        getListElement(NodeType.REF, Telephone.class, "1")
                                ))
                ));



    }

}
