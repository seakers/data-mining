package ifeed.mining.moea.operators;

import ifeed.feature.logic.LogicalConnectiveType;
import ifeed.local.params.BaseParams;
import ifeed.mining.moea.GPMOEABase;

public abstract class AbstractSimplificationOperator extends AbstractLogicOperator {

    public AbstractSimplificationOperator(BaseParams params, GPMOEABase base){
        super(params, base);
    }

    public AbstractSimplificationOperator(BaseParams params, GPMOEABase base, LogicalConnectiveType targetLogic){
        super(params, base, targetLogic);
    }
}
