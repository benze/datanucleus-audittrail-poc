package org.datanucleus.datatrail;

import org.datanucleus.datatrail.impl.NodeAction;
import org.datanucleus.datatrail.impl.NodeType;

public interface Node {
    /**
     * returns the type of node represented by this object
     * @return
     */
    NodeType getType();

    String getName();

    String getClassName();

    Object getValue();

    Object getPrev();

    NodeAction getAction();
}