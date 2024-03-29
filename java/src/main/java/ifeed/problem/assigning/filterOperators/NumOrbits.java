///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package ifeed.problem.assigning.filterOperators;
//
//import ifeed.local.params.BaseParams;
//import ifeed.filter.BinaryInputFilterOperator;
//
//import java.util.*;
//
///**
// *
// * @author bang
// */
//public class NumOrbits extends ifeed.problem.assigning.filters.NumOrbits implements BinaryInputFilterOperator {
//
//    public NumOrbits(BaseParams params, int n){
//        super(params, n);
//    }
//
//    @Override
//    public BitSet breakSpecifiedCondition(BitSet input, List<Integer> instruments){
//        return input;
//    }
//
//    @Override
//    public BitSet disrupt(BitSet input){
//
//        if(!super.apply(input)){
//            // Do nothing
//            return input;
//        }else{
//            ArrayList<Integer> nonEmptyOrbits = new ArrayList<>();
//
//            for(int o = 0; o< this.params.getRightSetCardinality(); o++){
//                boolean used = false;
//                for(int i = 0; i< this.params.getLeftSetCardinality(); i++){
//                    if(input.get(o* this.params.getLeftSetCardinality() +i)){
//                        used=true;
//                        break;
//                    }
//                }
//                if(used){
//                    nonEmptyOrbits.add(o);
//                }
//            }
//
//            // Satisfies all constraints
//            Random random = new Random();
//            int max = nonEmptyOrbits.size() - 1;
//            int min = 0;
//            int randInt = random.nextInt(max + 1 - min) + min;
//            int randOrb = nonEmptyOrbits.get(randInt);
//
//            BitSet out = (BitSet) input.clone();
//            for(int i = 0; i < this.params.getLeftSetCardinality(); i++){
//                out.clear(randOrb * this.params.getLeftSetCardinality() + i);
//            }
//            return out;
//        }
//    }
//
//    @Override
//    public BitSet repair(BitSet input){
//        if(super.apply(input)){
//            // Do nothing
//            return input;
//        }else{
//            ArrayList<Integer> nonEmptyOrbits = new ArrayList<>();
//            ArrayList<Integer> emptyOrbits = new ArrayList<>();
//
//            for(int o = 0; o< this.params.getRightSetCardinality(); o++){
//                boolean used = false;
//                for(int i = 0; i< this.params.getLeftSetCardinality(); i++){
//                    if(input.get(o* this.params.getLeftSetCardinality() +i)){
//                        used=true;
//                        break;
//                    }
//                }
//                if(used){
//                    nonEmptyOrbits.add(o);
//                }else{
//                    emptyOrbits.add(o);
//                }
//            }
//
//            Random random = new Random();
//            BitSet out = (BitSet) input.clone();
//            int diff = super.num - nonEmptyOrbits.size();
//
//            for(int i = 0; i < Math.abs(diff); i++){
//                if(diff > 0){
//                    // Add instruments to empty orbits
//                    Collections.shuffle(emptyOrbits);
//                    int o = emptyOrbits.get(0);
//                    emptyOrbits.remove(0);
//
//                    // Get random instrument to add
//                    int max = this.params.getLeftSetCardinality() - 1;
//                    int min = 0;
//                    int randInt = random.nextInt(max + 1 - min) + min;
//                    out.set(o * this.params.getLeftSetCardinality() + randInt);
//                }else{
//                    // Remove instruments from nonEmpty orbits
//                    // Add instruments to empty orbits
//                    Collections.shuffle(nonEmptyOrbits);
//                    int o = nonEmptyOrbits.get(0);
//                    nonEmptyOrbits.remove(0);
//
//                    for(int j = 0; j < this.params.getLeftSetCardinality(); j++){
//                        out.clear(o * this.params.getLeftSetCardinality() + j);
//                    }
//                }
//            }
//
//            return out;
//        }
//    }
//
//    @Override
//    public void mutate(){
//        int store = this.num;
//        while(store == this.num){
//            Random random = new Random();
//            int max = this.params.getRightSetCardinality();
//            int min = 1;
//            int randInt = random.nextInt(max + 1 - min) + min;
//            this.num = randInt;
//        }
//    }
//}
