

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ifeed_dm;


import java.util.ArrayList;
//import weka.gui.treevisualizer.PlaceNode2;
//import weka.gui.treevisualizer.TreeVisualizer;
//import weka.core.converters.ConverterUtils.DataSink;
//import weka.core.converters.CSVSaver;
//import java.io.File;
//import java.awt.BorderLayout;



//import rbsa.eoss.Apriori.Feature;
import javaInterface.Architecture;

//import weka.classifiers.trees.J48;
//import weka.core.Attribute;
//import weka.core.FastVector;
//import weka.core.Instance;
//import weka.core.Instances;

/**
 *
 * @author Bang
 */
public class DrivingFeaturesGenerator {
	

    private ArrayList<Integer> behavioral;
    private ArrayList<Integer> non_behavioral;
    private ArrayList<Integer> population;
    private int[] labels;
    
    private ArrayList<DrivingFeature> drivingFeatures;
    private ArrayList<String> userDefFeatures;
    
    private double[][] dataFeatureMat;
    private int[][] dataFeatureMatInt;
    private ArrayList<int[]> drivingFeatures_satList;
    
    private double adaptSupp;
    private double[] thresholds;
    
    private double ninstr;
    private double norb;
    private boolean apriori;

    
    private ArrayList<javaInterface.Architecture> all_archs;
    private FilterExpressionHandler feh;
    
    
    private int maxIter;
    private int minRuleNum;
    private int maxRuleNum;
    

    public DrivingFeaturesGenerator(){
    }
    
    
    public void initialize(ArrayList<Integer> behavioral, ArrayList<Integer> non_behavioral, ArrayList<Architecture> archs,
    					double supp, double conf, double lift){
       
    	this.thresholds = new double[3];
    	thresholds[0] = supp;
    	thresholds[1] = lift;
    	thresholds[2] = conf;
      
        // Store id's of behavioral and non-behavioral architectures
    	this.behavioral = behavioral;
    	this.non_behavioral = non_behavioral;
    	this.population = new ArrayList<>();
    	this.population.addAll(behavioral);
    	this.population.addAll(non_behavioral);
    	
        // Adaptive support threshold
        this.adaptSupp= (double) behavioral.size() / population.size() * 0.5 ;  
        // Number of rules required
        minRuleNum = 30;
        maxRuleNum = 600;
        // Maximum iteration for adjusting the number of rules
        maxIter = 7;
		
        this.all_archs = archs;
        
        // The number of instruments and orbits: hard-coded. Needs improvement!
        this.ninstr = 12;
        this.norb = 5;      
        //this.ninstr = Params.instrument_list.length;
        //this.norb = Params.orbit_list.length;



        userDefFeatures = new ArrayList<>();
        drivingFeatures = new ArrayList<>();
        drivingFeatures_satList = new ArrayList<>();
        feh = new FilterExpressionHandler();
        feh.setArchs(archs,behavioral,non_behavioral,population);      
      
        // Apriori turned off
        this.apriori=false;
  }    
    
    
    
    private double[] computeMetrics(ArrayList<Integer> matchedArchIDs){
        
        if (matchedArchIDs.isEmpty()){
            double[] metrics = {0,0,0,0};
            return metrics;
        }

        double cnt_all= (double) non_behavioral.size() + behavioral.size();
        double cnt_F=0.0;
        double cnt_S= (double) behavioral.size();
        double cnt_SF=0.0;

        // Need to count cnt_SF and cnt_F
        for(int id:matchedArchIDs){
            cnt_F++;
            if(behavioral.contains(id)){
                cnt_SF++;
            }
        }
        double cnt_NS = cnt_all-cnt_S;
        double cnt_NF = cnt_all-cnt_F;
        double cnt_S_NF = cnt_S-cnt_SF;
        double cnt_F_NS = cnt_F-cnt_SF;
        
    	double[] metrics = new double[4];
    	
        double support = cnt_SF/cnt_all;
        double support_F = cnt_F/cnt_all;
        double support_S = cnt_S/cnt_all;
        
        double lift=0;
        double conf_given_F=0;
        if(cnt_F!=0){
            lift = (cnt_SF/cnt_S) / (cnt_F/cnt_all);
            conf_given_F = (cnt_SF)/(cnt_F);   // confidence (feature -> selection)
        }
        double conf_given_S = (cnt_SF)/(cnt_S);   // confidence (selection -> feature)

    	metrics[0] = support;
    	metrics[1] = lift;
    	metrics[2] = conf_given_F;
    	metrics[3] = conf_given_S;
    	
    	return metrics;
    }  

    

