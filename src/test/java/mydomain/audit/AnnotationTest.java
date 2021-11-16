package mydomain.audit;

import com.zaradai.matchers.IsClassAnnotated;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.is;

class AnnotationTest {

    @DataTrail(excludeFromDataTrail = true)
    static public class ParentClass{
    }

    @DataTrail(excludeFromDataTrail = false)
    static public class ChildInDataTrailClass extends ParentClass{
    }

    static public class ChildInheritedClass extends ParentClass{
    }

    @Test
    public void testAnnotationPresentParent(){
        ParentClass sut = new ParentClass();
        MatcherAssert.assertThat( sut, IsClassAnnotated.with( DataTrail.class) );
        MatcherAssert.assertThat( sut, IsClassAnnotated.withParamValue( DataTrail.class, "excludeFromDataTrail", is(true) ) );
    }

    @Test void testAnnotationChildPresent(){
        ParentClass sut = new ChildInDataTrailClass();
        MatcherAssert.assertThat( sut, IsClassAnnotated.with( DataTrail.class) );
        MatcherAssert.assertThat( sut, IsClassAnnotated.withParamValue( DataTrail.class, "excludeFromDataTrail", is(false) ) );
    }

    @Test void testAnnotationChildInheritedPresent(){
        ParentClass sut = new ChildInheritedClass();
        MatcherAssert.assertThat( sut, IsClassAnnotated.with( DataTrail.class) );
        MatcherAssert.assertThat( sut, IsClassAnnotated.withParamValue( DataTrail.class, "excludeFromDataTrail", is(true) ) );
    }


}
