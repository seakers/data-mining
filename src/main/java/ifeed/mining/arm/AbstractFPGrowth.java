package ifeed.mining.arm;

import ifeed.architecture.AbstractArchitecture;
import ifeed.feature.Feature;
import ifeed.feature.FeatureMetric;
import ifeed.feature.FeatureMetricComparator;
import ifeed.io.ARMFeatureIO;
import ifeed.local.params.BaseParams;
import org.moeaframework.util.TypedProperties;
import java.util.*;
import java.util.concurrent.Callable;

public abstract class AbstractFPGrowth extends AbstractAssociationRuleMining implements Callable<AbstractFPGrowth>{

    /**
     * The base features that are combined to create the Hasse diagram in the
     * AbstractApriori algorithm. Each BitSet corresponds to a feature and contains the
     * binary vector of the observations that match the feature
     *
     */

    /**
     * The features given to the AbstractApriori algorithm
     *
     */
    private List<Feature> baseFeatures;

    private int maxFeatureLength;

    /**
     * The number of observations in the data
     */
    private final int numberOfObservations;

    private final int numberOfBehavioralObservations;

    private FeatureMetricComparator featureComparator;

    /**
     * The threshold for support
     */
    private double supportThreshold;

    private ARMFeatureIO featureIO;
    private String filename;
    private int numRulesToSaveInFile = 20000; // 20k

    private FPTreeNode root;
    private HashMap<Integer, FPTreeNode> headerTable;

    private int numFeatureCount;
    private int numMetricCalculation;

    private int runIndex;
    private int numOfRuns;
    private int saveDataIndex;

    public AbstractFPGrowth(BaseParams params,
                            int maxFeatureLength,
                            List<AbstractArchitecture> architectures,
                            List<Integer> behavioral,
                            List<Integer> non_behavioral,
                            double supp, double conf, double lift){

        super(params, architectures, behavioral, non_behavioral, supp, conf, lift);

        this.maxFeatureLength = maxFeatureLength;

        this.supportThreshold = supp;
        this.baseFeatures = super.generateBaseFeatures();

        // First filter out features that have support lower than the threshold
        List<Feature> temp = new ArrayList<>();
        for(Feature feature: this.baseFeatures){
            if(feature.getSupport() >= this.supportThreshold){
                temp.add(feature);
            }
        }
        this.baseFeatures = temp;

        // Sort features
        featureComparator = new FeatureMetricComparator(FeatureMetric.SUPPORT);
        Collections.sort(this.baseFeatures, featureComparator);
        Collections.reverse(this.baseFeatures);

        this.numberOfObservations = this.architectures.size();
        this.numberOfBehavioralObservations = super.labels.cardinality();

        this.featureIO = null;
        this.filename = null;

        numFeatureCount = 0;
        numMetricCalculation = 0;

        saveDataIndex = 0;
        numOfRuns = -1;
        runIndex = -1;
    }

    @Override
    public AbstractFPGrowth call(){

        if(this.numOfRuns < 0){
            throw new IllegalStateException("Run index should be set using setRunIndex() method.");
        }

        int numBaseFeatures = this.baseFeatures.size();
        int temp = numBaseFeatures / numOfRuns;
        int start = numBaseFeatures - (temp * runIndex);
        int end = numBaseFeatures - (temp * (runIndex + 1));

        long t0 = System.currentTimeMillis();
        System.out.println("Association rule mining - FP-growth algorithm");
        System.out.println("...["+ this.getClass().getSimpleName() + "] supp: " + support_threshold +
                ", conf: " + confidence_threshold + ", lift: " + lift_threshold + "");

        System.out.println("...[" + this.getClass().getSimpleName() + "] The number of candidate features: " + baseFeatures.size());

        this.root = new FPTreeNode();
        this.headerTable = new HashMap<>();

        buildFPTree(root, headerTable);
        System.out.println("Finished building FP Tree (depth: " +
                getDepthOfTree(this.root, 0) + ", width: " +
                getWidthOfTree(this.headerTable) + ")");

        BitSet matches = new BitSet(numberOfObservations);
        matches.set(0, numberOfObservations);
        mine(start, end, headerTable, new ArrayList<>(), matches);

        long t1 = System.currentTimeMillis();
        System.out.println("...["+ this.getClass().getSimpleName() +"] Total data mining time : " + String.valueOf(t1 - t0) + " msec");
        return this;
    }