    public ArrayList<DrivingFeature> getPrimitiveDrivingFeatures(){
        
    	

    	this.drivingFeatures = new ArrayList<>();
    	this.drivingFeatures_satList = new ArrayList<>();
    	ArrayList<String> candidate_features = new ArrayList<>();
    	
    	
        // Input variables
        // present, absent, inOrbit, notInOrbit, together2, 
        // separate2, separate3, together3, emptyOrbit
        // numOrbits, numOfInstruments, subsetOfInstruments
        
        // Preset filter expression example:
        // {presetName[orbits;instruments;numbers]}    
                    
        for(int i=0;i<ninstr;i++){
            // present, absent
            candidate_features.add("{present[;"+i+";]}");
            candidate_features.add("{absent[;"+i+";]}");
            
            for(int j=0;j<norb+1;j++){
                // numOfInstruments (number of specified instruments across all orbits)
                candidate_features.add("{numOfInstruments[;"+i+";"+j+"]}");
            }                
            
            for(int j=0;j<i;j++){
                // together2, separate2
                candidate_features.add("{together[;"+i+","+j+";]}");
                candidate_features.add("{separate[;"+i+","+j+";]}");
                for(int k=0;k<j;k++){
                    // together3, separate3
                    candidate_features.add("{together[;"+i+","+j+","+k+";]}");
                    candidate_features.add("{separate[;"+i+","+j+","+k+";]}");
                }
            }
        }
        for(int i=0;i<norb;i++){
            for(int j=1;j<9;j++){
                // numOfInstruments (number of instruments in a given orbit)
                candidate_features.add("{numOfInstruments["+i+";;"+j+"]}");
            }
            // emptyOrbit
            candidate_features.add("{emptyOrbit["+i+";;]}");
            // numOrbits
            int numOrbitsTemp = i+1;
            candidate_features.add("{numOrbits[;;"+numOrbitsTemp+"]}");
            for(int j=0;j<ninstr;j++){
                // inOrbit, notInOrbit
                candidate_features.add("{inOrbit["+i+";"+j+";]}");
                candidate_features.add("{notInOrbit["+i+";"+j+";]}");
                for(int k=0;k<j;k++){
                    // togetherInOrbit2
                    candidate_features.add("{inOrbit["+i+";"+j+","+k+";]}");
                    for(int l=0;l<k;l++){
                        // togetherInOrbit3
                        candidate_features.add("{inOrbit["+i+";"+j+","+k+","+l+";]}");
                    }
                }
            }
        }
        for(int i=0;i<16;i++){
            // numOfInstruments (across all orbits)
            candidate_features.add("{numOfInstruments[;;"+i+"]}");
        }
        
        
        
        try{
        
        ArrayList<String> featureData_name = new ArrayList<>();
        ArrayList<String> featureData_exp = new ArrayList<>();
        ArrayList<double[]> featureData_metrics = new ArrayList<>();
        ArrayList<int[]> featureData_satList = new ArrayList<>();
        
        for(String feature:candidate_features){ 
            String feature_expression_inside = feature.substring(1,feature.length()-1);
            String name = feature_expression_inside.split("\\[")[0];
            double[] metrics = feh.processSingleFilterExpression_computeMetrics(feature_expression_inside);
            featureData_satList.add(feh.getSatisfactionArray());
            featureData_name.add(name);
            featureData_exp.add(feature);
            featureData_metrics.add(metrics);
        }


		// Add the user-defined features
		if(!this.userDefFeatures.isEmpty()){
		    for(String exp:this.userDefFeatures){
		        if(exp.isEmpty()){
		            continue;
		        }
		        ArrayList<Integer> matchedArchIDs = feh.processFilterExpression(exp);		        
		        double[] metrics = this.computeMetrics(matchedArchIDs);
		        featureData_satList.add(feh.getSatisfactionArray());
		        featureData_name.add(exp);
		        featureData_exp.add(exp);
		        featureData_metrics.add(metrics);           
		    }
	    }
      

        int iter=0;
        ArrayList<Integer> addedFeatureIndices = new ArrayList<>();
        double[] bounds = new double[2];
        bounds[0] = 0;
        bounds[1] = (double) behavioral.size() / population.size();

        if(apriori){
            while(addedFeatureIndices.size() < minRuleNum || addedFeatureIndices.size() > maxRuleNum){

                iter++;
                if(iter > maxIter){
                        break;
                }else if(iter > 1){
                        // max supp threshold is support_S
                        // min supp threshold is 0
                        double a;
                                if(addedFeatureIndices.size() > maxRuleNum){ // Too many rules -> increase threshold
                                        bounds[0] = this.adaptSupp;
                                        a = bounds[1];
                                }else{ // too few rules -> decrease threshold
                                        bounds[1] = this.adaptSupp;
                                        a = bounds[0];
                                }
                        // Bisection
                        this.adaptSupp = (double) (this.adaptSupp + a) * 0.5;	
                }
                addedFeatureIndices = new ArrayList<>();
                    for(int i=0;i<featureData_name.size();i++){
                    double[] metrics = featureData_metrics.get(i);
                    if(metrics[0]>adaptSupp){
                            addedFeatureIndices.add(i);
                        if(addedFeatureIndices.size() > this.maxRuleNum && iter < maxIter){
                            break;
                        }else if(( candidate_features.size() - (i+1) ) + addedFeatureIndices.size() < this.minRuleNum){
                            break;
                        }
                    }
                }        	
                System.out.println("RuleSetSize: " + addedFeatureIndices.size() +" Treshold: "+ this.adaptSupp);
            }		
            System.out.println("Driving features extracted in "+ iter +" steps with size: " + addedFeatureIndices.size());
        }else{
            for(int i=0;i<featureData_name.size();i++){
                double[] metrics = featureData_metrics.get(i);
                if(metrics[0]>this.thresholds[0]&&metrics[1]>thresholds[1]&&metrics[2]>thresholds[2]&&metrics[3]>thresholds[2]){
                    addedFeatureIndices.add(i);
                }
            }
        }
		

        int id=0;
    	for(int i:addedFeatureIndices){
    		this.drivingFeatures.add(new DrivingFeature(id,featureData_name.get(i), featureData_exp.get(i), featureData_metrics.get(i)));
    		this.drivingFeatures_satList.add(featureData_satList.get(i));
    		id++;
    	}


        if(apriori) return getDrivingFeatures();
        else return drivingFeatures;
 

        }catch(Exception e){
        	e.printStackTrace();
        	return new ArrayList<>();
        }
    }
    
    
    
