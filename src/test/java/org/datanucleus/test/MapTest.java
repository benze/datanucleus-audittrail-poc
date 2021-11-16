package org.datanucleus.test;

import com.spotify.hamcrest.pojo.IsPojo;
import mydomain.datanucleus.datatrail.Node;
import mydomain.datanucleus.datatrail.NodeType;
import org.datanucleus.test.model.CountryCode;
import org.datanucleus.test.model.QCountryCode;
import org.datanucleus.test.model.QStreet;
import org.datanucleus.test.model.QStudent;
import org.datanucleus.test.model.Street;
import org.datanucleus.test.model.Student;
import org.datanucleus.test.model.Telephone;
import org.datanucleus.test.model.TelephoneType;
import org.datanucleus.enhancement.Persistable;
import org.datanucleus.test.model.MapClass;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static mydomain.datanucleus.datatrail.NodeAction.CREATE;
import static mydomain.datanucleus.datatrail.NodeAction.DELETE;
import static mydomain.datanucleus.datatrail.NodeAction.UPDATE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@Execution(ExecutionMode.SAME_THREAD)
public class MapTest extends AbstractTest {


    @Test
    public void createMap() throws IOException {
        executeTx(pm -> {

            CountryCode cc = new CountryCode("canada", 1);

            Student student = new Student("student");
            student.addTelephoneNb(TelephoneType.HOME, new Telephone("514-555-5555", cc));
            student.addMark("english", "A");

            pm.makePersistent(student);
        });

        final IsPojo<Node> student = getEntity(CREATE, Student.class, "1")
                .withProperty("fields", hasItems(
                        getField(NodeType.PRIMITIVE, String.class, "name", "student", null),
                        getContainerField(NodeType.MAP, "telephoneNbs")
                                .withProperty("added", hasItem(
                                        getMapElement(NodeType.PRIMITIVE, TelephoneType.class, TelephoneType.HOME.toString(), NodeType.REF, Telephone.class, "1", null)
                                ))
                ));


        final IsPojo<Node> cc = getEntity(CREATE, CountryCode.class, "1")
                .withProperty("fields", hasItems(
                        getField(NodeType.PRIMITIVE, String.class, "country", "canada", null),
                        getField(NodeType.PRIMITIVE, Integer.class, "code", "1", null)
                ));

        final IsPojo<Node> telephone = getEntity(CREATE, Telephone.class, "1")
                .withProperty("fields", hasItems(
                        getField(NodeType.PRIMITIVE, String.class, "number", "514-555-5555", null),
                        getField(NodeType.REF, CountryCode.class, "countryCode", "1", null)
                ));


        Collection<Node> entities = audit.getModifications();
        assertThat(entities, hasItem(cc));
        assertThat(entities, hasItem(telephone));
        assertThat(filterEntity(entities, Student.class, CREATE).get(), is(student));
        assertThat(entities, hasItem(student));
        assertThat(entities, containsInAnyOrder(student, cc, telephone));
    }


    @DisplayName("Deletes an Entity containing a Map object.")
    @Test
    public void deleteMap() throws IOException {
        Map<TelephoneType, String> ids = new HashMap<>();

        executeTx(pm -> {
            CountryCode cc = new CountryCode("canada", 1);

            Student student = new Student("student");
            student.addTelephoneNb(TelephoneType.HOME, new Telephone("514-555-5555", cc));
            student.addTelephoneNb(TelephoneType.MOBILE, new Telephone("514-777-5555", cc));
            student.addMark("english", "A");
            student.addMark("french", "A+");

            pm.makePersistent(student);

            ids.put(TelephoneType.HOME, getId((Persistable) student.getTelephoneNbs().get(TelephoneType.HOME)));
            ids.put(TelephoneType.MOBILE, getId((Persistable) student.getTelephoneNbs().get(TelephoneType.MOBILE)));
        }, false);


        // test removal of map members
        executeTx(pm -> {
            // find the student
            Student student = pm.newJDOQLTypedQuery(Student.class).filter(QStudent.candidate().name.eq("student")).executeUnique();
            pm.deletePersistent(student);
        });


        final IsPojo<Node> student = getEntity(DELETE, Student.class, "1")
                .withProperty("fields", hasItems(
                        getField(NodeType.PRIMITIVE, String.class, "name", "student", null),
                        getContainerField(NodeType.MAP, "telephoneNbs")
                                .withProperty("removed", hasItems(
                                        getMapElement(NodeType.PRIMITIVE, TelephoneType.class, TelephoneType.HOME.toString(), NodeType.REF, Telephone.class, ids.get(TelephoneType.HOME), null),
                                        getMapElement(NodeType.PRIMITIVE, TelephoneType.class, TelephoneType.MOBILE.toString(), NodeType.REF, Telephone.class, ids.get(TelephoneType.MOBILE), null)
                                )),
                        getContainerField(NodeType.MAP, "marks")
                                .withProperty("removed", hasItems(
                                        getMapElement(NodeType.PRIMITIVE, String.class, "english", NodeType.PRIMITIVE, String.class, "A", null),
                                        getMapElement(NodeType.PRIMITIVE, String.class, "french", NodeType.PRIMITIVE, String.class, "A+", null)
                                ))
                ));


        Collection<Node> entities = audit.getModifications();
        assertThat(entities, hasItem(student));
        assertThat("Should show an entity with a Map containing removed items", entities, contains(student));
    }


