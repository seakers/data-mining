package ifeed.mining.moea.operators;

import ifeed.feature.logic.LogicalConnectiveType;
import ifeed.local.params.BaseParams;
import ifeed.mining.moea.MOEABase;

public abstract class AbstractSimplificationOperator extends AbstractLogicOperator {

    public AbstractSimplificationOperator(BaseParams params, MOEABase base){
        super(params, base);
    }

    public AbstractSimplificationOperator(BaseParams params, MOEABase base, LogicalConnectiveType targetLogic){
        super(params, base, targetLogic);
    }
}
