/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.assigning;

import ifeed.architecture.AbstractArchitecture;
import ifeed.feature.Feature;
import ifeed.filter.AbstractFilter;
import ifeed.mining.arm.AbstractAssociationRuleMining;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.*;
import java.util.stream.IntStream;

/**
 *
 * @author bang
 */

public class AssociationRuleMining extends AbstractAssociationRuleMining {

    public AssociationRuleMining(List<AbstractArchitecture> architectures, List<Integer> behavioral, List<Integer> non_behavioral, double supp, double conf, double lift) {
        super(architectures, behavioral, non_behavioral, supp, conf, lift);
    }

    @Override
    public List<AbstractFilter> generateCandidates(){
        return new FeatureGenerator().generateCandidates();
    }

    // TODO: Remove this
    public void writeToFile(List<Feature> baseFeatures){

        File file = new File("/Users/bang/workspace/FeatureExtractionGA/data/baseFeatures");
        File file2 = new File("/Users/bang/workspace/FeatureExtractionGA/data/featureNames");
        File file3 = new File("/Users/bang/workspace/FeatureExtractionGA/data/labels");

        int dataSize = this.architectures.size();

        try{

            BufferedWriter writer = new BufferedWriter(new FileWriter(file));

            BufferedWriter featureNameWriter = new BufferedWriter(new FileWriter(file2));


            String printRow = "";

            for(int j=0;j<baseFeatures.size();j++){

                BitSet bs = baseFeatures.get(j).getMatches();
                int nbits = dataSize;

                final StringBuilder buffer = new StringBuilder(nbits);
                IntStream.range(0, nbits).mapToObj(i -> bs.get(i) ? '1' : '0').forEach(buffer::append);

                writer.write(buffer.toString() + "\n");
                featureNameWriter.write(baseFeatures.get(j).getName() + "\n");
            }

            System.out.println("Done");
            writer.close();
            featureNameWriter.close();

        }catch(IOException e){
            System.out.println(e.getMessage());
        }

        try{

            BufferedWriter writer = new BufferedWriter(new FileWriter(file3));
            String printRow = "";

            BitSet bs = this.labels;
            int nbits = dataSize;

            final StringBuilder buffer = new StringBuilder(nbits);
            IntStream.range(0, nbits).mapToObj(i -> bs.get(i) ? '1' : '0').forEach(buffer::append);

            writer.write(buffer.toString() + "\n");

            System.out.println("Done");
            writer.close();

        }catch(IOException e){
            System.out.println(e.getMessage());
        }
    }
}