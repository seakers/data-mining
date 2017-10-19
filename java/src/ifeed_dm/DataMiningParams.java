package ifeed_dm;


public class DataMiningParams {
	
//    // Threshold configured from the web GUI
//    public static double support_threshold = 0;
//    public static double confidence_threshold = 0;
//    public static double lift_threshold = 0;

    
    
    // Maximum number of iterations for adjusting the number of rules
    public static int maxIter = 7;
    // Number of rules required
    public static int minRuleNum = 30;
    public static int maxRuleNum = 500;

    public static boolean tallMatrix = false;

    // Maximum length of features
    public static int maxLength = 2;
    
    public static FeatureMetric metric = FeatureMetric.FCONFIDENCE;
    
    // use only inOrbit and notInOrbit
    public static boolean use_only_primitive_features = false;
    
    // Run mRMR
    public static boolean run_mRMR = false;
    
    
    public static int max_number_of_features_before_mRMR = 1000000;
    public static int numThreads = 2;
    
}
