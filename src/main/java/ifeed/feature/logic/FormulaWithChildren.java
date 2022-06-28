package ifeed.feature.logic;

import java.util.List;

public interface FormulaWithChildren{

    void childNodeModified();

    boolean removeNode(Formula node);

    boolean containsNode(Formula node);

    boolean removeNode(Formula node, boolean searchDescendants);

    boolean containsNode(Formula node, boolean searchDescendants);

    List<Connective> getDescendantConnectives();

    List<IfThenStatement> getDescendantIfThenStatements();

    List<Literal> getDescendantLiterals();

    List<Formula> getDescendantNodes();
}