    @DisplayName("Updates the Key/Value pairs of a Map<Prim,Prim> map")
    @Test
    public void updateMapPrim() throws IOException {
        Map<TelephoneType, String> ids = new HashMap<>();

        executeTx(pm -> {
            CountryCode cc = new CountryCode("canada", 1);

            Student student = new Student("student");
            student.addTelephoneNb(TelephoneType.HOME, new Telephone("514-555-5555", cc));
            student.addTelephoneNb(TelephoneType.MOBILE, new Telephone("514-777-5555", cc));
            student.addMark("english", "A");
            student.addMark("french", "A+");

            pm.makePersistent(student);

            ids.put(TelephoneType.HOME, getId((Persistable) student.getTelephoneNbs().get(TelephoneType.HOME)));
            ids.put(TelephoneType.MOBILE, getId((Persistable) student.getTelephoneNbs().get(TelephoneType.MOBILE)));
        }, false);


        // test removal and change of map members
        executeTx(pm -> {
            // find the student
            Student student = pm.newJDOQLTypedQuery(Student.class).filter(QStudent.candidate().name.eq("student")).executeUnique();
            student.addMark("german", "B");
            student.getMarks().remove("french");
            student.addMark("english", "B");
        });


        final IsPojo<Node> student = getEntity(UPDATE, Student.class, "1")
                .withProperty("fields", hasItems(
                        getContainerField(NodeType.MAP, "marks")
                                .withProperty("added", hasItems(
                                        getMapElement(NodeType.PRIMITIVE, String.class, "german", NodeType.PRIMITIVE, String.class, "B", null)
                                ))
                                .withProperty("removed", hasItems(
                                        getMapElement(NodeType.PRIMITIVE, String.class, "french", NodeType.PRIMITIVE, String.class, "A+", null)
                                ))
                                .withProperty("changed", hasItems(
                                        getMapElement(NodeType.PRIMITIVE, String.class, "english", NodeType.PRIMITIVE, String.class, "B",
                                                getField(NodeType.PRIMITIVE, String.class, null, "A", null))
                                ))
                ));


        Collection<Node> entities = audit.getModifications();
        assertThat(entities, hasItem(student));
        assertThat(entities, contains(student));
    }


    @DisplayName("Updates the Key/Value pairs of a Map<Prim,Ref> map")
    @Test
    public void updateMapRef() throws IOException {
        Map<TelephoneType, String> ids = new HashMap<>();

        executeTx(pm -> {
            CountryCode cc = new CountryCode("canada", 1);

            Student student = new Student("student");
            student.addTelephoneNb(TelephoneType.HOME, new Telephone("514-555-5555", cc));
            student.addTelephoneNb(TelephoneType.MOBILE, new Telephone("514-777-5555", cc));

            pm.makePersistent(student);

            ids.put(TelephoneType.HOME, getId((Persistable) student.getTelephoneNbs().get(TelephoneType.HOME)));
            ids.put(TelephoneType.MOBILE, getId((Persistable) student.getTelephoneNbs().get(TelephoneType.MOBILE)));
        }, false);


        // test removal and change of map members
        executeTx(pm -> {

            CountryCode cc = new CountryCode("USA", 1);

            // find the student
            Student student = pm.newJDOQLTypedQuery(Student.class).filter(QStudent.candidate().name.eq("student")).executeUnique();
            student.getTelephoneNbs().put(TelephoneType.MOBILE, new Telephone("514-555-9999", cc));
        });

        final IsPojo<Node> telephone = getEntity(CREATE, Telephone.class, ANY)
                .withProperty("fields", hasItems(
                        getField(NodeType.PRIMITIVE, String.class, "number", "514-555-9999", null),
                        getField(NodeType.REF, CountryCode.class, "countryCode", ANY, null)
                ));


        final IsPojo<Node> student = getEntity(UPDATE, Student.class, "1")
                .withProperty("fields", hasItems(
                        getContainerField(NodeType.MAP, "telephoneNbs")
                                .withProperty("changed", hasItems(
                                        getMapElement(NodeType.PRIMITIVE, TelephoneType.class, TelephoneType.MOBILE.toString(), NodeType.REF, Telephone.class, ANY,
                                                getField(NodeType.REF, Telephone.class, null, ids.get(TelephoneType.MOBILE), null ))
                                ))
                ));

        final IsPojo<Node> usa = getEntity(CREATE, CountryCode.class, ANY)
                .withProperty("fields", hasItems(
                        getField(NodeType.PRIMITIVE, String.class, "country", "USA", null)
                ));


        Collection<Node> entities = audit.getModifications();
        assertThat(filterEntity(entities, CountryCode.class, CREATE).get(), is(usa));
        assertThat(entities, hasItem(usa));

        assertThat(filterEntity(entities, Student.class, UPDATE).get(), is(student));
        assertThat(entities, hasItem(student));
        assertThat(entities, containsInAnyOrder(student, usa, telephone));

    }


