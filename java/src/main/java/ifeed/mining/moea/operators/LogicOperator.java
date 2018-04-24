package ifeed.mining.moea.operators;

import ifeed.filter.Filter;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.Literal;
import ifeed.feature.logic.LogicalConnectiveType;

import java.util.ArrayList;
import java.util.List;

public abstract class LogicOperator {

    public abstract boolean checkApplicability(Connective root);

    public abstract void findApplicableNodesUnderGivenParentNode(Connective root, List<Literal> nodes, List<Filter> filters);

    public Connective getParentNodeOfApplicableNodes(Connective root, LogicalConnectiveType targetLogic){
        // Return the node whose child literals satisfy the condition needed to apply the current operator (uses depth-first search)

        // Inspect the literals only if the logical connective matches the target
        if(root.getLogic() == targetLogic){

            List<Literal> nodes = new ArrayList<>();
            List<Filter> filters = new ArrayList<>();

            // Check if there exist applicable nodes. When applicable nodes are found, nodes and filters are filled in as side effects
            this.findApplicableNodesUnderGivenParentNode(root, nodes, filters);

            if(!nodes.isEmpty()){ // Applicable nodes are found under the current node
                return root;
            }
        }

        for(Connective branch: root.getConnectiveChildren()){
            Connective temp = this.getParentNodeOfApplicableNodes(branch, targetLogic);
            if(temp != null){
                // Applicable node is found in one of the child branches
                return temp;
            }
        }

        return null;
    }


}