   public String buildClassificationTree(){
	   setDrivingFeatureSatisfactionData();
       ClassificationTreeBuilder ctb = new ClassificationTreeBuilder(dataFeatureMatInt,labels,drivingFeatures);
       //ctb.setDrivingFeatures(drivingFeatures);
       ctb.buildTree();
       String graph = ctb.printTree_json();
       return graph;
   }
   
   
   
   public void setDrivingFeatureSatisfactionData(){
	   
       // Get feature satisfaction matrix
       this.dataFeatureMat = new double[population.size()][drivingFeatures.size()];
       this.dataFeatureMatInt = new int[population.size()][drivingFeatures.size()];
       this.labels = new int[population.size()];
       
       for(int i=0;i<population.size();i++){
       	for(int j=0;j<drivingFeatures.size();j++){
       		
       		DrivingFeature df = drivingFeatures.get(j);
       		int index = df.getID();
   			this.dataFeatureMat[i][j] = (double) drivingFeatures_satList.get(index)[i];
   			dataFeatureMatInt[i][j] = drivingFeatures_satList.get(index)[i];
       	}
       	
       	if(behavioral.contains(population.get(i))){
       		labels[i]=1;
       	}else{
       		labels[i]=0;
       	}
       	
       }         
   }
   
   
    

    
    public ArrayList<DrivingFeature> getDrivingFeatures(){

//    	this.setDrivingFeatureSatisfactionData();
//    	
//    	System.out.println("higher level feature extraced");
//    	ArrayList<DrivingFeature> dfs=new ArrayList<>();
//    	
//    	int[] label_int = satisfactionArray(behavioral,population); 
//    	double[] label = new double[label_int.length];
//    	for(int i=0;i<label_int.length;i++){
//    		label[i] = (double) label_int[i];
//    	}
//
//        // Create a new instance of Apriori
//        Apriori ap = new Apriori(drivingFeatures, this.dataFeatureMat, label, thresholds);
//        
//        // Run Apriori algorithm
//        ArrayList<Apriori.Feature> new_features = ap.runApriori(2,false,100);
//
//        // Create a new list of driving features (assign new IDs)
//        int id=0;
//        for(int f=0;f<new_features.size();f++){
//            
//            Apriori.Feature feat = new_features.get(f);
//            String expression="";
//            String name="";
//            ArrayList<Integer> featureIndices = feat.getElements();
//            
//            int[] indices_array = new int[featureIndices.size()];
//            
//            for(int i=0;i<featureIndices.size();i++){
//                indices_array[i] = featureIndices.get(i);
//            }
//
//            boolean first = true;
//            for(int index:featureIndices){
//                if(first){
//                    first = false;
//                }
//                else{
//                    expression = expression + "&&";
//                    name = name + "&&";
//                }
//                DrivingFeature thisDF = this.drivingFeatures.get(index);
//                expression = expression + thisDF.getExpression();
//                name = name + thisDF.getName();
//            }
//            double[] metrics = feat.getMetrics();
//            DrivingFeature df = new DrivingFeature(id,name,expression, metrics, false);
//            id++;
//            dfs.add(df);
//        }
        
//        // Define the new feature satisfaction matrix       
//        DoubleMatrix prev_sat_matrix = new DoubleMatrix(this.drivingFeaturesMatrix);
//        DoubleMatrix new_sat_matrix = prev_sat_matrix.mmul(mapping_old_and_new_feature_indices);        
//        
//        DoubleMatrix newDrivingFeaturesMatrix = DoubleMatrix.zeros(new_sat_matrix.getRows(), new_sat_matrix.getColumns());
//        for(int i=0;i<new_sat_matrix.getColumns();i++){
//            DoubleMatrix col = new_sat_matrix.getColumn(i);
//            col = col.eq(save_feature_length[i]);
//            newDrivingFeaturesMatrix.putColumn(i, col);
//        }
        
//        this.drivingFeaturesMatrix = newDrivingFeaturesMatrix.toArray2();
//        this.drivingFeatures = dfs;
    	
    	return this.drivingFeatures;
    }
    
    
    
    
    
    
    public int[][] booleanToInt(boolean[][] b) {
        int[][] intVector = new int[b.length][b[0].length]; 
        for(int i = 0; i < b.length; i++){
            for(int j = 0; j < b[0].length; ++j) intVector[i][j] = b[i][j] ? 1 : 0;
        }
        return intVector;
    }
    


    private int[] satisfactionArray(ArrayList<Integer> matchedArchIDs, ArrayList<Integer> allArchIDs){
        int[] satArray = new int[allArchIDs.size()];
        for(int i=0;i<allArchIDs.size();i++){
            int id = allArchIDs.get(i);
            if(matchedArchIDs.contains(id)){
                satArray[i]=1;
            }else{
                satArray[i]=0;
            }
        }
        return satArray;
    }    
    
    
    public void turn_on_apriori(){
    	this.apriori=true;
    }
    
    
    public void addUserDefFeature(String expression){
    	this.userDefFeatures.add(expression);
    }
    
    

}
