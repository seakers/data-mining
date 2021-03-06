package ifeed.local.params;


import ifeed.feature.FeatureMetric;

public class ARMParams extends BaseParams{
	
//    // Thresholds are configured from the web GUI
//    public static double support_threshold = 0;
//    public static double confidence_threshold = 0;
//    public static double lift_threshold = 0;

    // Constrain the number of base features
    public static boolean adjustRuleSize = false;
    
    // Maximum number of iterations for adjusting the number of rules based on a given support threshold
    public static int adjustRuleSizeMaxIter = 30;
    
    // Number of rules required
    public static int minRuleNum = 100;
    public static int maxRuleNum = 1000;

    // Sorting metric
    public static FeatureMetric sortBy = FeatureMetric.PRECISION;

    // Use only inOrbit and notInOrbit
    public static boolean use_only_primitive_features = false;
    
    // Run mRMR
    public static boolean run_mRMR = false;
    public static int max_number_of_features_before_mRMR = 100000000;
    public static int numThreads = 1;
}