    @Override
    public List<Feature> run(){
        long t0 = System.currentTimeMillis();
        System.out.println("Association rule mining - FP-growth algorithm");
        System.out.println("...["+ this.getClass().getSimpleName() + "] supp: " + support_threshold +
                ", conf: " + confidence_threshold + ", lift: " + lift_threshold + "");

        System.out.println("...[" + this.getClass().getSimpleName() + "] The number of candidate features: " + baseFeatures.size());

        this.root = new FPTreeNode();
        this.headerTable = new HashMap<>();

        buildFPTree(root, headerTable);
        System.out.println("Finished building FP Tree (depth: " +
                getDepthOfTree(this.root, 0) + ", width: " +
                getWidthOfTree(this.headerTable) + ")");

        BitSet matches = new BitSet(numberOfObservations);
        matches.set(0, numberOfObservations);
        List<FPGrowthFeature> fpGrowthFeatures = mine(headerTable, new ArrayList<>(), matches);
        List<Feature> out = exportFeatures(fpGrowthFeatures);

        long t1 = System.currentTimeMillis();
        System.out.println("...["+ this.getClass().getSimpleName() +"] Total data mining time : " + String.valueOf(t1 - t0) + " msec");
        return out;
    }

    public void setRunIndex(int index, int numOfRuns){
        this.runIndex = index;
        this.numOfRuns = numOfRuns;
        this.filename = this.filename + "_" + index;
    }

    public void setSaveData(TypedProperties properties, String filename){
        this.filename = filename;
        this.featureIO = new ARMFeatureIO(params, properties);
    }

    protected void saveData(int index, List<Feature> features){
        featureIO.saveFeaturesCSV(  filename + "_" + index + ".features" , features, true);
    }

    protected void checkParent(FPTreeNode node){
        while(true){
            if(node.getParent() == null && !node.isRoot()){
                throw new IllegalStateException("wow");
            }else if(node.isRoot()){
                break;
            } else{
                node = node.getParent();
            }
        }
    }

    protected void checkParent(HashMap<Integer, FPTreeNode> headerTable){
        for(int key: headerTable.keySet()){
            FPTreeNode node = headerTable.get(key);
            while(true){
                checkParent(node);
                if(node.getNodeLink() == null){
                    break;
                }else{
                    node = node.getNodeLink();
                }
            }
        }
    }

    protected int getWidthOfTree(HashMap<Integer, FPTreeNode> headerTable){
        int maxWidth = -1;
        for(int key: headerTable.keySet()){
            int width = 0;
            FPTreeNode node = headerTable.get(key);
            while(true){
                width += 1;
                if(node.getNodeLink() == null){
                    break;
                }else{
                    node = node.getNodeLink();
                }
            }
            if(width > maxWidth){
                maxWidth = width;
            }
        }
        return maxWidth;
    }

    protected int getDepthOfTree(FPTreeNode node, int depth){
        if(!node.getChildren().isEmpty()){
            int maxDepth = depth;
            for(FPTreeNode child: node.getChildren()){
                int temp = getDepthOfTree(child, depth+1);
                if(temp > maxDepth){
                    maxDepth = temp;
                }
            }
            return maxDepth;
        } else {
            return depth + 1;
        }
    }

    protected int countFeature(int id, HashMap<Integer, FPTreeNode> headerTable){
        if(!headerTable.containsKey(id)){
            return 0;
        }
        int count = 0;
        FPTreeNode node = headerTable.get(id);
        while(true){
            count += node.getCount();

            if(node.getNodeLink() == null){
                break;
            }else{
                node = node.getNodeLink();
            }
        }
        return count;
    }

