import java.io.File;
import java.util.ArrayList;

public class MTexe {
    public static void main(String[] args) {
        DSParser dsp = new DSParser();
        File file = new File("Datasets/datasetsFromMurtree/datasetsDL/anneal.txt");
        File file2 = new File("Datasets/Processed/anneal.txt");
        double spread = 1;
        DataSet d = dsp.parseFileDiscrete(file2, spread);
        Pair<DataSet, DataSet> dataSplit = d.splitOnPercentage(0.75);
        MT m = new MT(spread);
        System.out.println(dataSplit.getTwo());


        long time = System.currentTimeMillis();

        System.out.println(dataSplit.getOne().size());

        SpecializedTree t = m.solveSubtree(dataSplit.getOne(),new Branch(),2, 15,Integer.MAX_VALUE);

        double avg = t.evaluateDataset(dataSplit.getTwo());
        for(int x = 0; x < 1000; x++) {
            avg += t.evaluateDataset(dataSplit.getTwo());
        }
        avg = avg / 1001;

        System.out.println("eval once: " +t.evaluateDataset(dataSplit.getTwo()));
        System.out.println("avg: " +avg);
        System.out.println("attack: " + t.evaluateDatasetWithAttack(dataSplit.getTwo(), ""));

        time = (System.currentTimeMillis() - time);

        System.out.println("Total runtime: " + time);
        System.out.println("Cache entries: " + m.cacheEntries);
        System.out.println("Amount of times subroutine is called:   " + "sst: " + m.countSolveSubtree + " general:" + m.countGeneralCase + " sstr: " + m.countSolveSubtreeRoot + " sd2:" + m.countDepthTwo);
        System.out.println("Time spent in subroutine:  " + "timesst: " + m.timeSS + " timegeneral:" + m.timeG + " timesstr: " + m.timeSSR + " timesd2:" + m.timeD2);
        System.out.println(t.toString());
    }
}