    @DisplayName("Test Map<Ref, Ref> objects")
    @Test
    public void updateMapRefRefTest() {

        // Create necessary objects
        Map<String, Persistable> ids = new HashMap<>();
        executeTx(pm -> {
            List<Object> objs = Arrays.asList(
                    // streets
                    new Street("Victoria"),
                    new Street("Younge"),
                    new Street("Hastings"),

                    // country codes (reuse as area code)
                    new CountryCode("Montreal", 514),
                    new CountryCode("Toronto", 416),
                    new CountryCode("Vancouver", 604)
            );

            objs.forEach(pc -> {
                pm.makePersistent(pc);

                // save detached copies for future reuse
                String key = pc instanceof Street ? ((Street) pc).getName() : ((CountryCode) pc).getCountry();
                ids.put(key, (Persistable) pm.detachCopy(pc));
            });
        }, false);


        // set all streets to montreal area code
        executeTx(pm -> {
            // fetch all the streets
            List<Street> streets = pm.newJDOQLTypedQuery(Street.class).executeList();
            CountryCode montreal = pm.newJDOQLTypedQuery(CountryCode.class).filter(QCountryCode.candidate().code.eq(514)).executeUnique();

            MapClass sut = new MapClass();
            pm.makePersistent(sut);

            Map<Street, CountryCode> map = sut.getStreetMap();
            streets.stream().forEach(street -> map.put(street, montreal));
        });


        IsPojo<Node> mapClass = getEntity(CREATE, MapClass.class, ANY)
                .withProperty("fields", hasItem(
                        getContainerField(NodeType.MAP, "streetMap")
                                .withProperty("added", hasItems(
                                        // create a mapElement for each street
                                        getMapElement(NodeType.REF, Street.class, getId(ids.get("Victoria")), NodeType.REF, CountryCode.class, getId(ids.get("Montreal")), null),
                                        getMapElement(NodeType.REF, Street.class, getId(ids.get("Younge")), NodeType.REF, CountryCode.class, getId(ids.get("Montreal")), null),
                                        getMapElement(NodeType.REF, Street.class, getId(ids.get("Hastings")), NodeType.REF, CountryCode.class, getId(ids.get("Montreal")), null)
                                ))
                ));

        assertThat("All streets mapped to Mtl area code", audit.getModifications(), contains(mapClass));


        // Change the streets to their propert area codes
        executeTx(pm -> {
            MapClass sut = pm.newJDOQLTypedQuery(MapClass.class).executeUnique();

            // updates all entries in the maps as per the `streetCodes` list
            Arrays.asList(
                    new SimpleEntry<>("Victoria", "Montreal"),
                    new SimpleEntry<>("Younge", "Toronto"),
                    new SimpleEntry<>("Hastings", "Vancouver")
            ).stream().forEach(entry -> {
                Street street = pm.newJDOQLTypedQuery(Street.class).filter(QStreet.candidate().name.eq(entry.getKey())).executeUnique();
                CountryCode countryCode = pm.newJDOQLTypedQuery(CountryCode.class).filter(QCountryCode.candidate().country.eq(entry.getValue())).executeUnique();
                sut.getStreetMap().put(street, countryCode);
            });

            assertThat("Only 3 area codes exist", sut.getStreetMap(), aMapWithSize(3));
        });


        mapClass = getEntity(UPDATE, MapClass.class, ANY)
                .withProperty("fields", hasItem(
                        getContainerField(NodeType.MAP, "streetMap")
                                .withProperty("changed", hasItems(
                                        // create a mapElement for each street
                                        // Montreal is considered as changed even though it is the same value
                                        getMapElement(NodeType.REF, Street.class, getId(ids.get("Victoria")), NodeType.REF, CountryCode.class, getId(ids.get("Montreal")),
                                                getField(NodeType.REF, CountryCode.class, null, getId(ids.get("Montreal")), null)),
                                        getMapElement(NodeType.REF, Street.class, getId(ids.get("Younge")), NodeType.REF, CountryCode.class, getId(ids.get("Toronto")),
                                                getField(NodeType.REF, CountryCode.class, null, getId(ids.get("Montreal")), null)),
                                        getMapElement(NodeType.REF, Street.class, getId(ids.get("Hastings")), NodeType.REF, CountryCode.class, getId(ids.get("Vancouver")),
                                                getField(NodeType.REF, CountryCode.class, null, getId(ids.get("Montreal")), null))
                                ))
                ));

        assertThat("All streets mapped to their proper area codes", audit.getModifications(), contains(mapClass));

        // delete an item from the map
        executeTx(pm -> {
            MapClass sut = pm.newJDOQLTypedQuery(MapClass.class).executeUnique();

            Street street = pm.newJDOQLTypedQuery(Street.class).filter(QStreet.candidate().name.eq( "Victoria")).executeUnique();
            sut.getStreetMap().remove(street);

            assertThat("Only 2 area codes exist", sut.getStreetMap(), aMapWithSize(2));
        });

        mapClass = getEntity(UPDATE, MapClass.class, ANY)
                .withProperty("fields", hasItem(
                        getContainerField(NodeType.MAP, "streetMap")
                                .withProperty("removed", hasItems(
                                        getMapElement(NodeType.REF, Street.class, getId(ids.get("Victoria")), NodeType.REF, CountryCode.class, getId(ids.get("Montreal")), null)
                                ))
                ));

        assertThat("Victoria street has been deleted", audit.getModifications(), contains(mapClass));
    }