    protected Map<Integer, Integer> countNodeOccurrence(List<List<FPTreeNode>> conditionalPatternBase){
        Map<Integer, Integer> counter = new HashMap<>();
        for(int i = 0; i < conditionalPatternBase.size(); i++){
            List<FPTreeNode> path = conditionalPatternBase.get(i);
            for(int j = 0; j < path.size(); j++){
                int id = path.get(j).getFeatureID();
                if(counter.containsKey(id)){
                    counter.put(id, counter.get(id)+1);
                }else{
                    counter.put(id, 1);
                }
            }
        }
        return counter;
    }

    protected List<FPGrowthFeature> mine(HashMap<Integer, FPTreeNode> headerTable, List<Integer> conditionedOn, BitSet featureMatches){
        return mine(this.baseFeatures.size(), 0, headerTable, conditionedOn, featureMatches);
    }

    protected List<FPGrowthFeature> mine(int start, int end, HashMap<Integer, FPTreeNode> headerTable, List<Integer> conditionedOn, BitSet featureMatches){

        List<FPGrowthFeature> minedFeatures = new ArrayList<>();
        List<List<Integer>> frequentItemSets = new ArrayList<>();
        List<Integer> frequentItems = new ArrayList<>();
        List<BitSet> frequentItemSetMatches = new ArrayList<>();

        if(conditionedOn.size() == maxFeatureLength){
            return minedFeatures;
        }

        for(int id: headerTable.keySet()){

            numFeatureCount++;
            int count = countFeature(id, headerTable);
            double support = (double)  count / numberOfObservations;

            if( support >= this.supportThreshold){
                List<Integer> temp = new ArrayList<>(conditionedOn);
                temp.add(id);
                frequentItemSets.add(temp);
                frequentItems.add(id);

                BitSet featureMatchesCopy = (BitSet) featureMatches.clone();
                featureMatchesCopy.and(this.baseFeatures.get(id).getMatches());
                frequentItemSetMatches.add(featureMatchesCopy);

                numMetricCalculation++;
                double[] metrics = computeMetrics(featureMatchesCopy, super.labels);
                double confidence = metrics[2];

                if(confidence >= this.confidence_threshold){
                    FPGrowthFeature feature = new FPGrowthFeature(temp, null, metrics[0], metrics[1], metrics[2], metrics[3]);
                    minedFeatures.add(feature);
                }
            }
        }

        long t0 = System.currentTimeMillis();
        long t1;

        List<FPGrowthFeature> minedFeaturesNextLevel = new ArrayList<>();
        for(int i = frequentItems.size()-1; i >= 0; i--){

            /////////////////////////////////////
            if(conditionedOn.size() == 0){
                if( i > start){
                    continue;
                }else if(i < end){
                    continue;
                }
            }
            /////////////////////////////////////

            int featureID = frequentItems.get(i);
            List<Integer> itemSet = frequentItemSets.get(i);
            BitSet itemSetMatches = frequentItemSetMatches.get(i);

            List<List<FPTreeNode>> conditionalPatternBase = getConditionalPatternBase(headerTable, featureID);

            // Skip if the conditional pattern base is empty
            if(conditionalPatternBase.isEmpty()){
                continue;
            }

            // Count the number of node occurrences in order to build conditional FP-tree
            Map<Integer, Integer> nodeOccurrenceCounter = countNodeOccurrence(conditionalPatternBase);
            NodeOccurrenceComparator nodeOccurrenceComparator = new NodeOccurrenceComparator(nodeOccurrenceCounter);

            // Sort nodes in the frequency decreasing order of node occurrence
            for(List<FPTreeNode> path: conditionalPatternBase){
                Collections.sort(path, nodeOccurrenceComparator);
            }

            // Build a conditional FP-tree
            ConditionalFPTreeNode root = new ConditionalFPTreeNode(itemSet);
            HashMap<Integer, FPTreeNode> conditionalFPTreeHeaderTable = new HashMap<>();
            buildConditionalFPTree(root, conditionalFPTreeHeaderTable, conditionalPatternBase);

            StringJoiner temp = new StringJoiner(",");
            for(int ii:itemSet){
                temp.add(Integer.toString(ii));
            }
//            System.out.println("itemset: "+ temp.toString() + ", IfThenStatement FP-Tree depth: " + getDepthOfTree(root, 0) + "");

            minedFeaturesNextLevel.addAll(mine(conditionalFPTreeHeaderTable, itemSet, itemSetMatches));

            if(conditionedOn.size() == 0){
                t1 = System.currentTimeMillis();

                if(this.runIndex > -1){
                    System.out.println("[" + this.runIndex + "] " + i + "/" + frequentItemSets.size() +": itemsets found: " +
                            minedFeaturesNextLevel.size() + ", time: " + String.valueOf(t1 - t0) + " msec");

                }else{
                    System.out.println(i + "/" + frequentItemSets.size() +": itemsets found: " +
                            minedFeaturesNextLevel.size() + ", time: " + String.valueOf(t1 - t0) + " msec");
                }

                t0 = t1;
            }

            if(featureIO != null){
                if( minedFeaturesNextLevel.size() > numRulesToSaveInFile ) {
                    List<Feature> out = exportFeatures(minedFeaturesNextLevel);
                    if(saveDataIndex == 0){
                        out.addAll(0, exportFeatures(minedFeatures));
                        minedFeatures.clear();
                    }
                    this.saveData(saveDataIndex++, out);
                    minedFeaturesNextLevel.clear();
                }
            }
        }

        minedFeatures.addAll(minedFeaturesNextLevel);

        if(conditionedOn.size() == 0){
            if(featureIO != null && !minedFeatures.isEmpty()){
                List<Feature> out = exportFeatures(minedFeatures);
                minedFeatures.clear();
                this.saveData(saveDataIndex++, out);
            }

            System.out.println("Num feature count: " + numFeatureCount);
            System.out.println("Num metric calculation: " + numMetricCalculation);
        }
        return minedFeatures;
    }

