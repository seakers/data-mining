//package ifeed.mining.arm;
//
//import ifeed.feature.Feature;
//import weka.associations.Apriori;
//import weka.core.Instances;
//import weka.core.converters.ConverterUtils.DataSource;
//
//import java.io.File;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.BitSet;
//import java.util.Date;
//import java.util.List;
//
//public class WekaApriori {
//
//    /**
//     * The features given to the Apriori algorithm
//     *
//     */
//    private final ArrayList<Feature> baseFeatures;
//
//    /**
//     * The number of observations in the data
//     */
//    private final int numberOfObservations;
//
//    /**
//     * The threshold for support
//     */
//    private double supportThreshold;
//
//    private BitSet labels;
//    private String path;
//    private String filename;
//
//    public WekaApriori(int numberOfObservations, List<Feature> features, BitSet labels) {
//        this.numberOfObservations = numberOfObservations;
//        this.baseFeatures = new ArrayList<>(features);
//        this.labels = labels;
//        this.path =  System.getProperty("user.dir");
//        this.filename = this.path + File.separator + "temp" + File.separator + new SimpleDateFormat("yyyy-MM-dd-HH-mm").format(new Date());
//    }
//
//    public void writeCSVFile(){
//
//    }
//
//
//    public void temp(){
//
//        // load data
//        Instances data = DataSource.read();
//        data.setClassIndex(data.numAttributes() - 1);
//
//        try{
//            // build associator
//            Apriori apriori = new Apriori();
//            apriori.setClassIndex(data.classIndex());
//            apriori.buildAssociations(data);
//
//            // output associator
//            System.out.println(apriori);
//
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//    }
//
//
//}
