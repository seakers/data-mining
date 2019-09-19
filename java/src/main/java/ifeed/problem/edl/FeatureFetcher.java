package ifeed.problem.edl;

import ifeed.architecture.AbstractArchitecture;
import ifeed.expression.Symbols;
import ifeed.expression.Utils;
import ifeed.feature.AbstractFeatureFetcher;
import ifeed.feature.Feature;
import ifeed.local.params.BaseParams;

import java.util.ArrayList;
import java.util.List;

public class FeatureFetcher extends AbstractFeatureFetcher {

    public FeatureFetcher(BaseParams params){
        super(params, new FilterFetcher(params));
    }

    public FeatureFetcher(BaseParams params, List<AbstractArchitecture> architectures){
        super(params, architectures, new FilterFetcher(params));
    }

    public FeatureFetcher(BaseParams params, List<Feature> baseFeatures, List<AbstractArchitecture> architectures){
        super(params, baseFeatures, architectures, new FilterFetcher(params));
    }

//    @Override
//    public List<String> getNames(String expression){
//        String e = Utils.remove_outer_parentheses(expression);
//
//        // Remove individual feature wrapper
//        if(e.startsWith(Symbols.individual_expression_wrapper_open) && e.endsWith(Symbols.individual_expression_wrapper_close)){
//            e = e.substring(1, e.length() - 1);
//        }
//
//        List<String> names = new ArrayList<>();
//
//        // Single element
//        String[] eSplit = e.split("==");
//
//        names.add(eSplit[0]);
//        return names;
//    }
//
//    @Override
//    public List<String[]> getArgs(String expression){
//        String e = Utils.remove_outer_parentheses(expression);
//
//        // Remove individual feature wrapper
//        if(e.startsWith(Symbols.individual_expression_wrapper_open) && e.endsWith(Symbols.individual_expression_wrapper_close)){
//            e = e.substring(1, e.length() - 1);
//        }
//
//        List<String[]> argSets = new ArrayList<>();
//
//        // Single element
//        String[] eSplit = e.split("==");
//        String[] args = new String[1];
//        args[0] = eSplit[1];
//        argSets.add(args);
//        return argSets;
//    }
}
