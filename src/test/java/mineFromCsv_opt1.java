import ifeed.server.ContinuousInputArchitecture;
import ifeed.server.DataMiningInterfaceHandler;
import ifeed.server.Feature;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class mineFromCsv_opt1 {
    public static void main(String[] args){
        DataMiningInterfaceHandler handler = new DataMiningInterfaceHandler();
        List<Integer> behavioral = new ArrayList<>();
        List<Integer> nonBehavioral = new ArrayList<>();
        List<ContinuousInputArchitecture> arches = new ArrayList<>();
        List<List<String>> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("opt1_arches.csv"))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                records.add(Arrays.asList(values));
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        for (int i = 0; i < records.size(); i++) {
            List<Double> dvs = new ArrayList<>();
            dvs.add(Double.parseDouble(records.get(i).get(0)));
            dvs.add(Double.parseDouble(records.get(i).get(1)));
            dvs.add(Double.parseDouble(records.get(i).get(2)));
            List<Double> objs = new ArrayList<>();
            objs.add(Double.parseDouble(records.get(i).get(3)));
            objs.add(Double.parseDouble(records.get(i).get(4)));
            arches.add(new ContinuousInputArchitecture(i,dvs,objs));
        }
        int objNum = 2;
        int[] objIndices = new int[]{0,1};
        int[] domination_counter = new int[arches.size()];
        for (int i = 0; i < arches.size(); i++) {
            for (int j = i + 1; j < arches.size(); j++) {
                Integer[] dominate = new Integer[objNum];
                for (int k = 0; k < objNum; k++) {
                    if(arches.get(i).getOutputs().get(objIndices[k]) > arches.get(j).getOutputs().get(objIndices[k])) {
                        dominate[k] = -1;
                    } else if (arches.get(i).getOutputs().get(objIndices[k]) < arches.get(j).getOutputs().get(objIndices[k])) {
                        dominate[k] = 1;
                    }
                }
                List<Integer> dominateList = new ArrayList<>(Arrays.asList(dominate));
                if (dominateList.contains(1) && !dominateList.contains(-1)) {
                    domination_counter[j] += 1;
                } else if (dominateList.contains(-1) && !dominateList.contains(1)) {
                    domination_counter[i] += 1;
                }
            }
        }
        for (int y = 0; y < domination_counter.length; y++) {
            if(domination_counter[y]==0) {
                behavioral.add(y);
            } else {
                nonBehavioral.add(y);
            }
        }
        List<Feature> features = handler.getDrivingFeaturesContinuous("dshield_opt1",behavioral,nonBehavioral,arches,0.05,0.3,1.0);
        for (Feature f : features) {
            System.out.println(f);
        }
    }
}