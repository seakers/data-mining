package ifeed.filter;

public abstract class AbstractFilterConstraint {

    protected Class constraintSetterClass;
    protected Class targetClass;

    protected AbstractFilterConstraint(Class constraintSetterClass, Class targetClass){
        this.constraintSetterClass = constraintSetterClass;
        this.targetClass = targetClass;
    }

    public String getConstraintSetterClassName(){
        return this.constraintSetterClass.getSimpleName();
    }

    public String getTargetClassName(){
        return this.targetClass.getSimpleName();
    }

    public abstract void setConstraints(AbstractFilter constraintSetter);

    public abstract boolean check(AbstractFilter filterToTest);
}
