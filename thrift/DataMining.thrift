


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


struct DrivingFeature{
  1: int id,
  2: string name,
  3: string expression,
  4: list<double> metrics
}

struct Architecture{
  1: int id,
  2: string bitString,
  3: double science,
  4: double cost
}


service DataMiningInterface{
   
   void ping(),

   list<DrivingFeature> getDrivingFeatures(1:list<int> behavioral, 2:list<int> non_behavioral, 3:list<Architecture> all_archs, 4:double supp, 5:double conf, 6:double lift)
   
   list<DrivingFeature> getMarginalDrivingFeatures(1:list<int> behavioral, 2:list<int> non_behavioral, 3:list<Architecture> all_archs, 4:list<DrivingFeature> current_features, 5:double supp, 6:double conf, 7:double lift)

}