    protected void buildFPTree(FPTreeNode root, HashMap<Integer, FPTreeNode> headerTable){

        HashMap<Integer, FPTreeNode> nodeLinkEdges = new HashMap<>();
        for(int i = 0; i < this.numberOfObservations; i++){
            if(!super.labels.get(i)){
                // Only use samples that are behavioral
                continue;
            }
            ArrayList<Integer> featureIDs = new ArrayList<>();
            ArrayList<Feature> features = new ArrayList<>();

            for(int j = 0; j < this.baseFeatures.size(); j++){
                if(this.baseFeatures.get(j).getMatches().get(i)){
                    featureIDs.add(j);
                    features.add(this.baseFeatures.get(j));
                }
            }
            root.insertFeatures(featureIDs, features, headerTable, nodeLinkEdges);
        }
    }

    protected List<List<FPTreeNode>> getConditionalPatternBase(HashMap<Integer, FPTreeNode> headerTable,
                                                               Integer featureID){

        List<List<FPTreeNode>> conditionalPatternBase = new ArrayList<>();
        FPTreeNode node = headerTable.get(featureID);
        while(true){
            List<FPTreeNode> path = node.getPrefixPath();
            if(!path.isEmpty()){
                List<FPTreeNode> countAdjustedPath = new ArrayList<>();
                int count = node.getCount();
                for(FPTreeNode nodeInPath: path){
                    FPTreeNode countAdjustedNode = nodeInPath.copy(count);
                    countAdjustedPath.add(countAdjustedNode);
                }
                conditionalPatternBase.add(countAdjustedPath);
            }

            if(node.getNodeLink() == null){
                break;
            }else{
                node = node.getNodeLink();
            }
        }
        return conditionalPatternBase;
    }

