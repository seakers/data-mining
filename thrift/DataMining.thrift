


/**
 *  The available types in Thrift are:
 *
 *  bool        Boolean, one byte
 *  i8 (byte)   Signed 8-bit integer
 *  i16         Signed 16-bit integer
 *  i32         Signed 32-bit integer
 *  i64         Signed 64-bit integer
 *  double      64-bit floating point value
 *  string      String
 *  binary      Blob (byte array)
 *  map<t1,t2>  Map from one type to another
 *  list<t1>    Ordered list of one type
 *  set<t1>     Set of unique elements of one type
 *
 */


namespace java javaInterface
namespace py pyInterface

typedef i32 int


struct Feature{
  1: int id,
  2: string name,
  3: string expression,
  4: list<double> metrics
}

struct BinaryInputArchitecture{
  1: int id,
  2: list<bool> inputs,
  3: list<double> outputs
}

struct DiscreteInputArchitecture{
  1: int id,
  2: list<int> inputs,
  3: list<double> outputs
}

struct Architecture{
  1: int id,
  2: list<double> inputs,
  3: list<double> outputs
}


service DataMiningInterface{
   
   void ping(),

   list<Feature> getDrivingFeaturesBinary(1:string problem, 2:list<int> behavioral, 3:list<int> non_behavioral, 4:list<BinaryInputArchitecture> all_archs, 5:double supp, 6:double conf, 7:double lift),
   
   list<Feature> runAutomatedLocalSearchBinary(1:string problem, 2:list<int> behavioral, 3:list<int> non_behavioral, 4:list<BinaryInputArchitecture> all_archs, 5:double supp, 6:double conf, 7:double lift),
   
   list<Feature> getMarginalDrivingFeaturesConjunctiveBinary(1:string problem, 2:list<int> behavioral, 3:list<int> non_behavioral, 4:list<BinaryInputArchitecture> all_archs, 5:string feature, 6:list<int> archs_with_feature, 7:double supp, 8:double conf, 9:double lift),
   
   list<Feature> getMarginalDrivingFeaturesBinary(1:string problem, 2:list<int> behavioral, 3:list<int> non_behavioral, 4:list<BinaryInputArchitecture> all_archs, 5:string featureExpression, 6:double supp, 7:double conf, 8:double lift)




   list<Feature> getDrivingFeaturesDiscrete(1:string problem, 2:list<int> behavioral, 3:list<int> non_behavioral, 4:list<DiscreteInputArchitecture> all_archs, 5:double supp, 6:double conf, 7:double lift),
   
   list<Feature> runAutomatedLocalSearchDiscrete(1:string problem, 2:list<int> behavioral, 3:list<int> non_behavioral, 4:list<DiscreteInputArchitecture> all_archs, 5:double supp, 6:double conf, 7:double lift),
   
   list<Feature> getMarginalDrivingFeaturesConjunctiveDiscrete(1:string problem, 2:list<int> behavioral, 3:list<int> non_behavioral, 4:list<DiscreteInputArchitecture> all_archs, 5:string feature, 6:list<int> archs_with_feature, 7:double supp, 8:double conf, 9:double lift),
   
   list<Feature> getMarginalDrivingFeaturesDiscrete(1:string problem, 2:list<int> behavioral, 3:list<int> non_behavioral, 4:list<DiscreteInputArchitecture> all_archs, 5:string featureExpression, 6:double supp, 7:double conf, 8:double lift)

}