    @DisplayName("Updating Map with same key/value pair should trigger a DataTrail entry with a version change only")
    @Test
    public void updateMapWithSameKeyValuePair() {
        executeTx(pm -> {
            MapClass sut = new MapClass();
            sut.getStreetMap().put(new Street("Victoria"), new CountryCode("Montreal", 514));
            pm.makePersistent(sut);
        }, false);

        final Map<String, String> ids = new HashMap<>();
        executeTx(pm -> {
            MapClass sut = pm.newJDOQLTypedQuery(MapClass.class).executeUnique();
            Map.Entry<Street, CountryCode> entry = sut.getStreetMap().entrySet().iterator().next();
            sut.getStreetMap().put(entry.getKey(), entry.getValue());

            ids.put("Victoria", getId((Persistable) entry.getKey()));
            ids.put("Montreal", getId((Persistable) entry.getValue()));
        });


        IsPojo<Node> mapClass = getEntity(UPDATE, MapClass.class, ANY)
                .withProperty("fields", hasItem(
                        getContainerField(NodeType.MAP, "streetMap")
                                .withProperty("changed", hasItems(
                                        getMapElement(NodeType.REF, Street.class, ids.get("Victoria"), NodeType.REF, CountryCode.class, ids.get("Montreal"),
                                                getField(NodeType.REF, CountryCode.class, null, ids.get("Montreal"), null))
                                ))
                ))
                ;

        assertThat("Should be 1 modification update since the version has effectively changed", audit.getModifications(), contains(mapClass));
    }


    @DisplayName("Add key and update key in same tx Map<Ref,Ref>.  Should show as added")
    @Test
    public void addKeyAndUpdate(){
        executeTx(pm -> {
            MapClass sut = new MapClass();
            pm.makePersistent(sut);
        }, false);


        Map<String, String> ids = new HashMap<>();
        executeTx(pm ->{
            MapClass sut = pm.newJDOQLTypedQuery(MapClass.class).executeUnique();
            Street victoria = new Street("Victoria");
            CountryCode vancouver = new CountryCode("Vancouver", 604);
            sut.getStreetMap().put(victoria, new CountryCode("Montreal", 514));
            sut.getStreetMap().put(victoria, vancouver);
            pm.flush();

            ids.put( "Victoria", getId((Persistable) victoria));
            ids.put( "Vancouver", getId((Persistable) vancouver));

        });


        IsPojo<Node> mapClass = getEntity(UPDATE, MapClass.class, ANY)
                .withProperty("fields", hasItem(
                        getContainerField(NodeType.MAP, "streetMap")
                                .withProperty("added", hasItems(
                                        getMapElement(NodeType.REF, Street.class, ids.get("Victoria"), NodeType.REF, CountryCode.class, ids.get("Vancouver"), null)
                                ))
                ))
                ;

        assertThat("Should be 1 modification added", audit.getModifications(), hasItem(mapClass));

    }
}
