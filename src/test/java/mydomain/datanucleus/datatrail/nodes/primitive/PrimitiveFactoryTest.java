package mydomain.datanucleus.datatrail.nodes.primitive;

import mockit.Invocation;
import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit5.JMockitExtension;
import mydomain.datanucleus.datatrail.nodes.StringConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@ExtendWith(JMockitExtension.class)
public class PrimitiveFactoryTest {

    PrimitiveFactory primitiveFactory;

    @BeforeEach
    public void setup(){
        primitiveFactory = new PrimitiveFactory();
    }



    @DisplayName("A custom converter can be added to format a specific object (Date)")
    @Test
    public void testDateConverter(){

        // inject a new Date converter
        new MockUp<PrimitiveFactory>() {
            @Mock
            protected Set<StringConverter> loadStringConverters(Invocation invocation) {
                Set<StringConverter> converters = invocation.proceed();
                converters.add( new StringConverter() {
                        @Override
                        public boolean supports(Class<?> clazz) {
                            return Date.class.isAssignableFrom(clazz);
                        }

                        @Override
                        public String getAsString(Object value) {
                            return new SimpleDateFormat("yy-MM-dd").format((Date) value);
                        }
                    });
                return converters;
            }
        };

        primitiveFactory = new PrimitiveFactory();

        assertThat(primitiveFactory.getAsString(new GregorianCalendar(2021, Calendar.FEBRUARY, 01).getTime()), is("21-02-01"));
    }

    @DisplayName("Tests that the null converter works")
    @Test
    public void testNullConverter(){
        assertThat(primitiveFactory.getAsString(null), is(nullValue()));
    }


    @DisplayName("Tests that a new converter with a higher priority is used")
    @Test
    public void testOrderedConverter(){
        // inject a new Null  converter with a higher priority
        new MockUp<PrimitiveFactory>() {
            @Mock
            protected Set<StringConverter> loadStringConverters(Invocation invocation) {
                Set<StringConverter> converters = invocation.proceed();
                converters.add( new StringConverter() {
                    @Override
                    public boolean supports(Class<?> clazz) {
                        return clazz == null;
                    }

                    @Override
                    public String getAsString(Object value) {
                        return "";
                    }
                });
                return converters;
            }
        };

        primitiveFactory = new PrimitiveFactory();

        assertThat(primitiveFactory.getAsString(null), is(emptyString()));
    }


}
