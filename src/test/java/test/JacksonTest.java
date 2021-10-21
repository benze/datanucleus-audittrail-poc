package test;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import mydomain.datatrail.Entity;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JacksonTest {

    @Test
    void getJson() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JavaTimeModule module = new JavaTimeModule();
        mapper.registerModule(module);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false);

        List<Map.Entry> contents = new ArrayList<>();
        contents.add(new AbstractMap.SimpleImmutableEntry("keyString", "valueString"));


        StringWriter sw = new StringWriter();
//        for (Entity entity : entities)
            mapper.writeValue(sw, contents);
        System.out.println( sw.toString());
    }

}
