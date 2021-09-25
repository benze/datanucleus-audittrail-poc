package org.datanucleus.test;

import mydomain.datatrail.Entity;
import mydomain.model.Street;
import org.datanucleus.util.NucleusLogger;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class PrimitiveTest extends AbstractTest {
    @Test
    public void createPrimitive() throws IOException {
        executeTx((pm) -> {
            Street street = new Street("Regina");
            pm.makePersistent(street);
        });


        List<Entity> entities = audit.getModifications();
        assertThat(entities, hasSize(1));
        Entity entity = entities.get(0);

        assertThat(entity, allOf(
                hasProperty("action", hasToString("CREATE")),
                hasProperty("id", is("1")),
                hasProperty("dateModified", notNullValue()),
                hasProperty("fields", hasSize(1))
        ));


        assertThat(entity.getFields().get(0), allOf(
                hasProperty("name", is("name")),
                hasProperty("type", hasToString("PRIMITIVE")),
                hasProperty("value", is("Regina")),
                hasProperty("className", is(String.class.getName()))
        ));


        // check that the datatrail log is correct
        NucleusLogger.GENERAL.info(getJson(audit.getModifications()));
    }
}
