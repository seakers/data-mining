package ifeed.feature;

import ifeed.feature.logic.Connective;
import ifeed.filter.AbstractFilterFetcher;
import ifeed.local.params.BaseParams;

public abstract class AbstractFeatureSimplifier {

    protected BaseParams params;
    protected FeatureExpressionHandler expressionHandler;
    protected AbstractFeatureFetcher featureFetcher;
    protected AbstractFilterFetcher filterFetcher;

    public AbstractFeatureSimplifier(BaseParams params, AbstractFeatureFetcher featureFetcher, AbstractFilterFetcher filterFetcher){
        this.featureFetcher = featureFetcher;
        this.filterFetcher = filterFetcher;
        this.params = params;
    }

    public abstract boolean simplify(Connective root);
}
