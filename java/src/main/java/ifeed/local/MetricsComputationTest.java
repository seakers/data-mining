//package ifeed.local;
//
//import ifeed.Utils;
//import ifeed.architecture.AbstractArchitecture;
//import ifeed.feature.logic.Connective;
//import ifeed.io.InputDatasetReader;
//import ifeed.mining.moea.GPMOEABase;
//import ifeed.problem.assigning.GPMOEA;
//
//import java.util.ArrayList;
//import java.util.BitSet;
//import java.util.List;
//
//public class MetricsComputationTest {
//
//
//
//    public static void main(String[] args){
//
//
//        String inputDataFile = "/Users/bang/workspace/daphne/data-mining/data/data.csv";
//        InputDatasetReader reader = new InputDatasetReader(inputDataFile);
//        reader.setInputType(InputDatasetReader.InputType.BINARY_BITSTRING);
//        reader.setColumnInfo(InputDatasetReader.ColumnType.CLASSLABEL,0);
//        reader.setColumnInfo(InputDatasetReader.ColumnType.DECISION, 1);
//        reader.readData();
//
//        List<AbstractArchitecture> architectures = reader.getArchs();
//        BitSet label = reader.getLabel();
//        List<Integer> behavioral = new ArrayList<>();
//        List<Integer> non_behavioral = new ArrayList<>();
//        for(int i = 0; i < architectures.size(); i++){
//            if(label.get(i)){
//                behavioral.add(i);
//            }else{
//                non_behavioral.add(i);
//            }
//        }
//
//        GPMOEABase base = new GPMOEA(architectures, behavioral, non_behavioral);
//
//
//        String expression = "({absent[;5;]}&&{inOrbit[0;0;]})";
//
//        Connective root = base.getFeatureHandler().generateFeatureTree(expression);
//
//        BitSet featureMatches = root.getMatches();
//        double[] metrics = Utils.computeMetricsSetNaNZero(featureMatches, base.getLabels(), base.getSamples().size());
//
//        double coverage = metrics[2];
//        double specificity = metrics[3];
//
//        System.out.println(coverage + ", " + specificity);
//    }
//
//
//}
