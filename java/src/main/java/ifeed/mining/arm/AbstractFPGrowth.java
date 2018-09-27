package ifeed.mining.arm;

import ifeed.architecture.AbstractArchitecture;
import ifeed.feature.Feature;
import ifeed.feature.FeatureMetric;
import ifeed.feature.FeatureMetricComparator;
import ifeed.local.params.BaseParams;
import java.io.File;
import java.util.*;

public abstract class AbstractFPGrowth extends AbstractAssociationRuleMining{

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

    private String path;
    private String dirname;
    private String filename;

    private FPTreeNode root;
    private HashMap<Integer, FPTreeNode> headerTable;

    public AbstractFPGrowth(BaseParams params,
                            List<AbstractArchitecture> architectures,
                            List<Integer> behavioral,
                            List<Integer> non_behavioral,
                            double supp, double conf, double lift){

        super(params, architectures, behavioral, non_behavioral, supp, conf, lift);

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

        this.path =  System.getProperty("user.dir");

        this.dirname = this.path + File.separator + "temp" + File.separator;
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
        System.out.println("Finished building FP Tree (depth: " + getDepthOfTree(this.root, 0) + ")");

        List<List<Integer>> minedFeatures = mine(headerTable, new ArrayList<>());
        //System.out.println(frequentItemSets);



        long t1 = System.currentTimeMillis();
//        System.out.println("...["+ this.getClass().getSimpleName() +"] Total features found: " + extracted_features.size());
        System.out.println("...["+ this.getClass().getSimpleName() +"] Total data mining time : " + String.valueOf(t1 - t0) + " msec");
        return new ArrayList<>();
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
        }else{
            return depth + 1;
        }
    }

    protected int countFeature(boolean behavioral, int id, HashMap<Integer, FPTreeNode> headerTable){
        if(!headerTable.containsKey(id)){
            return 0;
        }
        int count = 0;
        FPTreeNode node = headerTable.get(id);
        while(true){
            count += node.getCount(behavioral);

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

    protected List<List<Integer>> mine(HashMap<Integer, FPTreeNode> headerTable, List<Integer> conditionedOn){

        List<List<Integer>> minedFeatures = new ArrayList<>();
        List<List<Integer>> frequentItemSets = new ArrayList<>();
        List<Integer> frequentItems = new ArrayList<>();

        if(conditionedOn.size() > 1){
            return frequentItemSets;
        }

        for(int id: headerTable.keySet()){
            int bCount = countFeature(true, id, headerTable);
            int nCount = countFeature(false, id, headerTable);

            double[] metrics = computeMetrics(bCount, nCount);
            double support = metrics[0];
            double confidence = metrics[2]; // Confidence given feature (equivalent to precision)

            if( support >= this.supportThreshold){
                List<Integer> temp = new ArrayList<>(conditionedOn);
                temp.add(id);
                frequentItemSets.add(temp);
                frequentItems.add(id);

                if(confidence >= this.confidence_threshold){
                    minedFeatures.add(temp);
                }
            }
        }

        List<List<Integer>> minedFeaturesNextLevel = new ArrayList<>();
        for(int i = frequentItems.size()-1; i >= 0; i--){

            int featureID = frequentItems.get(i);
            List<Integer> itemSet = frequentItemSets.get(i);

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
                Collections.reverse(path);
            }

            // Build a conditional FP-tree
            ConditionalFPTreeNode root = new ConditionalFPTreeNode(itemSet);
            HashMap<Integer, FPTreeNode> conditionalFPTreeHeaderTable = new HashMap<>();
            buildConditionalFPTree(root, conditionalFPTreeHeaderTable, conditionalPatternBase);

            StringJoiner temp = new StringJoiner(",");
            for(int ii:itemSet){
                temp.add(Integer.toString(ii));
            }

            //System.out.println("itemset: "+ temp.toString() +", conditionalPatternBaseAvgLength: "+ avgLength + ", Conditional FP-Tree depth: " + getDepthOfTree(root, 0) + "");

            minedFeaturesNextLevel.addAll(mine(conditionalFPTreeHeaderTable, itemSet));

            if(conditionedOn.size() == 0){
                System.out.println(i + "/" + frequentItemSets.size() +", itemsets found: " + minedFeaturesNextLevel.size());
            }
        }

        minedFeatures.addAll(minedFeaturesNextLevel);
        return minedFeatures;
    }

    protected void buildFPTree(FPTreeNode root, HashMap<Integer, FPTreeNode> headerTable){

        HashMap<Integer, FPTreeNode> nodeLinkEdges = new HashMap<>();

        for(int i = 0; i < this.numberOfObservations; i++){

            boolean behavioral = false;
            if(super.labels.get(i)){
                behavioral = true;
            }
            ArrayList<Integer> featureIDs = new ArrayList<>();
            ArrayList<Feature> features = new ArrayList<>();

            for(int j = 0; j < this.baseFeatures.size(); j++){
                if(this.baseFeatures.get(j).getMatches().get(i)){
                    featureIDs.add(j);
                    features.add(this.baseFeatures.get(j));
                }
            }
            root.insertFeatures(behavioral, featureIDs, features, headerTable, nodeLinkEdges);
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
                int bCount = node.getCount(true);
                int nCount = node.getCount(false);
                for(FPTreeNode nodeInPath: path){
                    FPTreeNode countAdjustedNode = nodeInPath.copy();
                    countAdjustedNode.setCount(true, bCount);
                    countAdjustedNode.setCount(false, nCount);
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


//    protected void pruneConditionalFPTree(HashMap<Integer, FPTreeNode> headerTable){
//
//        List<Integer> nodesToRemove = new ArrayList<>();
//
//        for(int key: headerTable.keySet()){
//            if(!(headerTable.get(key) instanceof ConditionalFPTreeNode)){
//                throw new IllegalArgumentException("Conditional FPTree should be given.");
//            }
//
//            // Add up all the counts along the node-links
//            int cnt = 0;
//            FPTreeNode node = headerTable.get(key);
//            while(true){
//                cnt += node.getCount();
//                if(node.getNodeLink() == null){
//                    break;
//                }else{
//                    node = node.getNodeLink();
//                }
//            }
//
//            if( ((double) cnt / this.numberOfObservations) < this.supportThreshold){
//                // Remove this node from the tree
//                nodesToRemove.add(key);
//            }
//        }
//
//        for(int featureID: nodesToRemove){
//            removeFeatureFromFPTree(featureID, headerTable);
//        }
//    }


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

    protected class ConditionalFPTreeNode extends FPTreeNode{

        private List<Integer> conditionedOn;

        public ConditionalFPTreeNode(List<Integer> conditionedOn){
            super();
            this.conditionedOn = conditionedOn;
        }

        public ConditionalFPTreeNode(FPTreeNode node, ConditionalFPTreeNode parent, List<Integer> conditionedOn){
            super(node.getFeatureID(), node.getFeature(), parent, false);
            this.conditionedOn = conditionedOn;
            this.setCount(true, node.getCount(true));
            this.setCount(false, node.getCount(false));

            for(FPTreeNode child: node.getChildren()){
                this.children.add(new ConditionalFPTreeNode(child, this, conditionedOn));
            }
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
                node.addCount(branch.get(0).getCount(true), true);
                node.addCount(branch.get(0).getCount(false), false);
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
        private int bCount;
        private int nCount;
        private Feature feature;
        private FPTreeNode parent;
        private FPTreeNode nodeLink;
        protected ArrayList<FPTreeNode> children;

        public FPTreeNode(){
            this.parent = null;
            this.feature = null;
            this.children = new ArrayList<>();
            this.bCount = -1;
            this.nCount = -1;
            this.nodeLink = null;
        }

        public FPTreeNode(int id, Feature feature, FPTreeNode parent, boolean behavioral){
            this.featureID = id;
            this.feature = feature;
            this.parent = parent;
            this.children = new ArrayList<>();
            this.nodeLink = null;
            if(behavioral){
                this.bCount = 1;
                this.nCount = 0;
            }else{
                this.bCount = 0;
                this.nCount = 1;
            }
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

        public void addCount(boolean behavioral){
            if(behavioral){
                this.bCount++;
            }else{
                this.nCount++;
            }

        }

        public void addCount(int num, boolean behavioral){
            if(behavioral){
                this.bCount = this.bCount + num;
            }else{
                this.nCount = this.nCount + num;
            }
        }

        public void setNodeLink(FPTreeNode node){
            if(this.featureID != node.getFeatureID()){
                throw new IllegalStateException();
            }else{
                this.nodeLink = node;
            }
        }

        public void insertFeatures(boolean behavioral,
                                   ArrayList<Integer> featureIDs,
                                   ArrayList<Feature> features,
                                   HashMap<Integer, FPTreeNode> headerTable,
                                   HashMap<Integer, FPTreeNode> nodeLinkEdges){

            int index = this.childFeatureIndex(featureIDs.get(0));
            if(index > -1){
                // One of the child nodes have the feature
                FPTreeNode node = this.children.get(index);
                node.addCount(behavioral);
                featureIDs.remove(0);
                features.remove(0);

                if(featureIDs.isEmpty()){
                    return;
                }else{
                    node.insertFeatures(behavioral, featureIDs, features, headerTable, nodeLinkEdges);
                }

            }else{
                // No child node has the feature
                this.addBranch(behavioral, featureIDs, features, headerTable, nodeLinkEdges);
            }
        }

        public void addBranch(boolean behavioral,
                              ArrayList<Integer> featureIDs,
                              ArrayList<Feature> features,
                              HashMap<Integer, FPTreeNode> headerTable,
                              HashMap<Integer, FPTreeNode> nodeLinkEdges){

            int id = featureIDs.remove(0);
            Feature feature = features.remove(0);
            FPTreeNode child = new FPTreeNode(id, feature, this, behavioral);

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
                child.addBranch(behavioral, featureIDs, features, headerTable, nodeLinkEdges);
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

        public int getCount(boolean behavioral){
            if(behavioral){
                return this.bCount;
            }else{
                return this.nCount;
            }
        }

        public FPTreeNode getNodeLink() {
            return nodeLink;
        }

        public void setCount(boolean behavioral, int count){
            if(behavioral){
                this.bCount = count;
            } else{
                this.nCount = count;
            }
        }

        public FPTreeNode copy(){
            FPTreeNode copy = new FPTreeNode(this.featureID, this.feature, null, false);
            copy.setCount(true, this.bCount);
            copy.setCount(false, this.nCount);

//            for(FPTreeNode node: this.children){
//                copy.addChild(node.copy());
//            }
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
            return Integer.compare(nodeOccurrenceCounter.get(n1.getFeatureID()), nodeOccurrenceCounter.get(n2.getFeatureID()));
        }
    }

}

