package ifeed.feature.logic;

public interface LogicOperator {

    boolean checkApplicability(Connective root);
    void apply(Connective input);

}
