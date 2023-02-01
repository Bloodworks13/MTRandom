package DiscreteMurTree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class MT {
    HashMap<DataSet,ArrayList<Quadruple<Integer,Integer,Integer,Integer>>> lowerBoundCache;
    HashMap<DataSet,ArrayList<Quadruple<SpecializedTree,Integer,Integer,Boolean>>> optimalSubtreeCache;
    HashMap<Branch,ArrayList<Quadruple<Integer,Integer,Integer,Integer>>> lowerBoundCacheBranch;
    HashMap<Branch,ArrayList<Quadruple<SpecializedTree,Integer,Integer,Boolean>>> optimalSubtreeCacheBranch;
    HashMap<Integer, DataSet> similarity;
    ArrayList<Quadruple<DataSet,HashMap<Feature,Integer>, HashMap<Feature,Integer>, Pair<HashMap<Pair<Feature, Feature>,Integer>, HashMap<Pair<Feature, Feature>,Integer>>>> specInc;
    boolean branchCache = false;
    public int cacheEntries;
    public int countSolveSubtree;
    public int countGeneralCase;
    public int countSolveSubtreeRoot;
    public int countDepthTwo;
    public int timeSS;
    public int timeG;
    public int timeSSR;
    public int timeD2;

    public MT() {
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
    }

    public SpecializedTree solveSubtree(DataSet D, Branch B, int d, int n, int UB) {
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
            if (leafMisclassification(D) <= UB) {
                return classificationNode(D);
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


        boolean updatedOptimalSolution = updateCacheUsingSimilarity(D,B,d,n);
        if (updatedOptimalSolution) {
            SpecializedTree retTree = retrieveOptimalSubtreeFromCache(D,B,d,n);
            if (Misclassifications(retTree) <= UB) {
                return retTree;
            }
            else {
                return null;
             }
        }

        int LB = retrieveLowerBoundFromCache(D,B,d,n);
        if (LB > UB) {
            return null;
        }

        if (LB == leafMisclassification(D)) {
            return classificationNode(D);
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

    //STILL NEEDS WORK: SIMILARITY BOUND
    public SpecializedTree generalCase(DataSet D, Branch B, int d, int n, int UB) {
        countGeneralCase++;
        long time = System.currentTimeMillis();
        SpecializedTree treeBest = classificationNode(D);
        if (leafMisclassification(D) > UB) {
            treeBest = null;
        }

        int LB = retrieveLowerBoundFromCache(D,B,d,n);
        int RLB = Integer.MAX_VALUE;

        int nMax = Math.min((int) (Math.pow(2,d-1) - 1), n-1);
        int nMin = n - 1 - nMax;


        ArrayList<Feature> features = D.getFeatureList();

        for (Feature f : features) {
            for (int x = 0; x < f.getMaxValue(); x++) {
                Feature splitF = new Feature(f.getName(), x, f.index, f.getMaxValue());
                if (Misclassifications(treeBest) == LB) {
                    break;
                }
                int fAmount = D.splitOnFeature(splitF).getOne().size();
                if (fAmount == 0 || fAmount == D.size()) {
                    continue;
                }
                for (int nl = nMin; nl <= nMax; nl++) {
                    int nr = n - 1 - nl;

                    int UBN = Math.min(UB, Misclassifications(treeBest) - 1);
                    timeG += (System.currentTimeMillis() - time);
                    Pair<SpecializedTree, Integer> treePair = solveSubTreeGivenRootFeature(D,B,splitF,d,nl,nr,UBN);
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

    public Pair<SpecializedTree, Integer> solveSubTreeGivenRootFeature(DataSet D, Branch B, Feature fRoot, int d, int nl, int nr, int UB) {
        long time = System.currentTimeMillis();
        int dl = Math.min(d-1, nl);
        int dr = Math.min(d-1, nr);
        Branch leftBranch = B.newBranchLeft(fRoot);
        Branch rightBranch = B.newBranchRight(fRoot);
        countSolveSubtreeRoot++;
        Pair<DataSet, DataSet> datasetSplitByF = D.splitOnFeature(fRoot);
        if (leafMisclassification(datasetSplitByF.getOne()) > leafMisclassification(datasetSplitByF.getTwo())) {
            DataSet DwithF0 = datasetSplitByF.getOne();
            DataSet DwithF1 = datasetSplitByF.getTwo();
            int UBLeft = UB - retrieveLowerBoundFromCache(DwithF0,rightBranch,dr,nr);
            timeSSR += (System.currentTimeMillis() - time);
            SpecializedTree treeLeft = solveSubtree(DwithF0, leftBranch, dl, nl, UBLeft);
            time = System.currentTimeMillis();

            if (treeLeft == null) {
                int LBLocal = retrieveLowerBoundFromCache(DwithF0,leftBranch,dl,nl) + retrieveLowerBoundFromCache(DwithF1,rightBranch,dr,nr);
                Pair<SpecializedTree, Integer> retPair = new Pair(null, LBLocal);
                timeSSR += (System.currentTimeMillis() - time);
                return retPair;
            }

            int UBRight = UB - Misclassifications(treeLeft);
            timeSSR += (System.currentTimeMillis() - time);
            SpecializedTree treeRight = solveSubtree(DwithF1,rightBranch,dr,nr,UBRight);
            time = System.currentTimeMillis();
            if (treeRight != null) {
                SpecializedTree optTree = new SpecializedTree(D, fRoot, treeLeft, treeRight, null);
                Pair<SpecializedTree, Integer> retPair = new Pair(optTree, Misclassifications(optTree));
                timeSSR += (System.currentTimeMillis() - time);
                return retPair;
            }
            else {
                int LBLocal = retrieveLowerBoundFromCache(DwithF0,leftBranch,dl,nl) + retrieveLowerBoundFromCache(DwithF1,rightBranch,dr,nr);
                Pair<SpecializedTree, Integer> retPair = new Pair(null, LBLocal);
                timeSSR += (System.currentTimeMillis() - time);
                return retPair;
            }
        }
        else {
            DataSet DwithF0 = datasetSplitByF.getOne();
            DataSet DwithF1 = datasetSplitByF.getTwo();
            int UBRight = UB - retrieveLowerBoundFromCache(DwithF1,leftBranch,dl,nl);
            timeSSR += (System.currentTimeMillis() - time);
            SpecializedTree treeRight = solveSubtree(DwithF1, rightBranch, dr, nr, UBRight);
            time = System.currentTimeMillis();
            if (treeRight == null) {
                int LBLocal = retrieveLowerBoundFromCache(DwithF0,leftBranch,dl,nl) + retrieveLowerBoundFromCache(DwithF1,rightBranch,dr,nr);
                Pair<SpecializedTree, Integer> retPair = new Pair(null, LBLocal);
                timeSSR += (System.currentTimeMillis() - time);
                return retPair;
            }

            int UBLeft = UB - Misclassifications(treeRight);
            timeSSR += (System.currentTimeMillis() - time);
            SpecializedTree treeLeft = solveSubtree(DwithF0,leftBranch,dl,nl,UBLeft);
            time = System.currentTimeMillis();
            if (treeLeft != null) {
                SpecializedTree optTree = new SpecializedTree(D, fRoot, treeLeft, treeRight, null);
                Pair<SpecializedTree, Integer> retPair = new Pair(optTree, Misclassifications(optTree));
                timeSSR += (System.currentTimeMillis() - time);
                return retPair;
            }
            else {
                int LBLocal = retrieveLowerBoundFromCache(DwithF0,leftBranch,dl,nl) + retrieveLowerBoundFromCache(DwithF1,rightBranch,dr,nr);
                Pair<SpecializedTree, Integer> retPair = new Pair(null, LBLocal);
                timeSSR += (System.currentTimeMillis() - time);
                return retPair;
            }
        }
    }

    public SpecializedTree specializedDepthTwoAlgorithm(DataSet D, Branch B, int d, int n) {
        long time = System.currentTimeMillis();
        ArrayList<Feature> features = D.getFeatureList();
        countDepthTwo++;
        HashMap<Feature, Integer> FQPlus = new HashMap<>();
        HashMap<Feature, Integer> FQMinus = new HashMap<>();
        HashMap<Pair<Feature, Feature>, Integer> FQPairPlus = new HashMap<>();
        HashMap<Pair<Feature, Feature>, Integer> FQPairMinus = new HashMap<>();
        DataSet DMinus = D.getDataset(-1);
        DataSet DPlus = D.getDataset(1);


        if(specInc.size() == 0) {
            // Initialize frequency maps to 0;
            for (int x = 0; x < features.size(); x++) {
                Feature fi = features.get(x);
                FQPlus.put(fi, 0);
                FQMinus.put(fi, 0);

                for (int y = x+1; y < features.size(); y++) {
                    Feature fj = features.get(y);
                    Pair<Feature, Feature> pair = new Pair<>(fi,fj);
                    FQPairPlus.put(new Pair<>(fi, fj),0);
                    FQPairMinus.put(new Pair<>(fi, fj),0);
                }

            }

            // Calculate frequency count for D+
            for(DataPoint dTemp : DPlus.getDataPoints()) {
                ArrayList<Feature> fv = dTemp.getFeatureListPositive();
                for(int x = 0; x < fv.size(); x++) {
                    Feature fi = fv.get(x);
                    FQPlus.put(fi, FQPlus.get(fi)+1);

                    for(int y = x+1; y < fv.size(); y++) {
                        Feature fj = fv.get(y);

                        Pair<Feature, Feature> tempPair = new Pair<>(fi, fj);
                        FQPairPlus.put(tempPair, FQPairPlus.get(tempPair)+1);
                    }
                }
            }

            // Calculate frequency count for D-
            for(DataPoint dTemp : DMinus.getDataPoints()) {
                ArrayList<Feature> fv = dTemp.getFeatureListPositive();
                for(int x = 0; x < fv.size(); x++) {
                    Feature fi = fv.get(x);
                    FQMinus.put(fi, FQMinus.get(fi)+1);

                    for(int y = x+1; y < fv.size(); y++) {
                        Feature fj = fv.get(y);

                        Pair<Feature, Feature> tempPair = new Pair<>(fi, fj);
                        FQPairMinus.put(tempPair, FQPairMinus.get(tempPair)+1);
                    }
                }
            }
        }
        else {
            Quadruple<DataSet,HashMap<Feature,Integer>, HashMap<Feature,Integer>, Pair<HashMap<Pair<Feature, Feature>,Integer>, HashMap<Pair<Feature, Feature>,Integer>>> specIncBest = specInc.get(0);
            int minDiff = Integer.MAX_VALUE;

            for (Quadruple<DataSet,HashMap<Feature,Integer>, HashMap<Feature,Integer>, Pair<HashMap<Pair<Feature, Feature>,Integer>, HashMap<Pair<Feature, Feature>,Integer>>> tup : specInc) {
                HashSet<DataPoint> dNew = D.getDataPoints();
                HashSet<DataPoint> dOld = tup.getOne().getDataPoints();
                HashSet<DataPoint> dSame = (HashSet<DataPoint>) dNew.clone();
                dSame.retainAll(dOld);
                int diff = (dNew.size() - dSame.size()) + (dOld.size() - dSame.size());
                if (diff < minDiff) {
                    minDiff = diff;
                    specIncBest = tup;

                }
            }

            DataSet dOld = specIncBest.getOne();
            DataSet dOut = dOld.setExclude(D);
            DataSet dIn = D.setExclude(dOld);
            FQPlus = (HashMap<Feature,Integer>) specIncBest.getTwo().clone();
            FQMinus = (HashMap<Feature,Integer>) specIncBest.getThree().clone();
            FQPairPlus = (HashMap<Pair<Feature, Feature>,Integer>) specIncBest.getFour().getOne().clone();
            FQPairMinus = (HashMap<Pair<Feature, Feature>,Integer>) specIncBest.getFour().getTwo().clone();

            long time2 = System.currentTimeMillis();
            for (DataPoint dTemp : dOut.hasLabel("+", false).getDataPoints()) {
                ArrayList<Feature> fv = dTemp.getFeatureListPositive();

                for(int x = 0; x < fv.size(); x++) {

                    Feature fi = fv.get(x);
                    FQPlus.put(fi, FQPlus.get(fi)-1);

                    for(int y = x+1; y < fv.size(); y++) {

                        Feature fj = fv.get(y);

                        Pair<Feature, Feature> tempPair = new Pair<>(fi, fj);
                        FQPairPlus.put(tempPair, FQPairPlus.get(tempPair)-1);

                    }
                }

            }

            for (DataPoint dTemp : dIn.hasLabel("+", false).getDataPoints()) {
                ArrayList<Feature> fv = dTemp.getFeatureListPositive();
                for(int x = 0; x < fv.size(); x++) {
                    Feature fi = fv.get(x);
                    FQPlus.put(fi, FQPlus.get(fi)+1);

                    for(int y = x+1; y < fv.size(); y++) {
                        Feature fj = fv.get(y);

                        Pair<Feature, Feature> tempPair = new Pair<>(fi, fj);
                        FQPairPlus.put(tempPair, FQPairPlus.get(tempPair)+1);
                    }
                }
            }

            for (DataPoint dTemp : dOut.hasLabel("-", false).getDataPoints()) {
                ArrayList<Feature> fv = dTemp.getFeatureListPositive();
                for(int x = 0; x < fv.size(); x++) {
                    Feature fi = fv.get(x);
                    FQMinus.put(fi, FQMinus.get(fi)-1);

                    for(int y = x+1; y < fv.size(); y++) {
                        Feature fj = fv.get(y);

                        Pair<Feature, Feature> tempPair = new Pair<>(fi, fj);
                        FQPairMinus.put(tempPair, FQPairMinus.get(tempPair)-1);
                    }
                }
            }

            for (DataPoint dTemp : dIn.hasLabel("-", false).getDataPoints()) {
                ArrayList<Feature> fv = dTemp.getFeatureListPositive();
                for(int x = 0; x < fv.size(); x++) {
                    Feature fi = fv.get(x);
                    FQMinus.put(fi, FQMinus.get(fi)+1);

                    for(int y = x+1; y < fv.size(); y++) {
                        Feature fj = fv.get(y);

                        Pair<Feature, Feature> tempPair = new Pair<>(fi, fj);
                        FQPairMinus.put(tempPair, FQPairMinus.get(tempPair)+1);
                    }
                }
            }
        }
        SpecializedTree[] bestLeftSubtree = new SpecializedTree[features.size()];
        SpecializedTree[] bestRightSubtree = new SpecializedTree[features.size()];

        for(int z = 0; z < features.size(); z++) {
            int size = D.getDataPoints().size();
            Pair<DataSet, DataSet> dPair = D.splitOnFeature(features.get(z),0);
            bestLeftSubtree[z] = new SpecializedTree(dPair.getOne(), null, null, null, null);
            bestLeftSubtree[z].setMisclassifications(size);
            bestRightSubtree[z] = new SpecializedTree(dPair.getTwo(), null, null, null, null);
            bestRightSubtree[z].setMisclassifications(size);
        }

        for (int x = 0; x < features.size(); x++) {
            for (int y = 0; y < features.size(); y++) {
                if (x == y) {
                    continue;
                }

                Feature fi = features.get(x);
                Feature fj = features.get(y);

                Pair<Feature, Feature> fPair;

                if (y < x) {
                    fPair = new Pair<>(fj, fi);
                }
                else {
                    fPair = new Pair<>(fi, fj);
                }


                int plnr = FQPlus.get(fi) - FQPairPlus.get(fPair);
                int pnlr = FQPlus.get(fj) - FQPairPlus.get(fPair);
                int pnlnr = DPlus.getDataPoints().size() - FQPlus.get(fi) - FQPlus.get(fj) + FQPairPlus.get(fPair);
                int mlnr = FQMinus.get(fi) - FQPairMinus.get(fPair);
                int mnlr = FQMinus.get(fj) - FQPairMinus.get(fPair);
                int mnlnr = DMinus.getDataPoints().size() - FQMinus.get(fi) - FQMinus.get(fj) + FQPairMinus.get(fPair);

                // values for left
                int CSNotIJ = Math.min(pnlr, mnlr);
                int CSNotINotJ = Math.min(pnlnr, mnlnr);

                int MSLeft = CSNotIJ + CSNotINotJ;

                // values for right
                int CSINotJ = Math.min(plnr, mlnr);
                int CSIJ = Math.min(FQPairPlus.get(fPair), FQPairMinus.get(fPair));

                int MSRight = CSINotJ + CSIJ;


                // LEFT SUBTREE

                if (bestLeftSubtree[x].getMisclassifications() > MSLeft) {
                    bestLeftSubtree[x].setMisclassifications(MSLeft);
                    bestLeftSubtree[x].setFeature(new Feature(fj.getName(), 0, fj.index, fj.getMaxValue()));
                }

                // RIGHT SUBTREE

                if (bestRightSubtree[x].getMisclassifications() > MSRight) {
                    bestRightSubtree[x].setMisclassifications(MSRight);
                    bestRightSubtree[x].setFeature(new Feature(fj.getName(), 0, fj.index, fj.getMaxValue()));
                }
            }
        }

        // Calculate best feature split
        int minMiss = Integer.MAX_VALUE;
        int minInd = -1;
        for (int i = 0; i < features.size(); i++) {
            if (minMiss > bestLeftSubtree[i].getMisclassifications() + bestRightSubtree[i].getMisclassifications()) {
                minMiss = bestLeftSubtree[i].getMisclassifications() + bestRightSubtree[i].getMisclassifications();
                minInd = i;
            }
        }

        bestLeftSubtree[minInd].split();
        bestRightSubtree[minInd].split();
        Feature split = features.get(minInd);
        SpecializedTree returnTree = new SpecializedTree(D,new Feature(split.getName(),0,split.index,split.getMaxValue()), bestLeftSubtree[minInd], bestRightSubtree[minInd], null);
        returnTree.setMisclassifications(minMiss);

        storeOptimalSubtreeInCache(returnTree,D,B,d,n);
        storeInc(new Quadruple<>(D, FQPlus, FQMinus, new Pair<>(FQPairPlus, FQPairMinus)));
        timeD2 += (System.currentTimeMillis() - time);
        return returnTree;
    }

    public static int leafMisclassification(DataSet D) {
        int sizeMinus = D.getDataset(-1).size();
        return Math.min(D.getDataPoints().size()-sizeMinus,sizeMinus);
    }

    public static int Misclassifications(SpecializedTree T) {
        if (T == null) {
            return Integer.MAX_VALUE;
        }
        else if (T.getMisclassifications() != Integer.MAX_VALUE) {
            return T.getMisclassifications();
        }
        else {
            if (T.getLabel() == null) {
                int misses = Misclassifications(T.getLeftTree()) + Misclassifications(T.getRightTree());
                T.setMisclassifications(misses);
                return misses;
            }
            else {
                return T.getMisclassifications();
            }
        }
    }

    public static SpecializedTree classificationNode(DataSet D) {
        DataSet DMinus = D.getDataset(-1);
        DataSet DPlus = D.getDataset(1);
        String label = ((DMinus.size() < DPlus.size()) ? "+" : "-");
        SpecializedTree retTree = new SpecializedTree(D,null,null,null,label);
        retTree.setMisclassifications(leafMisclassification(D));
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

    public void storeInc(Quadruple<DataSet,HashMap<Feature,Integer>, HashMap<Feature,Integer>, Pair<HashMap<Pair<Feature, Feature>,Integer>, HashMap<Pair<Feature, Feature>,Integer>>> tup) {
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

    public int retrieveLowerBoundFromCache(DataSet D, Branch B, int d, int n) {
        if(branchCache) {
            return retrieveLowerBoundFromCacheBranch(D,B,d,n);
        }
        if (!lowerBoundCache.containsKey(D)) {
            return 0;
        }
        int bestLowerBound = 0;
        for(Quadruple<Integer,Integer,Integer,Integer> entry : lowerBoundCache.get(D)) {
            if(n <= entry.getThree() && d <= entry.getTwo()) {
                int localLowerBound = entry.getOne();
                bestLowerBound = Math.max(bestLowerBound, localLowerBound);
            }
        }
        return bestLowerBound;
    }

    public int retrieveLowerBoundFromCacheBranch(DataSet D, Branch B, int d, int n) {
        if (!lowerBoundCacheBranch.containsKey(B)) {
            return 0;
        }
        int bestLowerBound = 0;
        for(Quadruple<Integer,Integer,Integer,Integer> entry : lowerBoundCacheBranch.get(B)) {
            if(n <= entry.getThree() && d <= entry.getTwo()) {
                int localLowerBound = entry.getOne();
                bestLowerBound = Math.max(bestLowerBound, localLowerBound);
            }
        }
        return bestLowerBound;
    }

    public void storeLowerBoundInCache(DataSet D, Branch B, int d, int n, int LB) {
        if(branchCache) {
            storeLowerBoundInCacheBranch(D,B,d,n,LB);
            return;
        }
        if(!lowerBoundCache.containsKey(D)) {
            ArrayList<Quadruple<Integer,Integer,Integer,Integer>> list = new ArrayList<>();
            list.add(new Quadruple<>(LB,d,n,0));
            lowerBoundCache.put(D,list);
        }
        else {
            boolean foundEntry = false;

            for (Quadruple<Integer,Integer,Integer,Integer> entry : lowerBoundCache.get(D)) {
                if (entry.getTwo() == d && entry.getThree() == n) {
                    entry.setOne(LB);
                    foundEntry = true;
                    break;
                }
            }

            if (!foundEntry) {
                ArrayList<Quadruple<Integer,Integer,Integer,Integer>> tup = lowerBoundCache.get(D);
                tup.add(new Quadruple<Integer,Integer,Integer,Integer>(LB, d, n, 0));
                lowerBoundCache.put(D, tup);
            }
        }
    }

    public void storeLowerBoundInCacheBranch(DataSet D, Branch B, int d, int n, int LB) {
        if(!lowerBoundCacheBranch.containsKey(D)) {
            ArrayList<Quadruple<Integer,Integer,Integer,Integer>> list = new ArrayList<>();
            list.add(new Quadruple<>(LB,d,n,0));
            lowerBoundCacheBranch.put(B,list);
        }
        else {
            boolean foundEntry = false;

            for (Quadruple<Integer,Integer,Integer,Integer> entry : lowerBoundCacheBranch.get(B)) {
                if (entry.getTwo() == d && entry.getThree() == n) {
                    entry.setOne(LB);
                    foundEntry = true;
                    break;
                }
            }

            if (!foundEntry) {
                ArrayList<Quadruple<Integer,Integer,Integer,Integer>> tup = lowerBoundCacheBranch.get(B);
                tup.add(new Quadruple<Integer,Integer,Integer,Integer>(LB, d, n, 0));
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

        int lowerBoundEntry = retrieveLowerBoundFromCache(entryD,B,d,n);
        if (entryD.size() > D.size() && entryD.size() - D.size() >= lowerBoundEntry) {
            return result;
        }
        HashSet<DataPoint> dOld = new HashSet<>(entryD.getDataPoints());
        HashSet<DataPoint> dNew = new HashSet<>(D.getDataPoints());
        HashSet<DataPoint> dOut = (HashSet<DataPoint>) dOld.clone();
        HashSet<DataPoint> dIn = (HashSet<DataPoint>) dNew.clone();

        dOut.removeAll(dNew);
        dIn.removeAll(dOut);

        result.setTwo(Math.max(result.getTwo(), lowerBoundEntry - dOut.size()));

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
