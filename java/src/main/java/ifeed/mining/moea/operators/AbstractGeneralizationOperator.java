package ifeed.mining.moea.operators;

import ifeed.feature.logic.LogicalConnectiveType;
import ifeed.local.params.BaseParams;
import ifeed.mining.moea.MOEABase;


public abstract class AbstractGeneralizationOperator extends AbstractLogicOperator {

    public AbstractGeneralizationOperator(BaseParams params, MOEABase base){
        super(params, base);
    }

    public AbstractGeneralizationOperator(BaseParams params, MOEABase base, LogicalConnectiveType targetLogic){
        super(params, base, targetLogic);
    }
}
