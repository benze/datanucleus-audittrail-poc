package org.datanucleus.test2;

import com.spotify.hamcrest.pojo.IsPojo;
import mydomain.datanucleus.datatrail2.Node;
import mydomain.datanucleus.datatrail2.NodeType;
import mydomain.model.CountryCode;
import mydomain.model.QStudent;
import mydomain.model.Student;
import mydomain.model.Telephone;
import mydomain.model.TelephoneType;
import org.datanucleus.enhancement.Persistable;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import javax.jdo.JDOHelper;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static mydomain.datanucleus.datatrail2.Node.Action.CREATE;
import static mydomain.datanucleus.datatrail2.Node.Action.DELETE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;

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
                                    getMapElement(NodeType.PRIMITIVE, TelephoneType.class, TelephoneType.HOME.toString(), NodeType.REF, Telephone.class, "1")
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
        assertThat(entities, hasItem(student));
        assertThat(entities, containsInAnyOrder(student, cc, telephone));
    }


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

            ids.put( TelephoneType.HOME, getId((Persistable) student.getTelephoneNbs().get(TelephoneType.HOME)));
            ids.put( TelephoneType.MOBILE, getId((Persistable) student.getTelephoneNbs().get(TelephoneType.MOBILE)));
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
                                    getMapElement(NodeType.PRIMITIVE, TelephoneType.class, TelephoneType.HOME.toString(), NodeType.REF, Telephone.class, ids.get(TelephoneType.HOME)),
                                    getMapElement(NodeType.PRIMITIVE, TelephoneType.class, TelephoneType.MOBILE.toString(), NodeType.REF, Telephone.class, ids.get(TelephoneType.MOBILE))
                            )),
                        getContainerField(NodeType.MAP, "marks")
                            .withProperty("removed", hasItems(
                                    getMapElement(NodeType.PRIMITIVE, String.class, "english", NodeType.PRIMITIVE, String.class, "A"),
                                    getMapElement(NodeType.PRIMITIVE, String.class, "french", NodeType.PRIMITIVE, String.class, "A+")
                            ))
                ));


        Collection<Node> entities = audit.getModifications();
        assertThat(entities, hasItem(student));
        assertThat(entities, contains(student));
    }


}
