package ifeed.feature.logic;

import java.util.BitSet;

public interface LocalSearchTester {

    /**
     * Sets the current node to add a new literal. (no new branch created)
     */
    void setAddNewNode();

    void cancelAddNode();

    Formula getNewNode();

    boolean getAddNewNode();

    void setNewNode(Formula node);

    void setNewNode(String name, BitSet matches);

    void finalizeNewNodeAddition();

}
