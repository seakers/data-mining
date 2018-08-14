/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.mining.moea;

import ifeed.feature.logic.Connective;
import org.moeaframework.core.Variable;

import java.io.Serializable;

/**
 *
 * @author hsbang
 */

public class FeatureTreeVariable implements Variable, Serializable {

    private static final long serialVersionUID = 4142639957025157845L;

    Connective root;
    MOEABase base;

    public FeatureTreeVariable(MOEABase base, Connective root) {
        this.root = root;
        this.base = base;
    }

    public void setRoot(Connective root){
        this.root = root;
    }
    public Connective getRoot(){ return this.root; }

    @Override
    public void randomize(){
        // Randomly generate a feature tree and save it
        this.root = this.base.getFeatureSelector().generateRandomFeature();
    }

    @Override
    public Variable copy() {
        return new FeatureTreeVariable(this.base, this.root.copy());
    }

    @Override
    public String toString() {
        return this.root.getName();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FeatureTreeVariable other = (FeatureTreeVariable) obj;
        return this.base.getFeatureHandler().featureTreeEquals(this.root, other.getRoot());
    }

}