    protected void buildConditionalFPTree(ConditionalFPTreeNode root,
                                          HashMap<Integer, FPTreeNode> headerTable,
                                          List<List<FPTreeNode>> conditionalPatternBase){

        HashMap<Integer, FPTreeNode> nodeLinkEdges = new HashMap<>();

        for(int i = 0; i < conditionalPatternBase.size(); i++){
            List<FPTreeNode> prefixPath = conditionalPatternBase.get(i);
            if(!prefixPath.isEmpty()){
                root.insertFeatures(prefixPath, headerTable, nodeLinkEdges);
            }
        }
    }

    protected void removeFeatureFromFPTree(int featureID, HashMap<Integer, FPTreeNode> headerTable){

        FPTreeNode node = headerTable.get(featureID);
        while(true){
            FPTreeNode parent = node.getParent();
            parent.removeChild(node);

            List<FPTreeNode> children = node.getChildren();
            for(FPTreeNode child: children){
                child.setParent(parent);
                parent.addChild(child);
            }

            if(node.getNodeLink() == null){
                break;
            }else{
                node = node.getNodeLink();
            }
        }
        headerTable.remove(featureID);
    }

    /**
     * Computes the metrics of a feature. The feature is represented as the
     * bitset that specifies which base features define it. If the support
     * threshold is not met, then the other metrics are not computed.
     *
     * @param feature the bit set specifying which base features define it
     * @param labels the behavioral/non-behavioral labeling
     * @return a 4-tuple containing support, lift, fcondfidence, and
     * rconfidence. If the support threshold is not met, all metrics will be NaN
     */
    protected double[] computeMetrics(BitSet feature, BitSet labels) {
        double[] out = new double[4];

        BitSet copy = (BitSet) feature.clone();
        copy.and(labels);
        double cnt_SF = (double) copy.cardinality();
        out[0] = cnt_SF / (double) numberOfObservations; //support

        // Check if it passes minimum support threshold
        if (out[0] > supportThreshold) {
            //compute the confidence and lift
            double cnt_S = (double) numberOfBehavioralObservations;
            double cnt_F = (double) feature.cardinality();
            out[1] = (cnt_SF / cnt_S) / (cnt_F / (double) numberOfObservations); //lift
            out[2] = (cnt_SF) / (cnt_F);   // confidence (feature -> selection)
            out[3] = (cnt_SF) / (cnt_S);   // confidence (selection -> feature)
        } else {
            Arrays.fill(out, Double.NaN);
        }
        return out;
    }

    protected double[] computeMetrics(int numFeatureBehavioral, int numFeatureNonBehavioral) {
        double[] out = new double[4];

        double cnt_SF = (double) numFeatureBehavioral;
        out[0] = cnt_SF / (double) numberOfObservations; //support

        // Check if it passes minimum support threshold
        if (out[0] > supportThreshold) {
            //compute the confidence and lift
            double cnt_S = (double) numberOfBehavioralObservations;
            double cnt_F = (double) (numFeatureBehavioral + numFeatureNonBehavioral);
            out[1] = (cnt_SF / cnt_S) / (cnt_F / (double) numberOfObservations); //lift
            out[2] = (cnt_SF) / (cnt_F);   // confidence (feature -> selection)
            out[3] = (cnt_SF) / (cnt_S);   // confidence (selection -> feature)
        } else {
            Arrays.fill(out, Double.NaN);
        }
        return out;
    }

    public List<Feature> exportFeatures(List<FPGrowthFeature> features){

        ArrayList<Feature> out = new ArrayList<>(features.size());

        for (FPGrowthFeature feature:features) {

            //build the binary array taht is 1 for each solution matching the feature
            StringJoiner sj = new StringJoiner("&&");
            List<Integer> featureCombo = feature.getFeatureIndices();

            //find feature indices
            for (int i: featureCombo) {
                sj.add(baseFeatures.get(i).getName());
            }

            out.add(new Feature(sj.toString(), feature.getMatches(),
                    feature.getSupport(), feature.getLift(),
                    feature.getPrecision(), feature.getRecall()));
        }
        return out;
    }

