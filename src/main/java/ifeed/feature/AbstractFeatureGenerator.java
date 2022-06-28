package ifeed.feature;

import ifeed.filter.AbstractFilter;
import ifeed.local.params.BaseParams;

import java.util.List;

public abstract class AbstractFeatureGenerator {

    protected BaseParams params;

    public abstract List<AbstractFilter> generateCandidates();

    public AbstractFeatureGenerator(BaseParams params){
        this.params = params;
    }
}
