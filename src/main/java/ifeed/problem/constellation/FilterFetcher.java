package ifeed.problem.constellation;

import ifeed.filter.AbstractFilter;
import ifeed.filter.AbstractFilterFetcher;
import ifeed.local.params.BaseParams;
import ifeed.problem.constellation.filters.*;

import java.util.List;

public class FilterFetcher extends AbstractFilterFetcher {

    public FilterFetcher(BaseParams params){
        super(params);
    }

    @Override
    public AbstractFilter fetch(List<String> names, List<String[]> argSets){

        AbstractFilter filter;
        String type = names.get(0);
        String[] args = argSets.get(0);

        try{
            switch (type) {
                case "inclinationRange":

                    double lb = Double.parseDouble(args[0]);
                    double ub = Double.parseDouble(args[1]);

                    Integer cardinality;
                    Integer[] cardinalityRange;

                    if(args[2] == null){
                        filter = new InclinationRange(super.params, lb, ub, (Integer) null);

                    }else if(args[2].contains("~")){ // Range is given

                        cardinalityRange = new Integer[2];
                        String[] temp = args[2].split("~");

                        if(temp.length == 1){ // example: "a~"
                            cardinalityRange[0] = Integer.parseInt(temp[0]);
                            cardinalityRange[1] = 999;
                        }else{
                            if(temp[0].isEmpty()){ // example: "~a"
                                cardinalityRange[0] = 0;
                                cardinalityRange[1] = Integer.parseInt(temp[1]);

                            }else{ // example: "a~b"
                                cardinalityRange[0] = Integer.parseInt(temp[0]);
                                cardinalityRange[1] = Integer.parseInt(temp[1]);
                            }
                        }
                        filter = new InclinationRange(super.params, lb, ub, cardinalityRange);

                    }else{
                        cardinality = Integer.parseInt(args[2]);
                        filter = new InclinationRange(super.params, lb, ub, cardinality);
                    }
                    break;

                case "altitudeRange":

                    lb = Double.parseDouble(args[0]);
                    ub = Double.parseDouble(args[1]);

                    if(args[2] == null){
                        filter = new AltitudeRange(super.params, lb, ub, (Integer) null);

                    }else if(args[2].contains("~")){ // Range is given

                        cardinalityRange = new Integer[2];
                        String[] temp = args[2].split("~");

                        if(temp.length == 1){ // example: "a~"
                            cardinalityRange[0] = Integer.parseInt(temp[0]);
                            cardinalityRange[1] = 999;
                        }else{
                            if(temp[0].isEmpty()){ // example: "~a"
                                cardinalityRange[0] = 0;
                                cardinalityRange[1] = Integer.parseInt(temp[1]);

                            }else{ // example: "a~b"
                                cardinalityRange[0] = Integer.parseInt(temp[0]);
                                cardinalityRange[1] = Integer.parseInt(temp[1]);
                            }
                        }
                        filter = new AltitudeRange(super.params, lb, ub, cardinalityRange);

                    }else{
                        cardinality = Integer.parseInt(args[2]);
                        filter = new AltitudeRange(super.params, lb, ub, cardinality);
                    }
                    break;

                case "numSats":

                    if(args[0].contains("~")){ // Range is given

                        cardinalityRange = new Integer[2];
                        String[] temp = args[0].split("~");

                        if(temp.length == 1){ // example: "a~"
                            cardinalityRange[0] = Integer.parseInt(temp[0]);
                            cardinalityRange[1] = 999;

                        }else{
                            if(temp[0].isEmpty()){ // example: "~a"
                                cardinalityRange[0] = 0;
                                cardinalityRange[1] = Integer.parseInt(temp[1]);

                            }else{ // example: "a~b"
                                cardinalityRange[0] = Integer.parseInt(temp[0]);
                                cardinalityRange[1] = Integer.parseInt(temp[1]);
                            }
                        }
                        filter = new NumSats(super.params, cardinalityRange);

                    }else{
                        cardinality = Integer.parseInt(args[0]);
                        filter = new NumSats(super.params, cardinality);
                    }
                    break;

                default:
                    throw new RuntimeException("Could not find filter type of: " + type);
            }
            return filter;

        }catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException("Exc in fetching a feature of type: " + type);
        }
    }

}