    protected class ConditionalFPTreeNode extends FPTreeNode{

        private List<Integer> conditionedOn;

        public ConditionalFPTreeNode(List<Integer> conditionedOn){
            super();
            this.conditionedOn = conditionedOn;
        }

        public ConditionalFPTreeNode(FPTreeNode node, ConditionalFPTreeNode parent, List<Integer> conditionedOn){
            super(node.getFeatureID(), node.getFeature(), parent);
            this.conditionedOn = conditionedOn;
            this.setCount(node.getCount());
        }

        public List<Integer> getCondition(){
            return this.conditionedOn;
        }

        public void insertFeatures(List<FPTreeNode> branch,
                                   HashMap<Integer, FPTreeNode> headerTable,
                                   HashMap<Integer, FPTreeNode> nodeLinkEdges){

            int index = this.childFeatureIndex(branch.get(0).getFeatureID());
            if(index > -1){
                // One of the child nodes have the feature
                ConditionalFPTreeNode node = (ConditionalFPTreeNode) this.children.get(index);
                node.addCount(branch.get(0).getCount());
                branch.remove(0);

                if(!branch.isEmpty()){
                    node.insertFeatures(branch, headerTable, nodeLinkEdges);
                }

            }else{
                // No child node has the feature
                this.addBranch(branch, headerTable, nodeLinkEdges);
            }
        }

        public void addBranch(List<FPTreeNode> branch,
                              HashMap<Integer, FPTreeNode> headerTable,
                              HashMap<Integer, FPTreeNode> nodeLinkEdges){

            FPTreeNode temp = branch.remove(0);
            ConditionalFPTreeNode node = new ConditionalFPTreeNode(temp, this, this.conditionedOn);

            if(!headerTable.containsKey(node.getFeatureID())){
                // Set the head of the node-links
                headerTable.put(node.getFeatureID(), node);
            }
            if(nodeLinkEdges.containsKey(node.getFeatureID())){
                nodeLinkEdges.get(node.getFeatureID()).setNodeLink(node);
            }
            // Replace the edge with the newly generated node
            nodeLinkEdges.put(node.getFeatureID(), node);

            if(!branch.isEmpty()){
                node.addBranch(branch, headerTable, nodeLinkEdges);
            }
            this.children.add(node);
        }
    }

    protected class FPTreeNode{

        private int featureID;
        private int count;
        private Feature feature;
        private FPTreeNode parent;
        private FPTreeNode nodeLink;
        protected ArrayList<FPTreeNode> children;

        public FPTreeNode(){
            this.parent = null;
            this.feature = null;
            this.children = new ArrayList<>();
            this.count = -1;
            this.nodeLink = null;
        }

        public FPTreeNode(int id, Feature feature, FPTreeNode parent){
            this.featureID = id;
            this.feature = feature;
            this.parent = parent;
            this.children = new ArrayList<>();
            this.nodeLink = null;
            this.count = 1;
        }

        public List<FPTreeNode> getPrefixPath(){
            List<FPTreeNode> path = new ArrayList<>();

            if(!this.getParent().isRoot()){
                // Insert the parent to the front of the prefixPath
                path.add(0, this.getParent());
                // Recursively insert the parent's prefixPath to the front
                path.addAll(0, this.getParent().getPrefixPath());
            }
            return path;
        }

        public void addCount(){
            this.count++;
        }

        public void addCount(int num){
            this.count += num;
        }

        public void setNodeLink(FPTreeNode node){
            if(this.featureID != node.getFeatureID()){
                throw new IllegalStateException();
            }else{
                this.nodeLink = node;
            }
        }

