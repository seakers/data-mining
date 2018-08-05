package ifeed.filter;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractFilterFinder {

    protected Class constraintSetterClass;
    protected Set<Class> matchingClasses;

    protected AbstractFilterFinder(Class constraintSetterClass, Class matchingClass){
        this.constraintSetterClass = constraintSetterClass;
        this.matchingClasses = new HashSet<>();
        this.matchingClasses.add(matchingClass);
    }

    protected AbstractFilterFinder(Class constraintSetterClass, HashSet<Class> matchingClasses){
        this.constraintSetterClass = constraintSetterClass;
        this.matchingClasses = matchingClasses;
    }

    public String getConstraintSetterClassName(){
        return this.constraintSetterClass.getSimpleName();
    }

    public Set<String> getMatchingClassName(){
        Set<String> names = new HashSet<>();
        for(Class c:this.matchingClasses){
            names.add(c.getSimpleName());
        }
        return names;
    }

    public boolean isConstraintSetterType(Class c){
        if(c == this.constraintSetterClass.getClass()){
            return true;
        }else{
            return false;
        }
    }

    public boolean isMatchingType(Class c){
        if(this.matchingClasses.contains(c)){
            return true;
        }else{
            return false;
        }
    }

    public abstract void setConstraints(AbstractFilter constraintSetter);
    public abstract void clearConstraints();

    public boolean check(AbstractFilter filterToTest){
        throw new UnsupportedOperationException();
    }

    public boolean check(Set<AbstractFilter> filtersToTest){
        throw new UnsupportedOperationException();
    }
}
