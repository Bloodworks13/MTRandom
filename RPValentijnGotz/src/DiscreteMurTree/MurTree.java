package DiscreteMurTree;//import java.nio.channels.Pipe;
//import java.util.ArrayList;
//import java.util.HashMap;
//
//public class MurTree {
//    ArrayList<Feature> features;
//    DataSet dataSet;
//    String type;
//    int misclassifications;
//    int UB;
//    MurTree leftTree;
//    MurTree rightTree;
//
//
//    public MurTree(DataSet D, MurTree B, int d, int n, int UB) {
//        dataSet = D;
//        this.type = "Feature";
//        leftTree = null;
//        rightTree = null;
//    }
//    public MurTree(DataSet D, MurTree B, int d, int n, int UB, MurTree left, MurTree right) {
//        dataSet = D;
//        this.type = "Feature";
//        leftTree = left;
//        rightTree = right;
//        this.UB = UB;
//    }
//    public MurTree(DataSet D, MurTree B, int d, int n, int UB, String type, int misclassifications) {
//        dataSet = D;
//        this.type = type;
//        this.misclassifications = misclassifications;
//        leftTree = null;
//        rightTree = null;
//    }
//
//    public MurTree solveSubtree(DataSet D, MurTree B, int d, int n, int UB) {
//        if (UB < 0) {
//            return null;
//        }
//
//        if (d == 0 || n == 0) {
//            if (leafMisclassification(D) <= UB) {
//                return classificationNode(D);
//            }
//            else {
//                return null;
//            }
//        }
//
//        if (isOptimalSubtreeInCache(D, B, d, n)) {
//            MurTree T = retrieveOptimalSubtreeFromCache(D, B, d, n);
//            if (misclassifications(T) <= UB) {
//                return T;
//            }
//            else {
//                return null;
//            }
//        }
//
//        boolean updatedOptimalSolution = updateCacheUsingSimilarity(D, B, d, n);
//        if (updatedOptimalSolution) {
//            MurTree T = retrieveOptimalSubtreeFromCache(D, B, d, n);
//            if (misclassifications(T) <= UB) {
//                return T;
//            }
//            else {
//                return null;
//            }
//        }
//
//        int LB = retrieveLowerBoundFromCache(D, B, d, n);
//        if(LB > UB) {
//            return null;
//        }
//
//        if (LB == leafMisclassification(D)) {
//            return classificationNode(D);
//        }
//
////        if (d <= 2) {
////            MurTree T = specializedDepthTwoAlgorithm(D,B,d,n);
////            if (misclassifications(T) <= UB) {
////                return T;
////            }
////            else {
////                return null;
////            }
////        }
//
//        return generalCase(D,B,d,n,UB);
//    }
//
//    public int leafMisclassification(DataSet D) {
//        HashMap<String, Integer> labelCount = new HashMap<String, Integer>();
//        ArrayList<String> labelList = new ArrayList<>();
//        for (DataPoint d : D.getDataPoints()) {
//            if (!labelCount.containsKey(d.getLabel())) {
//                labelCount.put(d.getLabel(),1);
//                labelList.add(d.getLabel());
//            }
//            else {
//                labelCount.put(d.getLabel(), labelCount.get(d.getLabel())+1);
//            }
//        }
//
//        int max = 0;
//
//        for (String l : labelList) {
//            if (max < labelCount.get(l)) {
//                max = labelCount.get(l);
//            }
//        }
//
//        return D.size() - max;
//    }
//
//    public MurTree classificationNode(DataSet D) {
//        HashMap<String, Integer> labelCount = new HashMap<String, Integer>();
//        ArrayList<String> labelList = new ArrayList<>();
//        for (DataPoint d : D.getDataPoints()) {
//            if (!labelCount.containsKey(d.getLabel())) {
//                labelCount.put(d.getLabel(),1);
//                labelList.add(d.getLabel());
//            }
//            else {
//                labelCount.put(d.getLabel(), labelCount.get(d.getLabel())+1);
//            }
//        }
//
//        int max = 0;
//        String maxLabel = null;
//
//        for (String l : labelList) {
//            if (max < labelCount.get(l)) {
//                max = labelCount.get(l);
//                maxLabel = l;
//            }
//        }
//
//        return new MurTree(D.hasLabel(maxLabel, false),null,0,0,0, "Classification", D.size() - max);
//    }
//
//    public boolean isOptimalSubtreeInCache(DataSet D, MurTree B, int d, int n) {
//        return true;
//    }
//
//    public MurTree retrieveOptimalSubtreeFromCache(DataSet D, MurTree B, int d, int n) {
//        return null;
//    }
//
//    public int misclassifications(MurTree T) {
//        if (T.getType() == "Classification") {
//            return T.misclassifications;
//        }
//        return misclassifications(T.leftTree) + misclassifications(T.rightTree);
//    }
//
//    public boolean updateCacheUsingSimilarity(DataSet D, MurTree T, int d, int n) {
//        return true;
//    }
//
//    public int retrieveLowerBoundFromCache(DataSet D, MurTree B, int d, int n) {
//        return 0;
//    }
//
//    public MurTree specializedDepthTwoAlgorithm(DataSet D, MurTree B, int d, int n) {
//        ArrayList<Feature> featureList = D.getDataPoints().get(0).getFeatureList();
//
//        HashMap<Feature, Integer> FQPlus = new HashMap<>();
//        HashMap<Feature, Integer> FQMinus = new HashMap<>();
//        HashMap<Pair<Feature, Feature>, Integer> FQPairPlus = new HashMap<>();
//        HashMap<Pair<Feature, Feature>, Integer> FQPairMinus = new HashMap<>();
//
//        // Initialize frequency maps to 0;
//        for (int x = 0; x < featureList.size(); x++) {
//            Feature fi = featureList.get(x);
//            FQPlus.put(fi, 0);
//            FQMinus.put(fi, 0);
//            for (int y = x+1; y < featureList.size(); y++) {
//                Feature fj = featureList.get(y);
//                FQPairPlus.put(new Pair<>(fi, fj),0);
//                FQPairMinus.put(new Pair<>(fi, fj),0);
//            }
//        }
//
//        // Calculate frequency count for D+
//        for(DataPoint dTemp : D.getDataset(1).getDataPoints()) {
//            ArrayList<Feature> fv = dTemp.getFeatureList();
//            for(int x = 0; x < fv.size(); x++) {
//                Feature fi = fv.get(x);
//                //MAYBE PUT CAN CAUSE PROBLEMS, USE REPLACE INSTEAD?
//                FQPlus.put(fi, FQPlus.get(fi)+1);
//                for(int y = x + 1; y < fv.size(); y++) {
//                    Feature fj = fv.get(y);
//                    Pair<Feature, Feature> tempPair = new Pair<Feature, Feature>(fi, fj);
//                    FQPairPlus.put(tempPair, FQPairPlus.get(tempPair)+1);
//                }
//            }
//        }
//
//        // Calculate frequency count for D-
//        for(DataPoint dTemp : D.getDataset(-1).getDataPoints()) {
//            ArrayList<Feature> fv = dTemp.getFeatureList();
//            for(int x = 0; x < fv.size(); x++) {
//                Feature fi = fv.get(x);
//                //MAYBE PUT CAN CAUSE PROBLEMS, USE REPLACE INSTEAD?
//                FQMinus.put(fi, FQMinus.get(fi)+1);
//                for(int y = x + 1; y < fv.size(); y++) {
//                    Feature fj = fv.get(y);
//                    Pair<Feature, Feature> tempPair = new Pair<Feature, Feature>(fi, fj);
//                    FQPairMinus.put(tempPair, FQPairMinus.get(tempPair)+1);
//                }
//            }
//        }
//
//        MurTree[] bestLeftSubtree = new MurTree[featureList.size()];
//        MurTree[] bestRightSubtree = new MurTree[featureList.size()];
//
//        for (int x = 0; x < featureList.size(); x++) {
//            for (int y = 0; y < featureList.size() && x != y; y++) {
//                // LEFT SUBTREE
//                Feature fi = new Feature(featureList.get(x).getName(), 0);
//                Feature fiBar = new Feature(featureList.get(x).getName(), 0);
//                Feature fj = new Feature(featureList.get(y).getName(), 1);
//                Feature fjBar = new Feature(featureList.get(y).getName(), 0);
//
//                Pair<Feature, Feature> fLeftRight = new Pair<>(fiBar, fj);
//                Pair<Feature, Feature> fLeftLeft = new Pair<>(fiBar, fjBar);
//
//                int CSNotIJ = Math.min(FQPairPlus.get(fLeftRight), FQPairMinus.get(fLeftRight));
//                int CSNotINotJ = Math.min(FQPairPlus.get(fLeftLeft), FQPairMinus.get(fLeftLeft));
//
//                int MSLeft = CSNotIJ + CSNotINotJ;
//                int bestLeft = Integer.MAX_VALUE;
//
//                if (bestLeft > MSLeft) {
//                    bestLeft = MSLeft;
//                }
//
//                // RIGHT SUBTREE
//                Pair<Feature, Feature> fRightLeft = new Pair<>(fi, fjBar);
//                Pair<Feature, Feature> fRightRight = new Pair<>(fi, fj);
//
//                int CSINotJ = Math.min(FQPairPlus.get(fRightLeft), FQPairMinus.get(fRightLeft));
//                int CSIJ = Math.min(FQPairPlus.get(fRightRight), FQPairMinus.get(fRightRight));
//
//                int MSRight = CSINotJ + CSIJ;
//                int bestRight = Integer.MAX_VALUE;
//
//                if (bestRight > MSRight) {
//                    bestRight = MSRight;
//                }
//            }
//        }
//
//        // Calculate best feature split
//        int minMiss = Integer.MAX_VALUE;
//        int minInd = -1;
//        for (int i = 0; i < featureList.size(); i++) {
//            if (minMiss > bestLeftSubtree[i].misclassifications + bestRightSubtree[i].misclassifications) {
//                minMiss = bestLeftSubtree[i].misclassifications + bestRightSubtree[i].misclassifications;
//                minInd = i;
//            }
//        }
//
//        return new MurTree(D,B,d,n,UB,bestLeftSubtree[minInd], bestRightSubtree[minInd]);
//    }
//
//    public MurTree generalCase(DataSet D, MurTree B, int d, int n, int UB) {
//        MurTree TBest = classificationNode(D);
//        if (leafMisclassification(D) > UB) {
//            TBest = null;
//        }
//        int LB = retrieveLowerBoundFromCache(D, B, d, n);
//        int RLB = Integer.MAX_VALUE;
//        int nMax = (int) Math.min(Math.pow(1,d-1)-1, n-1);
//        int nMin = n - 1 - nMax;
//
//        for(Feature f : D.getDataPoints().get(0).getFeatureList()) {
//            if (misclassifications(TBest) == LB) {
//                break;
//            }
////            if (D.hasFeature(f, true).size() == 0 || D.hasFeature(f, false).size() == 0) {
////                continue;
////            }
//            for (int nL = nMin; nL <= nMax; nL++) {
//                int nR = n - 1 - nL;
//
//                int UBNew = Math.min(UB, misclassifications(TBest) - 1);
//                Pair<MurTree, Integer> subTree = solveSubtreeGivenRootFeature(D,B,f,d,nL,nR,UBNew);
//
//                MurTree T = subTree.getOne();
//                int LBLocal = subTree.getTwo();
//                if(T != null) {
//                    TBest = T;
//                }
//                else {
//                    RLB = Math.min(RLB, LBLocal);
//                }
//            }
//        }
//        if (misclassifications(TBest) <= UB) {
//            storeOptimalSubTreeInCache(TBest,D,B,d,n);
//        }
//        else {
//            LB = Math.max(LB, UB+1);
//            if (RLB < Integer.MAX_VALUE) {
//                LB = Math.max(LB, RLB);
//            }
//            storeLowerBoundInCache(D,B,d,n,LB);
//        }
//        replaceDatasetForSimilarityBound(D,B,d);
//        return TBest;
//    }
//
//    public Pair<MurTree, Integer> solveSubtreeGivenRootFeature(DataSet D, MurTree B, Feature f, int d, int nL, int nR, int UB) {
//        int dL = Math.min(d-1, nL);
//        int dR = Math.min(d-1, nR);
//        Pair<MurTree, MurTree> childBranches = getChildBranches(B, f);
//        MurTree BL = childBranches.getOne();
//        MurTree BR = childBranches.getTwo();
//
////        if (leafMisclassification(D.hasFeature(f, true)) > leafMisclassification(D.hasFeature(f, false))) {
////            int UBL = UB - retrieveLowerBoundFromCache(D.hasFeature(f, true), BR, dR, nR);
////            MurTree TL = new MurTree(D.hasFeature(f, true), BL, dL, nL, UBL);
////
////            if (TL == null) {
////                int LBLocal = computeLocalBound();
////                return new Pair<MurTree, Integer>(null, LBLocal);
////            }
////            int UBR = UB - misclassifications(TL);
////            MurTree TR = new MurTree(D.hasFeature(f, false), BR, dR, nR, UBR);
////
////            if (TR == null) {
////                //PLACEHOLDER FOR T
////                MurTree T = null;
////                return new Pair<MurTree, Integer>(T, misclassifications(T));
////            }
////            else {
////                int LBLocal = computeLocalBound();
////                return new Pair<MurTree, Integer>(null, LBLocal);
////            }
//        }
//        else {
//            // Process right subtree first
//            return null;
//        }
//    }
//
//    public void storeOptimalSubTreeInCache(MurTree T, DataSet D, MurTree B, int d, int n) {
//
//    }
//
//    public void storeLowerBoundInCache(DataSet D, MurTree B, int d, int n, int LB) {
//
//    }
//
//    public void replaceDatasetForSimilarityBound(DataSet D, MurTree B, int d) {
//
//    }
//
//    public Pair<MurTree, MurTree> getChildBranches(MurTree B, Feature f) {
//        return null;
//    }
//
//    public int computeLocalBound() {
//        return 0;
//    }
//
//    public DataSet getDataset() {
//        return dataSet;
//    }
//
//    public String getType() {
//        return type;
//    }
//}
