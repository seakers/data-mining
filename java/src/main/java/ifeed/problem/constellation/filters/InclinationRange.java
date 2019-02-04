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
import ifeed.problem.constellation.AbstractConstellationProblemParams;

import java.util.Objects;
import java.util.StringJoiner;

/**
 *
 * @author bang
 */

public class InclinationRange extends AbstractFilter{

    protected AbstractConstellationProblemParams params;
    protected double lb;
    protected double ub;
    protected Integer cardinality;
    protected Integer[] cardinalityRange;
    protected int incIndex;

    public InclinationRange(BaseParams params, double lb, double ub, Integer cardinality){
        super(params);
        this.params = (AbstractConstellationProblemParams) params;
        this.lb = lb;
        this.ub = ub;
        this.cardinality = cardinality;
        this.cardinalityRange = null;
        this.incIndex = this.params.getOrbitalParametersList().indexOf("inc");
    }

    public InclinationRange(BaseParams params, double lb, double ub, Integer[] cardinalityRange){
        super(params);
        this.params = (AbstractConstellationProblemParams) params;
        this.lb = lb;
        this.ub = ub;
        this.cardinality = null;
        this.cardinalityRange = cardinalityRange;
        if(this.cardinalityRange[0] != null || cardinalityRange[1] != null){
            if(this.cardinalityRange[0] == null){
                this.cardinalityRange[0] = 0;
            }
            if(this.cardinalityRange[1] == null){
                this.cardinalityRange[1] = 999;
            }
        }
        this.incIndex = this.params.getOrbitalParametersList().indexOf("inc");
    }

    @Override
    public boolean apply(AbstractArchitecture a){
        return this.apply(((ContinuousInputArchitecture) a).getInputs());
    }

    @Override
    public boolean apply(double[] input){

        boolean out;
        int cnt = 0;

        int numSats;
        if(this.params.isNumSatsFixed()){
            numSats = this.params.getNumSats();
        }else{
            numSats = input.length / this.params.getOrbitalParameters().length;
        }

        for(int i = 0; i < numSats; i++){
            int index = i + (numSats * this.incIndex);

            double inc = input[index];
            if(inc >= lb && inc < ub){
                cnt++;
            }
        }

        if(cardinality != null){
            if(cnt == cardinality){
                out = true;
            }else{
                out = false;
            }
        }else{
            if(cardinalityRange[0] == null && cardinalityRange[1] == null){
                out = cnt == numSats;
            }else{
                if(cnt >= cardinalityRange[0] && cnt <= cardinalityRange[1]){
                    out = true;
                }else{
                    out = false;
                }
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

        if(cardinality != null){
            sj.add(Integer.toString(cardinality));
        }else{
            if(cardinalityRange[0] != null || cardinalityRange[1] != null){

                StringBuilder sb = new StringBuilder();
                if(cardinalityRange[0] != null){
                    sb.append(Integer.toString(cardinalityRange[0]));
                }
                sb.append("~");
                if(cardinalityRange[1] != null){
                    sb.append(Integer.toString(cardinalityRange[1]));
                }
                sj.add(sb.toString());
            }else{
                sj.add("");
            }
        }
        return "{inclinationRange["+ sj.toString() +"]}";
    }

    @Override
    public int hashCode() {
        int hash = 13;
        hash = 31 * hash + Objects.hashCode(this.lb);
        hash = 31 * hash + Objects.hashCode(this.ub);
        hash = 31 * hash + Objects.hashCode(this.cardinality);
        hash = 31 * hash + Objects.hashCode(this.cardinalityRange);
        hash = 31 * hash + Objects.hashCode(this.getName());
        return hash;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof InclinationRange){
            InclinationRange other = (InclinationRange) o;
            if(this.lb == other.lb && this.ub == other.ub && this.cardinality == other.cardinality && this.cardinalityRange == other.cardinalityRange){
                return true;
            }
        }
        return false;
    }
}
