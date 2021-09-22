package mydomain.datatrail;

import org.datanucleus.enhancement.Persistable;
import org.datanucleus.state.LifeCycleState;
import org.datanucleus.state.ObjectProvider;

import javax.jdo.PersistenceManager;
import java.time.Instant;
import java.util.Arrays;

public class Entity {
    enum Action{
        CREATE,
        UPDATE,
        DELETE
    }

    protected Action action;
    protected String classname;
    protected String id;
    protected String version;
    protected Field[] fields;
    protected String username;
    protected Instant dateModified;


    public Entity(Persistable pc) {
        PersistenceManager pm = (PersistenceManager)pc.dnGetExecutionContext().getOwner();
        ObjectProvider op = (ObjectProvider)pc.dnGetStateManager();
        setAction( op.getLifecycleState() );

        this.classname = pc.getClass().getName();
        this.version = pc.dnGetVersion() != null ? pc.dnGetVersion().toString() : null;
        this.dateModified = Instant.now();

        setFields( pc );
    }


    /**
     * Sets the action based on the LifeCycleState of the persistable object
     * @param lc
     */
    private void setAction(LifeCycleState lc){
        // set the action
        if( lc.isNew() ){
            action = Action.CREATE;
        } else if (lc.isDeleted()){
            action = Action.DELETE;
        } else if( lc.isDirty()){
            action = Action.UPDATE;
        }
    }


    /**
     * Identifies which fields need to be set
     * @param pc
     */
    private void setFields( Persistable pc){

    }


    @Override
    public String toString() {
        return "Object{" +
                "action=" + action +
                ", classname='" + classname + '\'' +
                ", id=" + id +
                ", version='" + version + '\'' +
                ", fields=" + Arrays.toString(fields) +
                ", username='" + username + '\'' +
                ", dateModified=" + dateModified +
                '}';
    }
}
