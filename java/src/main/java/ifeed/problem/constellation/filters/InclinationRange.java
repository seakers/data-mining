/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.constellation.filters;

import ifeed.architecture.AbstractArchitecture;
import ifeed.architecture.ContinuousInputArchitecture;
import ifeed.filter.AbstractFilter;
import ifeed.local.params.BaseParams;
import ifeed.problem.constellation.Params;

import java.util.Objects;
import java.util.StringJoiner;

/**
 *
 * @author bang
 */

public class InclinationRange extends AbstractFilter{

    protected Params params;
    protected double lb;
    protected double ub;
    protected int[] cardinality;
    protected int incIndex;

    public InclinationRange(BaseParams params, double lb, double ub, int[] cardinality){
        super(params);
        this.params = (Params) params;
        this.lb = lb;
        this.ub = ub;
        this.cardinality = cardinality;
        this.incIndex = this.params.getOrbitalParamsList().indexOf("inc");
    }

    @Override
    public boolean apply(AbstractArchitecture a){
        return this.apply(((ContinuousInputArchitecture) a).getInputs());
    }

    @Override
    public boolean apply(double[] input){

        boolean out;
        int cnt = 0;

        for(int i = 0; i < this.params.getNumSats(); i++){
            int index = i + (this.params.getNumSats() * this.incIndex);

            double inc = input[index];

            if(inc >= lb && inc < ub){
                cnt++;
            }
        }

        if(cardinality.length == 1){
            if(cnt == cardinality[0]){
                out = true;
            }else{
                out = false;
            }
        }else{
            if(cnt >= cardinality[0] && cnt <= cardinality[1]){
                out = true;
            }else{
                out = false;
            }

        }

        return out;
    }

    @Override
    public String getName(){return "inclinationRange";}

    @Override
    public String toString(){

        StringJoiner sj = new StringJoiner(";");
        sj.add(Double.toString(lb));
        sj.add(Double.toString(ub));

        if(cardinality.length == 1){
            sj.add(Integer.toString(cardinality[0]));
        }else{
            sj.add(Integer.toString(cardinality[0]) + "~" + Integer.toString(cardinality[1]));
        }

        return "{inclinationRange["+ sj.toString() +"]}";
    }

    @Override
    public int hashCode() {
        int hash = 13;
        hash = 31 * hash + Objects.hashCode(this.lb);
        hash = 31 * hash + Objects.hashCode(this.ub);
        hash = 31 * hash + Objects.hashCode(this.cardinality);
        hash = 31 * hash + Objects.hashCode(this.getName());
        return hash;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof AltitudeRange){
            AltitudeRange other = (AltitudeRange) o;
            if(this.lb == other.lb && this.ub == other.ub && this.cardinality == other.cardinality){
                return true;
            }
        }
        return false;
    }
}
