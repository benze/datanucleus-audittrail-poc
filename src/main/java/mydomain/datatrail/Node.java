package mydomain.datatrail;

abstract public class Node {
    protected enum Type{
        ENTITY,
        FIELD
    }

    protected Node parent;
    protected Type nodeType;


    public Node getParent() {
        return parent;
    }

    public Type getNodeType() {
        return nodeType;
    }
}
