package ifeed.feature;

import ifeed.architecture.AbstractArchitecture;
import ifeed.expression.Fetcher;
import ifeed.filter.AbstractFilter;
import ifeed.filter.AbstractFilterFetcher;
import ifeed.filter.AbstractFilterOperatorFetcher;
import ifeed.local.params.BaseParams;

import java.util.*;

public abstract class AbstractFeatureFetcher extends Fetcher{

    protected BaseParams params;
    protected List<Feature> baseFeatures;
    protected List<AbstractArchitecture> architectures;
    protected AbstractFilterFetcher filterFetcher;
    protected AbstractFilterOperatorFetcher filterOperatorFetcher;
    protected Map<AbstractFilter, Feature> filter2FeatureMap;

    public AbstractFeatureFetcher(BaseParams params, List<AbstractArchitecture> architectures, AbstractFilterFetcher filterFetcher){
        this.params = params;
        this.baseFeatures = new ArrayList<>();
        this.architectures = architectures;
        this.filterFetcher = filterFetcher;
        this.filter2FeatureMap = new HashMap<>();
    }

    public AbstractFeatureFetcher(BaseParams params, List<Feature> baseFeatures, List<AbstractArchitecture> architectures, AbstractFilterFetcher filterFetcher){
        this.params = params;
        this.baseFeatures = baseFeatures;
        this.architectures = architectures;
        this.filterFetcher = filterFetcher;
        this.filter2FeatureMap = new HashMap<>();
    }

    public void setFilterOperatorFetcher(AbstractFilterOperatorFetcher fetcher){ this.filterOperatorFetcher = fetcher; }

    public AbstractFilterOperatorFetcher getFilterOperatorFetcher() {
        if(this.filterOperatorFetcher == null){
            throw new IllegalStateException("AbstractFilterOperatorFetcher needs to be defined");
        }
        return filterOperatorFetcher;
    }

    public void setBaseFeatures(List<Feature> baseFeatures){ this.baseFeatures = baseFeatures; }

    public List<Feature> getBaseFeatures(){
        return this.baseFeatures;
    }

    public AbstractFilterFetcher getFilterFetcher(){ return this.filterFetcher; }

    public boolean emptyBaseFeature(){
        return this.baseFeatures.isEmpty();
    }

    public boolean emptyArchitectures(){ return this.architectures.isEmpty(); }

    public Feature fetch(String fullExpression){

        Feature match = null;

        // Examples of feature expressions: {name[arguments]}
        try{

            for(Feature feature: this.baseFeatures){
                if(fullExpression.equals(feature.getName())){
                    match = feature;
                    break;
                }
            }

            if(match == null){
                String[] nameAndArgs = super.getNameAndArgs(fullExpression);
                String type = nameAndArgs[0];
                String[] args = Arrays.copyOfRange(nameAndArgs, 1, nameAndArgs.length + 1);

                match = this.fetch(type, args);

                if(match == null){
                    throw new RuntimeException("Feature could not be fetched from: " + fullExpression);
                }

                this.baseFeatures.add(match);
            }

        }catch(Exception e){
            System.out.println("Exc in fetching a feature from an expression: " + fullExpression);
            e.printStackTrace();
            return null;
        }

        return match;
    }

    public Feature fetch(String type, String[] args){

        if(this.architectures.isEmpty() || this.filterFetcher == null){
            throw new RuntimeException("Exc in fetching a filter: architectures not setup");

        }else{
            return fetch(this.filterFetcher.fetch(type, args));
        }
    }

    public Feature fetch(AbstractFilter filter){

        if(this.architectures.isEmpty() || this.filterFetcher == null){
            throw new RuntimeException("Exc in fetching a filter: architectures not setup");

        }else{
            if(filter2FeatureMap.containsKey(filter)){
                return this.filter2FeatureMap.get(filter);

            }else{
                BitSet matches = new BitSet(this.architectures.size());
                for(int i = 0; i < this.architectures.size(); i++){
                    AbstractArchitecture a = this.architectures.get(i);
                    if(filter.apply(a)){
                        matches.set(i);
                    }
                }

                Feature out = new Feature(filter.toString(), matches);
                this.filter2FeatureMap.put(filter, out);

                return out;
            }
        }
    }
}
