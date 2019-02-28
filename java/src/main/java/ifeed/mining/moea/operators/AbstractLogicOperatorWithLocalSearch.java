package ifeed.mining.moea.operators;

import ifeed.feature.logic.LogicalConnectiveType;
import ifeed.local.params.BaseParams;
import ifeed.mining.AbstractLocalSearch;

public abstract class AbstractLogicOperatorWithLocalSearch extends AbstractLogicOperator{

    protected AbstractLocalSearch localSearch;

    public AbstractLogicOperatorWithLocalSearch(BaseParams params, AbstractLocalSearch localSearch){
        super(params, localSearch.getFilterFetcher());
        this.localSearch = localSearch;
    }

    public AbstractLogicOperatorWithLocalSearch(BaseParams params, AbstractLocalSearch localSearch, LogicalConnectiveType targetLogic){
        super(params, localSearch.getFilterFetcher(), targetLogic);
        this.localSearch = localSearch;
    }
}
