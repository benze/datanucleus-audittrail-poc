package org.datanucleus.test;

import com.spotify.hamcrest.pojo.IsPojo;
import mydomain.datanucleus.datatrail.Node;
import mydomain.datanucleus.datatrail.NodeAction;
import mydomain.datanucleus.datatrail.NodeType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Version;
import javax.jdo.annotations.VersionStrategy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.iterableWithSize;

public class SubclassTest extends AbstractTest{

    @PersistenceCapable
    @Version(strategy= VersionStrategy.VERSION_NUMBER, column="VERSN")
    abstract static public class AbstractBaseClass{

        AbstractBaseClass myField;

        public AbstractBaseClass getMyField() {
            return myField;
        }

        public void setMyField(AbstractBaseClass myField) {
            this.myField = myField;
        }
    }

    @PersistenceCapable
    @Version(strategy= VersionStrategy.VERSION_NUMBER, column="VERSN")
    static public class ChildClass extends AbstractBaseClass{
        String name = "ChildClass";
    }

    @PersistenceCapable
    @Version(strategy= VersionStrategy.VERSION_NUMBER, column="VERSN")
    static public class ChildClass2 extends AbstractBaseClass{
        String name = "ChildClass2";
    }

    @PersistenceCapable
    @Version(strategy= VersionStrategy.VERSION_NUMBER, column="VERSN")
    static public class ChildSubClass extends ChildClass{
        String name = "ChildSubClass";
    }

    @DisplayName("Should show the abstract class name if no value is defined")
    @Test
    public void checkForAbstractName(){
        executeTx( pm -> {
            ChildClass cc = new ChildClass();
            pm.makePersistent(cc);
        });

        IsPojo<Node> childClass = getEntity(NodeAction.CREATE, ChildClass.class, ANY)
                .withProperty("fields",  hasItems(
                        getField(NodeType.REF, AbstractBaseClass.class, "myField", null, null ),
                        getField(NodeType.PRIMITIVE, String.class, "name", "ChildClass", null )
                ))
                ;
        assertThat(audit.getModifications(), contains(childClass));
    }


    @DisplayName("Should show the child class name if the child is defined as the ref obj")
    @Test
    public void checkForChildName(){
        executeTx( pm -> {
            ChildClass cc = new ChildClass();
            cc.setMyField(cc);
            pm.makePersistent(cc);
        });

        IsPojo<Node> childClass = getEntity(NodeAction.CREATE, ChildClass.class, ANY)
                .withProperty("fields",  hasItems(
                        getField(NodeType.REF, ChildClass.class, "myField", "1", null ),
                        getField(NodeType.PRIMITIVE, String.class, "name", "ChildClass", null )
                ))
                ;
        assertThat(audit.getModifications(), contains(childClass));
    }


    @DisplayName("Should show the child sub class name if the child is defined as the ref obj")
    @Test
    public void checkForSubChildName(){
        executeTx( pm -> {
            ChildClass cc = new ChildSubClass();
            cc.setMyField(cc);
            pm.makePersistent(cc);
        });

        IsPojo<Node> childClass = getEntity(NodeAction.CREATE, ChildSubClass.class, ANY)
                .withProperty("fields",  hasItems(
                        getField(NodeType.REF, ChildSubClass.class, "myField", "1", null ),
                        getField(NodeType.PRIMITIVE, String.class, "name", "ChildSubClass", null )
                ))
                ;
        assertThat(audit.getModifications(), contains(childClass));
    }

    @DisplayName("Should show the child sub class name if the sub child is defined as the ref obj in the parent class")
    @Test
    public void checkForChildClassButChildSubclassName(){
        executeTx( pm -> {
            ChildClass cc = new ChildClass();
            cc.setMyField(new ChildSubClass());
            pm.makePersistent(cc);
        });

        IsPojo<Node> childClass = getEntity(NodeAction.CREATE, ChildClass.class, ANY)
                .withProperty("fields",  hasItems(
                        getField(NodeType.REF, ChildSubClass.class, "myField", "1", null ),
                        getField(NodeType.PRIMITIVE, String.class, "name", "ChildClass", null )
                ));

        IsPojo<Node> childSubClass = getEntity(NodeAction.CREATE, ChildSubClass.class, ANY)
                .withProperty("fields",  hasItems(
                        getField(NodeType.PRIMITIVE, String.class, "name", "ChildSubClass", null ),
                        getField(NodeType.PRIMITIVE, String.class, "name", "ChildClass", null )
                ));


        assertThat(audit.getModifications(), containsInAnyOrder(childClass, childSubClass));
    }

}
