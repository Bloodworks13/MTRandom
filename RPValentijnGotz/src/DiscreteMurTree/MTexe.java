package DiscreteMurTree;

import java.io.File;

public class MTexe {
    public static void main(String[] args) {
        DSParser dsp = new DSParser();
        File file = new File("Datasets/datasetsFromMurtree/datasetsDL/anneal.txt");
        DataSet d = dsp.parseFile(file);
        MT m = new MT();


        long time = System.currentTimeMillis();

        SpecializedTree t = m.solveSubtree(d,new Branch(),4,15,Integer.MAX_VALUE);

        time = (System.currentTimeMillis() - time);

        System.out.println("Total runtime: " + time);
        System.out.println("Cache entries: " + m.cacheEntries);
        System.out.println("Amount of times subroutine is called:   " + "sst: " + m.countSolveSubtree + " general:" + m.countGeneralCase + " sstr: " + m.countSolveSubtreeRoot + " sd2:" + m.countDepthTwo);
        System.out.println("Time spent in subroutine:  " + "timesst: " + m.timeSS + " timegeneral:" + m.timeG + " timesstr: " + m.timeSSR + " timesd2:" + m.timeD2);
        System.out.println(t.toString());
    }
}
