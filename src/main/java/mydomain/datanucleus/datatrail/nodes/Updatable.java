package mydomain.datanucleus.datatrail.nodes;

/**
 * Any node which implements this interface can update its fields using existing data already stored in the node.
 * For instance, a refence node can use this method to update the id from the source object after a transaction is committed
 */
public interface Updatable {

    void updateFields();
}
