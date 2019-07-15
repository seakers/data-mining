
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


namespace java ifeed.server
namespace py pyInterface

typedef i32 int


struct Feature{
  1: int id,
  2: string name,
  3: string expression,
  4: list<double> metrics,
  5: double complexity,
  6: string description
}

struct Architecture{
  1: int id,
  2: list<double> inputs,
  3: list<double> outputs
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

struct ContinuousInputArchitecture{
  1: int id,
  2: list<double> inputs,
  3: list<double> outputs
}

struct AssigningProblemEntities{
  1: list<string> leftSet,
  2: list<string> rightSet
}

struct FlattenedConceptHierarchy{
  1: map<string, list<string>> instanceMap,
  2: map<string, list<string>> superclassMap
}

service DataMiningInterface{
   
   void ping(),



   // Binary Input

   list<Feature> getDrivingFeaturesBinary(1:string problem, 2:list<int> behavioral, 3:list<int> non_behavioral, 4:list<BinaryInputArchitecture> all_archs, 5:double supp, 6:double conf, 7:double lift),
   
   list<Feature> getMarginalDrivingFeaturesBinary(1:string problem, 2:list<int> behavioral, 3:list<int> non_behavioral, 4:list<BinaryInputArchitecture> all_archs, 5:string featureExpression, 6:string logical_connective, 7:double supp, 8:double conf, 9:double lift),

   list<Feature> getDrivingFeaturesEpsilonMOEABinary(1:string problem, 2:list<int> behavioral, 3:list<int> non_behavioral, 4:list<BinaryInputArchitecture> all_archs),





   // Discrete Input
   list<Feature> getDrivingFeaturesDiscrete(1:string problem, 2:list<int> behavioral, 3:list<int> non_behavioral, 4:list<DiscreteInputArchitecture> all_archs, 5:double supp, 6:double conf, 7:double lift),
   
   list<Feature> getMarginalDrivingFeaturesDiscrete(1:string problem, 2:list<int> behavioral, 3:list<int> non_behavioral, 4:list<DiscreteInputArchitecture> all_archs, 5:string featureExpression, 6:string logical_connective, 7:double supp, 8:double conf, 9:double lift),

   list<Feature> getDrivingFeaturesEpsilonMOEADiscrete(1:string problem, 2:list<int> behavioral, 3:list<int> non_behavioral, 4:list<DiscreteInputArchitecture> all_archs),





   // Continuous Input
   list<Feature> getDrivingFeaturesContinuous(1:string problem, 2:list<int> behavioral, 3:list<int> non_behavioral, 4:list<ContinuousInputArchitecture> all_archs, 5:double supp, 6:double conf, 7:double lift),
   
   list<Feature> getDrivingFeaturesEpsilonMOEAContinuous(1:string problem, 2:list<int> behavioral, 3:list<int> non_behavioral, 4:list<ContinuousInputArchitecture> all_archs),






   // Generalization
   list<Feature> generalizeFeatureBinary(1:string problem, 2:string session, 3:list<int> behavioral, 4:list<int> non_behavioral, 5:list<BinaryInputArchitecture> all_archs, 6:string rootfeatureExpression, 7:string nodeFeatureExpression),

   list<Feature> getDrivingFeaturesWithGeneralizationBinary(1:string problem, 2:list<int> behavioral, 3:list<int> non_behavioral, 4:list<BinaryInputArchitecture> all_archs),

   string simplifyFeatureExpression(1:string problem, 2:string expression),





   // Stop search 
   int stopSearch(1:string session),





   // Logical operations
   list<double> computeComplexityOfFeatures(1:string problem, 2:list<string> expressions),
   list<int> computeAlgebraicTypicality(1:string problem, 2:BinaryInputArchitecture arch, 3:string feature),
   double computeComplexity(1:string problem, 2:string expression),
   string convertToCNF(1:string expression),
   string convertToDNF(1:string expression),




   // Setter/getters for problem-specific information
   bool setAssigningProblemEntities(1:string problem, 2:AssigningProblemEntities entities),
   bool setAssigningProblemGeneralizedConcepts(1:string problem, 2:AssigningProblemEntities generalizedConcepts),
   AssigningProblemEntities getAssigningProblemEntities(1:string problem),
   FlattenedConceptHierarchy getAssigningProblemConceptHierarchy(1:string problem, 2:AssigningProblemEntities params),



   // Temporary methods specific for IDETC2018 paper data analysis
   list<int> computeAlgebraicTypicalityWithStringInput(1:string problem, 2:string architecture, 3:string feature),
}
