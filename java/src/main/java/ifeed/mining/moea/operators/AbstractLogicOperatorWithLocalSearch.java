package ifeed.mining.moea.operators;

import ifeed.feature.logic.LogicalConnectiveType;
import ifeed.local.params.BaseParams;
import ifeed.mining.AbstractLocalSearch;
import ifeed.mining.moea.MOEABase;

public abstract class AbstractLogicOperatorWithLocalSearch extends AbstractLogicOperator{

    protected AbstractLocalSearch localSearch;

    public AbstractLogicOperatorWithLocalSearch(BaseParams params, MOEABase base, AbstractLocalSearch localSearch){
        super(params, base);
        this.localSearch = localSearch;
    }

    public AbstractLogicOperatorWithLocalSearch(BaseParams params, MOEABase base, AbstractLocalSearch localSearch, LogicalConnectiveType targetLogic){
        super(params, base, targetLogic);
        this.localSearch = localSearch;
    }
}
