//package ifeed.io;
//
//public class FeatureIO {
//
//    public static void saveSearchMetrics(InstrumentedAlgorithm instAlgorithm, String filename) {
//        Accumulator accum = instAlgorithm.getAccumulator();
//
//        File results = new File(filename + ".res");
//        System.out.println("Saving metrics");
//
//        try (FileWriter writer = new FileWriter(results)) {
//            Set<String> keys = accum.keySet();
//            Iterator<String> keyIter = keys.iterator();
//            while (keyIter.hasNext()) {
//                String key = keyIter.next();
//                int dataSize = accum.size(key);
//                writer.append(key).append(",");
//                for (int i = 0; i < dataSize; i++) {
//                    writer.append(accum.get(key, i).toString());
//                    if (i + 1 < dataSize) {
//                        writer.append(",");
//                    }
//                }
//                writer.append("\n");
//            }
//            writer.flush();
//
//        } catch (IOException ex) {
//            Logger.getLogger(ResultIO.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
//}
