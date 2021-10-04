package org.datanucleus.test;

import com.spotify.hamcrest.pojo.IsPojo;
import mydomain.datatrail.Entity;
import mydomain.datatrail.field.Field;
import mydomain.model.CountryCode;
import mydomain.model.Student;
import mydomain.model.Telephone;
import mydomain.model.TelephoneType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.io.IOException;
import java.util.Collection;

import static mydomain.datatrail.Entity.Action.CREATE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anything;
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

        final IsPojo<Entity> student = getEntity(CREATE, Student.class, "1")
                .withProperty("fields", hasItems(
                        getField(Field.Type.PRIMITIVE, String.class, "name", "student", null),
                        getContainerField(Field.Type.MAP, "telephoneNbs")
                            .withProperty("contents", hasItem(
                                    getMapElement(Field.Type.PRIMITIVE, TelephoneType.class, TelephoneType.HOME.toString(), Field.Type.REF, Telephone.class, "1")
                            ))
                ));


        final IsPojo<Entity> cc = getEntity(CREATE, CountryCode.class, "1")
                .withProperty("fields", hasItems(
                        getField(Field.Type.PRIMITIVE, String.class, "country", "canada", null),
                        getField(Field.Type.PRIMITIVE, Integer.class, "code", "1", null)
                ));

        final IsPojo<Entity> telephone = getEntity(CREATE, Telephone.class, "1")
                .withProperty("fields", hasItems(
                        getField(Field.Type.PRIMITIVE, String.class, "number", "514-555-5555", null),
                        getField(Field.Type.REF, CountryCode.class, "countryCode", "1", null)
                ));


        Collection<Entity> entities = audit.getModifications();
        assertThat(entities, containsInAnyOrder(student, cc, telephone));
    }


}
