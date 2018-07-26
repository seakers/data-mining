package ifeed.problem.eossPartitioningAndAssignment.filters;///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package ifeed.problem.eoss.filters;
//
//import ifeed.architecture.AbstractArchitecture;
//import ifeed.architecture.BinaryInputArchitecture;
//import ifeed.problem.eoss.EOSSParams;
//import ifeed.filter.Filter;
//
//import java.util.BitSet;
//import java.util.HashSet;
//import java.util.StringJoiner;
//
///**
// *
// * @author bang
// */
//public class SeparateTemp extends Filter {
//
//    private HashSet<Integer> instruments;
//
//    public SeparateTemp(int[] instruments){
//        this.instruments = new HashSet<>();
//        for(int i:instruments){
//            this.instruments.add(i);
//        }
//    }
//
//    public HashSet<Integer> getInstruments() {
//        return instruments;
//    }
//
//    @Override
//    public boolean apply(AbstractArchitecture a){
//        return this.apply(((BinaryInputArchitecture) a).getInputs());
//    }
//
//    @Override
//    public boolean apply(BitSet input){
//        boolean out = false;
//        for(int o = 0; o < EOSSParams.num_orbits; o++){
//            boolean sat = true;
//            for(int i:instruments){
//                if(!input.get(o*EOSSParams.num_instruments+i)){
//                    // If any one of the instruments are not present
//                    sat=false;
//                    break;
//                }
//            }
//            if(sat){
//                out=true;
//                break;
//            }
//        }
//        return !out;
//    }
//
//    @Override
//    public String getName(){return "separate";}
//
//    @Override
//    public String toString(){
//        StringJoiner sj = new StringJoiner(",");
//        for(int i:this.instruments){
//            sj.add(Integer.toString(i));
//        }
//        return "{separate[;" + sj.toString() + ";]}";
//    }
//
//    @Override
//    public boolean equals(Object o){
//        if(o instanceof SeparateTemp){
//            SeparateTemp other = (SeparateTemp) o;
//            return this.instruments.equals(other.getInstruments());
//        }
//        return false;
//    }
//
//}
