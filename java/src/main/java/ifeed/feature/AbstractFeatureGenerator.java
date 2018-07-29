package ifeed.feature;

import ifeed.filter.AbstractFilter;
import java.util.List;

public abstract class AbstractFeatureGenerator {

    public abstract List<AbstractFilter> generateCandidates();
}
