package ifeed.problem.assigning.filtersWithException;

import ifeed.filter.AbstractFilter;

import java.util.List;

public class FilterException {

    List<AbstractFilter> allowedExceptions;

    public FilterException(List<AbstractFilter> allowedExceptions){
        this.allowedExceptions = allowedExceptions;
    }

}
