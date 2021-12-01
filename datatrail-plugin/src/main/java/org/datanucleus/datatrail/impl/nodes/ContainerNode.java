package org.datanucleus.datatrail.impl.nodes;

import org.datanucleus.datatrail.Node;

import java.util.Collection;

public interface ContainerNode extends Node{
    Collection<Node> getAdded();

    Collection<Node> getRemoved();

    Collection<Node> getChanged();

    Collection<Node> getContents();
}