package ifeed.filter;

public interface DiscreteInputFilterOperator extends FilterOperator {

    int[] repair(int[] input);
    int[] disrupt(int[] input);

}
