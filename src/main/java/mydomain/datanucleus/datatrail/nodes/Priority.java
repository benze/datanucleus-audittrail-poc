package mydomain.datanucleus.datatrail.nodes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Priority {
    /**
     * Useful constant for the highest precedence value.
     * @see java.lang.Integer#MAX_VALUE
     */
    int HIGHEST_PRECEDENCE = Integer.MAX_VALUE;

    /**
     * Useful constant for the lowest precedence value.
     * @see java.lang.Integer#MIN_VALUE
     */
    int LOWEST_PRECEDENCE = Integer.MIN_VALUE;


    /**
     * The priority/precedence level in which this node will be selected in case multiple nodes implementations are detected
     * @return
     */
    int priority() default 0;
}
