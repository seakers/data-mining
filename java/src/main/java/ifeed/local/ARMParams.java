package ifeed.local;


import ifeed.feature.FeatureMetric;

public class ARMParams {
	
//    // Thresholds are configured from the web GUI
//    public static double support_threshold = 0;
//    public static double confidence_threshold = 0;
//    public static double lift_threshold = 0;

    // Constrain the number of base features
    public static boolean hardLimitRuleNum = true;
    
    // Maximum number of iterations for adjusting the number of rules based on a given support threshold
    public static int maxIter = 30;
    
    // Number of rules required
    public static int minRuleNum = 30;
    public static int maxRuleNum = 1500;
    
    // Maximum length of features
    public static int maxLength = 2;
    
    // Sorting metric
    public static FeatureMetric sortBy = FeatureMetric.FCONFIDENCE;

    // Use only inOrbit and notInOrbit
    public static boolean use_only_primitive_features = false;
    
    // Run mRMR
    public static boolean run_mRMR = false;
    public static int max_number_of_features_before_mRMR = 100000000;
    public static int numThreads = 1;
}
