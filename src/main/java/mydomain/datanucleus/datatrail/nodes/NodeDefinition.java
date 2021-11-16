package mydomain.datanucleus.datatrail.nodes;

import mydomain.datanucleus.datatrail.NodeAction;
import mydomain.datanucleus.datatrail.NodeType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface NodeDefinition {

    /**
     * Type of node implemented by the class
     */
    NodeType type();

    /**
     * Type of action implemented by the class
     * @return
     */
    NodeAction[] action() default {};

    interface Helper{
        /**
         * Determines if the given NodeDefinition supports the given action
         * @param nodeDefinition
         * @param action
         * @return
         */
        static boolean isSupported(final NodeDefinition nodeDefinition, final NodeAction action){
            return Arrays.asList( nodeDefinition.action()).contains(action);
        }
    }
}