        public void insertFeatures(
                                   ArrayList<Integer> featureIDs,
                                   ArrayList<Feature> features,
                                   HashMap<Integer, FPTreeNode> headerTable,
                                   HashMap<Integer, FPTreeNode> nodeLinkEdges){

            int index = this.childFeatureIndex(featureIDs.get(0));
            if(index > -1){
                // One of the child nodes have the feature
                FPTreeNode node = this.children.get(index);
                node.addCount();
                featureIDs.remove(0);
                features.remove(0);

                if(featureIDs.isEmpty()){
                    return;
                }else{
                    node.insertFeatures(featureIDs, features, headerTable, nodeLinkEdges);
                }

            }else{
                // No child node has the feature
                this.addBranch(featureIDs, features, headerTable, nodeLinkEdges);
            }
        }

        public void addBranch(
                              ArrayList<Integer> featureIDs,
                              ArrayList<Feature> features,
                              HashMap<Integer, FPTreeNode> headerTable,
                              HashMap<Integer, FPTreeNode> nodeLinkEdges){

            int id = featureIDs.remove(0);
            Feature feature = features.remove(0);
            FPTreeNode child = new FPTreeNode(id, feature, this);

            if(!headerTable.containsKey(id)){
                // Set the head of the node-links
                headerTable.put(id, child);
            }
            if(nodeLinkEdges.containsKey(id)){
                nodeLinkEdges.get(id).setNodeLink(child);
            }
            // Replace the edge with the newly generated node
            nodeLinkEdges.put(id, child);

            if(!featureIDs.isEmpty()){
                child.addBranch(featureIDs, features, headerTable, nodeLinkEdges);
            }
            this.children.add(child);
        }

        public int childFeatureIndex(int id){
            if(this.children.isEmpty()){
                return -1;
            }
            for(int i = 0; i < children.size(); i++){
                FPTreeNode child = children.get(i);
                if(child.getFeatureID() == id){
                    return i;
                }
            }
            return -1;
        }

        public void addChild(FPTreeNode node){
            node.setParent(this);
            this.children.add(node);
        }

        public void removeChild(FPTreeNode node){
            this.children.remove(node);
        }

        public void setParent(FPTreeNode node){
            this.parent = node;
        }

        public ArrayList<FPTreeNode> getChildren(){
            return this.children;
        }

        public FPTreeNode getParent() {
            return parent;
        }

        public Feature getFeature(){
            return feature;
        }

        public String getFeatureName(){
            return feature.getName();
        }

        public boolean isRoot(){
            return (this.parent == null) && (this.feature == null);
        }

        public int getFeatureID(){
            return this.featureID;
        }

        public int getCount(){
            return this.count;
        }

        public FPTreeNode getNodeLink() {
            return nodeLink;
        }

        public void setCount(int count){
            this.count = count;
        }

        public FPTreeNode copy(){
            FPTreeNode copy = new FPTreeNode(this.featureID, this.feature, null);
            copy.setCount(this.count);
            // Note: This method does not copy children or parent
            return copy;
        }

        public FPTreeNode copy(int count){
            FPTreeNode copy = new FPTreeNode(this.featureID, this.feature, null);
            copy.setCount(count);
            // Note: This method does not copy children or parent
            return copy;
        }
    }

    public class NodeOccurrenceComparator implements Comparator<FPTreeNode> {

        private Map<Integer, Integer> nodeOccurrenceCounter;

        public NodeOccurrenceComparator(Map<Integer, Integer> nodeOccurrenceCounter){
            this.nodeOccurrenceCounter = nodeOccurrenceCounter;
        }

        @Override
        public int compare(FPTreeNode n1, FPTreeNode n2) {
            return Integer.compare(-nodeOccurrenceCounter.get(n1.getFeatureID()), -nodeOccurrenceCounter.get(n2.getFeatureID()));
        }
    }

    public enum FPGrowthRunMode{
        frequentItemSet,
        features
    }

    private class FPGrowthFeature extends Feature {

        private final List<Integer> featureIndices;

        public FPGrowthFeature(List<Integer> featureIndices, BitSet matches, double support, double lift, double fconfidence, double rconfidence) {
            super(null, matches,support,lift,fconfidence,rconfidence);
            this.featureIndices = featureIndices;
        }

        public List<Integer> getFeatureIndices(){
            return this.featureIndices;
        }
    }
}

