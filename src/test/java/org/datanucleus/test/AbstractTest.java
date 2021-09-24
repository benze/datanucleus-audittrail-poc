package org.datanucleus.test;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import mydomain.datatrail.Entity;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

abstract public class AbstractTest {

    protected String getJson(List<Entity> entities) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JavaTimeModule module = new JavaTimeModule();
        mapper.registerModule(module);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        StringWriter sw = new StringWriter();
        for( Entity entity : entities)
            mapper.writeValue(sw, entity);
        return sw.toString();
    }
}
