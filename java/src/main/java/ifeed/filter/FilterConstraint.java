package ifeed.filter;

public abstract class FilterConstraint {

    protected Class constraintSetterClass;
    protected Class targetClass;

    protected FilterConstraint(Class constraintSetterClass, Class targetClass){
        this.constraintSetterClass = constraintSetterClass;
        this.targetClass = targetClass;
    }

    public String getConstraintSetterClassName(){
        return this.constraintSetterClass.getSimpleName();
    }

    public String getTargetClassName(){
        return this.targetClass.getSimpleName();
    }

    public abstract void setConstraints(Filter constraintSetter);

    public abstract boolean check(Filter filterToTest);
}
