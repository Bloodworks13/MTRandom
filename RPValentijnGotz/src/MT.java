import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class MT {
    HashMap<DataSet,ArrayList<Quadruple<Integer,Integer,Integer,Integer>>> lowerBoundCache;
    HashMap<DataSet,ArrayList<Quadruple<SpecializedTree,Integer,Integer,Boolean>>> optimalSubtreeCache;
    HashMap<Branch,ArrayList<Quadruple<Double,Integer,Integer,Integer>>> lowerBoundCacheBranch;
    HashMap<Branch,ArrayList<Quadruple<SpecializedTree,Integer,Integer,Boolean>>> optimalSubtreeCacheBranch;
    HashMap<Integer,DataSet> similarity;
    ArrayList<Quadruple<DataSet,HashMap<Feature,Integer>, HashMap<Feature,Integer>,Pair<HashMap<Pair<Feature,Feature>,Integer>, HashMap<Pair<Feature,Feature>,Integer>>>> specInc;
    boolean branchCache = true;
    public int cacheEntries;
    public int countSolveSubtree;
    public int countGeneralCase;
    public int countSolveSubtreeRoot;
    public int countDepthTwo;
    public int timeSS;
    public int timeG;
    public int timeSSR;
    public int timeD2;
    public static double spread = 12;

    public MT(double spread) {
        lowerBoundCache = new HashMap();
        optimalSubtreeCache = new HashMap();
        lowerBoundCacheBranch = new HashMap();
        optimalSubtreeCacheBranch = new HashMap();

        similarity = new HashMap<>();
        countSolveSubtree = 0;
        countDepthTwo = 0;
        countSolveSubtreeRoot = 0;
        countGeneralCase = 0;
        timeSS = 0;
        timeG = 0;
        timeSSR = 0;
        timeD2 = 0;
        cacheEntries = 0;
        specInc = new ArrayList<>();
        this.spread = spread;
    }

    public SpecializedTree solveSubtree(DataSet D, Branch B, int d, int n, double UB) {
        long time = System.currentTimeMillis();
        if (n > (int) Math.pow(2,d) - 1) {
            n = (int) Math.pow(2,d) - 1;
        }

        if (d > n) {
            d = n;
        }

        countSolveSubtree++;

        if (UB < 0) {
            return null;
        }

        if (d == 0 || n == 0) {
            if (leafMisclassificationRandom(D,B) <= UB) {
                return classificationNodeRandom(D,B);
            }
            else {
                return null;
            }
        }

        if (isOptimalSubtreeInCache(D,B,d,n)) {
            SpecializedTree retTree = retrieveOptimalSubtreeFromCache(D,B,d,n);
            if (Misclassifications(retTree) <= UB) {
                return retTree;
            }
            else {
                return null;
            }
        }


//        boolean updatedOptimalSolution = updateCacheUsingSimilarity(D,B,d,n);
//        if (updatedOptimalSolution) {
//            SpecializedTree retTree = retrieveOptimalSubtreeFromCache(D,B,d,n);
//            if (Misclassifications(retTree) <= UB) {
//                return retTree;
//            }
//            else {
//                return null;
//             }
//        }

        double LB = retrieveLowerBoundFromCache(D,B,d,n);
        if (LB > UB) {
            return null;
        }

        if (LB == leafMisclassificationRandom(D, B)) {
            return classificationNodeRandom(D, B);
        }

        timeSS += (System.currentTimeMillis() - time);
//        if (d <= 2) {
//            SpecializedTree retTree = specializedDepthTwoAlgorithm(D, B, d, n);
//            if (Misclassifications(retTree) <= UB) {
//                return retTree;
//            }
//            else {
//                return null;
//            }
//        }

        return generalCase(D,B,d,n,UB);
    }

    public SpecializedTree generalCase(DataSet D, Branch B, int d, int n, double UB) {
        countGeneralCase++;
        long time = System.currentTimeMillis();
        SpecializedTree treeBest = classificationNodeRandom(D, B);
        if (leafMisclassificationRandom(D,B) > UB) {
            treeBest = null;
        }

        double LB = retrieveLowerBoundFromCache(D,B,d,n);
        double RLB = Double.MAX_VALUE;

        int nMax = Math.min((int) (Math.pow(2,d-1) - 1), n-1);
        int nMin = n - 1 - nMax;


        ArrayList<Feature> features = D.getFeatureList();

        for (Feature f : features) {
            for (int x = 0; x < f.getMaxValue(); x++) {
                Feature splitF = new Feature(f.getName(), x, f.index, f.getMaxValue(), f.getSpread());
                if (Misclassifications(treeBest) == LB) {
                    break;
                }
                int fAmount = D.splitOnFeature(splitF).getOne().size();
                if (fAmount == 0 || fAmount == D.size()) {
                    continue;
                }
                for (int nl = nMin; nl <= nMax; nl++) {
                    int nr = n - 1 - nl;

                    double UBN = Math.min(UB, Misclassifications(treeBest) - 1);
                    timeG += (System.currentTimeMillis() - time);
                    Pair<SpecializedTree, Double> treePair = solveSubTreeGivenRootFeature(D,B,splitF,d,nl,nr,UBN);
                    time = System.currentTimeMillis();
                    if (treePair.getOne() != null) {
                        treeBest = treePair.getOne();
                    }
                    else {
                        RLB = Math.min(RLB, treePair.getTwo());
                    }
                }
            }
        }

        if (Misclassifications(treeBest) <= UB) {
             storeOptimalSubtreeInCache(treeBest,D,B,d,n);
        }
        else {
            LB = Math.max(LB, UB+1);
            if (RLB < Integer.MAX_VALUE) {
                LB = Math.max(LB, RLB);
            }
            storeLowerBoundInCache(D,B,d,n,LB);
        }
        replaceDatasetForSimilarityBound(D,B,d);
        timeG += (System.currentTimeMillis() - time);
        return treeBest;
    }

    public Pair<SpecializedTree, Double> solveSubTreeGivenRootFeature(DataSet D, Branch B, Feature fRoot, int d, int nl, int nr, double UB) {
        long time = System.currentTimeMillis();
        int dl = Math.min(d-1, nl);
        int dr = Math.min(d-1, nr);
        Branch leftBranch = B.newBranchLeft(fRoot);
        Branch rightBranch = B.newBranchRight(fRoot);
        countSolveSubtreeRoot++;
//        Pair<DataSet, DataSet> datasetSplitByF = D.splitOnFeature(fRoot);
        if (leafMisclassificationRandom(D,leftBranch) > leafMisclassificationRandom(D, rightBranch)) {
//            DataSet DwithF0 = datasetSplitByF.getOne();
//            DataSet DwithF1 = datasetSplitByF.getTwo();
            double UBLeft = UB - retrieveLowerBoundFromCache(D,rightBranch,dr,nr);
            timeSSR += (System.currentTimeMillis() - time);
            SpecializedTree treeLeft = solveSubtree(D, leftBranch, dl, nl, UBLeft);
            time = System.currentTimeMillis();

            if (treeLeft == null) {
                double LBLocal = retrieveLowerBoundFromCache(D,leftBranch,dl,nl) + retrieveLowerBoundFromCache(D,rightBranch,dr,nr);
                Pair<SpecializedTree, Double> retPair = new Pair(null, LBLocal);
                timeSSR += (System.currentTimeMillis() - time);
                return retPair;
            }

            double UBRight = UB - Misclassifications(treeLeft);
            timeSSR += (System.currentTimeMillis() - time);
            SpecializedTree treeRight = solveSubtree(D,rightBranch,dr,nr,UBRight);
            time = System.currentTimeMillis();
            if (treeRight != null) {
                SpecializedTree optTree = new SpecializedTree(D, fRoot, treeLeft, treeRight, null, spread);
                Pair<SpecializedTree, Double> retPair = new Pair(optTree, Misclassifications(optTree));
                timeSSR += (System.currentTimeMillis() - time);
                return retPair;
            }
            else {
                double LBLocal = retrieveLowerBoundFromCache(D,leftBranch,dl,nl) + retrieveLowerBoundFromCache(D,rightBranch,dr,nr);
                Pair<SpecializedTree, Double> retPair = new Pair(null, LBLocal);
                timeSSR += (System.currentTimeMillis() - time);
                return retPair;
            }
        }
        else {
//            DataSet DwithF0 = datasetSplitByF.getOne();
//            DataSet DwithF1 = datasetSplitByF.getTwo();
            double UBRight = UB - retrieveLowerBoundFromCache(D,leftBranch,dl,nl);
            timeSSR += (System.currentTimeMillis() - time);
            SpecializedTree treeRight = solveSubtree(D, rightBranch, dr, nr, UBRight);
            time = System.currentTimeMillis();
            if (treeRight == null) {
                double LBLocal = retrieveLowerBoundFromCache(D,leftBranch,dl,nl) + retrieveLowerBoundFromCache(D,rightBranch,dr,nr);
                Pair<SpecializedTree, Double> retPair = new Pair(null, LBLocal);
                timeSSR += (System.currentTimeMillis() - time);
                return retPair;
            }

            double UBLeft = UB - Misclassifications(treeRight);
            timeSSR += (System.currentTimeMillis() - time);
            SpecializedTree treeLeft = solveSubtree(D,leftBranch,dl,nl,UBLeft);
            time = System.currentTimeMillis();
            if (treeLeft != null) {
                SpecializedTree optTree = new SpecializedTree(D, fRoot, treeLeft, treeRight, null, spread);
                Pair<SpecializedTree, Double> retPair = new Pair(optTree, Misclassifications(optTree));
                timeSSR += (System.currentTimeMillis() - time);
                return retPair;
            }
            else {
                double LBLocal = retrieveLowerBoundFromCache(D,leftBranch,dl,nl) + retrieveLowerBoundFromCache(D,rightBranch,dr,nr);
                Pair<SpecializedTree, Double> retPair = new Pair(null, LBLocal);
                timeSSR += (System.currentTimeMillis() - time);
                return retPair;
            }
        }
    }

    public static double leafMisclassification(DataSet D) {
        int sizeMinus = D.getDataset(-1).size();
        return Math.min(D.getDataPoints().size()-sizeMinus,sizeMinus);
    }

    public static double leafMisclassificationRandom(DataSet D, Branch B) {
        HashMap<String, Double> labelProb = new HashMap<>();
        for(String l : D.getLabels()) {
            labelProb.put(l, 0.0);
        }
        for(DataPoint d : D.getDataPoints()) {
            double prob = 1;
            for(Feature par : B.getBranchLeft()) {
                int value = d.getFeatureList().get(par.getIndex()).getValue();
                prob *= par.getProbability(value);
            }
            for(Feature par : B.getBranchRight()) {
                int value = d.getFeatureList().get(par.getIndex()).getValue();
                prob *= (1-par.getProbability(value));
            }
            labelProb.put(d.getLabel(), labelProb.get(d.getLabel())+prob);
        }

        double max = Double.MIN_VALUE;
        double sum = 0.0;
        String bestLabel = "";
        for(String l : D.getLabels()) {
            sum += labelProb.get(l);
            if(labelProb.get(l) > max) {
                max = labelProb.get(l);
                bestLabel = l;
            }
        }

        return sum - max;
    }




    public static double Misclassifications(SpecializedTree T) {
        if (T == null) {
            return Double.MAX_VALUE;
        }
        else {
            return T.getMisclassifications();
        }
    }

//    public static SpecializedTree classificationNode(DataSet D) {
//        DataSet DMinus = D.getDataset(-1);
//        DataSet DPlus = D.getDataset(1);
//        String label = ((DMinus.size() < DPlus.size()) ? "+" : "-");
//        SpecializedTree retTree = new SpecializedTree(D,null,null,null,label);
//        retTree.setMisclassifications(leafMisclassification(D));
//        return retTree;
//    }

    public static SpecializedTree classificationNodeRandom(DataSet D, Branch B) {
        HashMap<String, Double> labelProb = new HashMap<>();
        for(String l : D.getLabels()) {
            labelProb.put(l, 0.0);
        }

        for(DataPoint d : D.getDataPoints()) {
            double prob = 1;
            for(Feature par : B.getBranchLeft()) {
                int value = d.getFeatureList().get(par.getIndex()).getValue();
                prob *= par.getProbability(value);
            }
            for(Feature par : B.getBranchRight()) {
                int value = d.getFeatureList().get(par.getIndex()).getValue();
                prob *= (1-par.getProbability(value));
            }
            labelProb.put(d.getLabel(), labelProb.get(d.getLabel())+prob);
        }

        double max = Double.MIN_VALUE;
        double sum = 0.0;
        String bestLabel = "";
        for(String l : D.getLabels()) {
            sum += labelProb.get(l);
            if(labelProb.get(l) > max) {
                max = labelProb.get(l);
                bestLabel = l;
            }
        }

        SpecializedTree retTree = new SpecializedTree(D,null,null,null,bestLabel, spread);

        retTree.setMisclassifications(sum - max);
        return retTree;
    }

    public boolean isOptimalSubtreeInCache(DataSet D, Branch B, int d, int n) {
        if(branchCache) {
            return isOptimalSubtreeInCacheBranch(D,B,d,n);
        }
        if (optimalSubtreeCache.containsKey(D)) {
            for (Quadruple<SpecializedTree, Integer, Integer, Boolean> entry : optimalSubtreeCache.get(D)) {
                if(entry.getTwo() == d && entry.getThree() == n && entry.getFour()) {
                    return true;
                }
            }
            return false;
        }
        else {
            return false;
        }
    }

    public boolean isOptimalSubtreeInCacheBranch(DataSet D, Branch B, int d, int n) {
        if (optimalSubtreeCacheBranch.containsKey(B)) {
            for (Quadruple<SpecializedTree, Integer, Integer, Boolean> entry : optimalSubtreeCacheBranch.get(B)) {
                if(entry.getTwo() == d && entry.getThree() == n && entry.getFour()) {
                    return true;
                }
            }
            return false;
        }
        else {
            return false;
        }
    }

    public void storeInc(Quadruple<DataSet,HashMap<Feature,Integer>, HashMap<Feature,Integer>,Pair<HashMap<Pair<Feature,Feature>,Integer>, HashMap<Pair<Feature,Feature>,Integer>>> tup) {
        if (specInc.size() < 5) {
            specInc.add(tup);
        }
        else {
            specInc.remove(0);
            specInc.add(tup);
        }
    }

    public SpecializedTree retrieveOptimalSubtreeFromCache(DataSet D, Branch B, int d, int n) {
        if (branchCache) {
            return retrieveOptimalSubtreeFromCacheBranch(D,B,d,n);
        }
        if (!optimalSubtreeCache.containsKey(D)) {
            return null;
        }
        for (Quadruple<SpecializedTree,Integer,Integer,Boolean> entry : optimalSubtreeCache.get(D)) {
            if (entry.getTwo() == d && entry.getThree() == n && entry.getFour()) {
                return entry.getOne();
            }
        }
        return null;
    }

    public SpecializedTree retrieveOptimalSubtreeFromCacheBranch(DataSet D, Branch B, int d, int n) {
        if (!optimalSubtreeCacheBranch.containsKey(B)) {
            return null;
        }
        for (Quadruple<SpecializedTree,Integer,Integer,Boolean> entry : optimalSubtreeCacheBranch.get(B)) {
            if (entry.getTwo() == d && entry.getThree() == n && entry.getFour()) {
                return entry.getOne();
            }
        }
        return null;
    }

    public void storeOptimalSubtreeInCache(SpecializedTree tree, DataSet D, Branch B, int d, int n) {
        if(branchCache) {
            storeOptimalSubtreeInCacheBranch(tree,D,B,d,n);
            return;
        }
        int optimalNodeDepth = Math.min(d, tree.getNumberOfNodes());
        cacheEntries++;
        if(!optimalSubtreeCache.containsKey(D)) {
            ArrayList<Quadruple<SpecializedTree,Integer,Integer,Boolean>> list = new ArrayList<>();
            for(int nodeBudget = tree.getNumberOfNodes(); nodeBudget <= n; nodeBudget++) {
                for(int depthBudget = optimalNodeDepth; depthBudget <= Math.min(d, nodeBudget); depthBudget++) {
                    boolean feasibleTree = false;
                    if (tree != null) {
                        feasibleTree = true;
                    }
                    list.add(new Quadruple<>(tree, depthBudget,nodeBudget, feasibleTree));
                }
            }
            optimalSubtreeCache.put(D,list);
        }
        else {
            boolean[][] budgetSeen = new boolean[n+1][d+1];
            for(Quadruple<SpecializedTree,Integer,Integer,Boolean> entry : optimalSubtreeCache.get(D)) {

                if(tree.getNumberOfNodes() <= entry.getThree() && entry.getThree() <= n && optimalNodeDepth <= entry.getTwo() && entry.getTwo() <= d) {
                    if (!(!entry.getFour() || Misclassifications(entry.getOne()) == Misclassifications(tree))) {

                    }
                    budgetSeen[entry.getThree()][entry.getTwo()] = true;
                    if(entry.getFour() == false && tree != null) {
                        entry.setOne(tree);
                        entry.setFour(true); //check this
                    }
                }
            }

            ArrayList<Quadruple<SpecializedTree,Integer,Integer,Boolean>> list = optimalSubtreeCache.get(D);

            for (int nodeBudget = tree.getNumberOfNodes(); nodeBudget <= n; nodeBudget++) {
                for(int depthBudget = optimalNodeDepth; depthBudget <= Math.min(d, nodeBudget); depthBudget++) {
                    if (!budgetSeen[nodeBudget][depthBudget]) {
                        boolean feasibleTree = false;
                        if (tree != null) {
                            feasibleTree = true;
                        }
                        list.add(new Quadruple<>(tree, depthBudget, nodeBudget, feasibleTree));
                    }
                }
            }
            optimalSubtreeCache.put(D, list);
        }
    }

    public void storeOptimalSubtreeInCacheBranch(SpecializedTree tree, DataSet Dff, Branch B, int d, int n) {
        int optimalNodeDepth = Math.min(d, tree.getNumberOfNodes());
        cacheEntries++;
        if(!optimalSubtreeCacheBranch.containsKey(B)) {
            ArrayList<Quadruple<SpecializedTree,Integer,Integer,Boolean>> list = new ArrayList<>();
            for(int nodeBudget = tree.getNumberOfNodes(); nodeBudget <= n; nodeBudget++) {
                for(int depthBudget = optimalNodeDepth; depthBudget <= Math.min(d, nodeBudget); depthBudget++) {
                    boolean feasibleTree = false;
                    if (tree != null) {
                        feasibleTree = true;
                    }
                    list.add(new Quadruple<>(tree, depthBudget,nodeBudget, feasibleTree));
                }
            }
            optimalSubtreeCacheBranch.put(B,list);
        }
        else {
            boolean[][] budgetSeen = new boolean[n+1][d+1];
            for(Quadruple<SpecializedTree,Integer,Integer,Boolean> entry : optimalSubtreeCacheBranch.get(B)) {

                if(tree.getNumberOfNodes() <= entry.getThree() && entry.getThree() <= n && optimalNodeDepth <= entry.getTwo() && entry.getTwo() <= d) {
                    budgetSeen[entry.getThree()][entry.getTwo()] = true;
                    if(entry.getFour() == false && tree != null) {
                        entry.setOne(tree);
                        entry.setFour(true); //check this
                    }
                }
            }

            ArrayList<Quadruple<SpecializedTree,Integer,Integer,Boolean>> list = optimalSubtreeCacheBranch.get(B);

            for (int nodeBudget = tree.getNumberOfNodes(); nodeBudget <= n; nodeBudget++) {
                for(int depthBudget = optimalNodeDepth; depthBudget <= Math.min(d, nodeBudget); depthBudget++) {
                    if (!budgetSeen[nodeBudget][depthBudget]) {
                        boolean feasibleTree = false;
                        if (tree != null) {
                            feasibleTree = true;
                        }
                        list.add(new Quadruple<>(tree, depthBudget, nodeBudget, feasibleTree));
                    }
                }
            }
            optimalSubtreeCacheBranch.put(B, list);
        }
    }

    public double retrieveLowerBoundFromCache(DataSet D, Branch B, int d, int n) {
        if(branchCache) {
            return retrieveLowerBoundFromCacheBranch(D,B,d,n);
        }
        if (!lowerBoundCache.containsKey(D)) {
            return 0;
        }
        double bestLowerBound = 0;
//        for(Quadruple<Integer,Integer,Integer,Integer> entry : lowerBoundCache.get(D)) {
//            if(n <= entry.getThree() && d <= entry.getTwo()) {
//                double localLowerBound = entry.getOne();
//                bestLowerBound = Math.max(bestLowerBound, localLowerBound);
//            }
//        }
        return bestLowerBound;
    }

    public double retrieveLowerBoundFromCacheBranch(DataSet D, Branch B, int d, int n) {
        if (!lowerBoundCacheBranch.containsKey(B)) {
            return 0;
        }
        double bestLowerBound = 0;
        for(Quadruple<Double,Integer,Integer,Integer> entry : lowerBoundCacheBranch.get(B)) {
            if(n <= entry.getThree() && d <= entry.getTwo()) {
                double localLowerBound = entry.getOne();
                bestLowerBound = Math.max(bestLowerBound, localLowerBound);
            }
        }
        return bestLowerBound;
    }

    public void storeLowerBoundInCache(DataSet D, Branch B, int d, int n, double LB) {
        if(branchCache) {
            storeLowerBoundInCacheBranch(D,B,d,n,LB);
            return;
        }
//        if(!lowerBoundCache.containsKey(D)) {
//            ArrayList<Quadruple<Integer,Integer,Integer,Integer>> list = new ArrayList<>();
//            list.add(new Quadruple<>(LB,d,n,0));
//            lowerBoundCache.put(D,list);
//        }
//        else {
//            boolean foundEntry = false;
//
//            for (Quadruple<Integer,Integer,Integer,Integer> entry : lowerBoundCache.get(D)) {
//                if (entry.getTwo() == d && entry.getThree() == n) {
//                    entry.setOne(LB);
//                    foundEntry = true;
//                    break;
//                }
//            }
//
//            if (!foundEntry) {
//                ArrayList<Quadruple<Integer,Integer,Integer,Integer>> tup = lowerBoundCache.get(D);
//                tup.add(new Quadruple<Integer,Integer,Integer,Integer>(LB, d, n, 0));
//                lowerBoundCache.put(D, tup);
//            }
//        }
    }

    public void storeLowerBoundInCacheBranch(DataSet D, Branch B, int d, int n, double LB) {
        if(!lowerBoundCacheBranch.containsKey(D)) {
            ArrayList<Quadruple<Double,Integer,Integer,Integer>> list = new ArrayList<>();
            list.add(new Quadruple<>(LB,d,n,0));
            lowerBoundCacheBranch.put(B,list);
        }
        else {
            boolean foundEntry = false;

            for (Quadruple<Double,Integer,Integer,Integer> entry : lowerBoundCacheBranch.get(B)) {
                if (entry.getTwo() == d && entry.getThree() == n) {
                    entry.setOne(LB);
                    foundEntry = true;
                    break;
                }
            }

            if (!foundEntry) {
                ArrayList<Quadruple<Double,Integer,Integer,Integer>> tup = lowerBoundCacheBranch.get(B);
                tup.add(new Quadruple<Double,Integer,Integer,Integer>(LB, d, n, 0));
                lowerBoundCacheBranch.put(B, tup);
            }
        }
    }


    public boolean updateCacheUsingSimilarity(DataSet D, Branch B, int d, int n) {
        Pair<Boolean, Integer> result = computeLowerBound(D, B, d, n);
        if (result.getOne() == true) {
            return true;
        }
        if (result.getTwo() > 0) {
            storeLowerBoundInCache(D,B,d,n,result.getTwo());
        }
        return false;
    }

    public Pair<Boolean, Integer> computeLowerBound(DataSet D, Branch B, int d, int n) {
        Pair<Boolean, Integer> result = new Pair<>(false, 0);
        if (!similarity.containsKey(d)) {
            return result;
        }

        DataSet entryD = similarity.get(d);

        double lowerBoundEntry = retrieveLowerBoundFromCache(entryD,B,d,n);
        if (entryD.size() > D.size() && entryD.size() - D.size() >= lowerBoundEntry) {
            return result;
        }
        HashSet<DataPoint> dOld = new HashSet<>(entryD.getDataPoints());
        HashSet<DataPoint> dNew = new HashSet<>(D.getDataPoints());
        HashSet<DataPoint> dOut = (HashSet<DataPoint>) dOld.clone();
        HashSet<DataPoint> dIn = (HashSet<DataPoint>) dNew.clone();

        dOut.removeAll(dNew);
        dIn.removeAll(dOut);

//        result.setTwo(Math.max(result.getTwo(), lowerBoundEntry - dOut.size()));

        if(dOut.size() + dIn.size() == 0) {
            transferEquivalentBranches(entryD, B, D, B);
            result.setOne(true);
        }

        return result;
    }

    public void replaceDatasetForSimilarityBound(DataSet D, Branch B, int d) {
        similarity.put(d,D);
    }

    public void transferEquivalentBranches(DataSet D1, Branch B1, DataSet D2, Branch B2) {
        return;
    }
}
