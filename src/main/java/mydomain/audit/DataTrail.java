package mydomain.audit;


import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface DataTrail {
    /**
     * Indicates that a {@link javax.jdo.annotations.Persistent} object should be excluded
     * from the DataTrail.
     *
     * If this annotation is applied to a {@link org.datanucleus.enhancement.Persistable} class, it will
     * be skipped when generating the list of top-level entities.  However, if it is used as a reference
     * in another object, it will still be included.
     *
     * This annotation is inherited, so any child class extending a base class with this annotation will
     * also be considered with the same values as the base class, unless explicitly overriding it
     *
     * @return true if it should be excluded
     */
    boolean excludeFromDataTrail() default false;
}
