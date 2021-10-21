package org.datanucleus.test2;

import com.spotify.hamcrest.pojo.IsPojo;
import mydomain.datanucleus.datatrail2.Node;
import mydomain.datanucleus.datatrail2.NodeType;
import mydomain.model.CountryCode;
import mydomain.model.Student;
import mydomain.model.Telephone;
import mydomain.model.TelephoneType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.io.IOException;
import java.util.Collection;

import static mydomain.datanucleus.datatrail2.Node.Action.CREATE;
import static org.hamcrest.MatcherAssert.assertThat;
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


}
