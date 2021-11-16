package mydomain.datanucleus.datatrail;

/**
 * Replicated class of the ITrailDesc which already exists in adams_ejb
 * The version in adams_ejb should simply be made to extend this interface to limit the invasiveness of this interface
 */
public interface ITrailDesc {
    /**Use for Audit Trail, describing an Object ID.*/
    String minimalTxtDesc();
}
