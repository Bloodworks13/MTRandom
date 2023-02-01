package DiscreteMurTree;

import java.util.*;

public class SpecializedTree {
    DataSet Dl;
    Feature splittingFeature;
    SpecializedTree leftTree;
    SpecializedTree rightTree;
    int misclassifications;
    int numberOfNodes;
    String label;
    ArrayList<Feature> features;

    public SpecializedTree(DataSet D, Feature splittingFeature , SpecializedTree left, SpecializedTree right, String label) {
        this.splittingFeature = splittingFeature;
        leftTree = left;
        rightTree = right;
        this.label = label;
        this.Dl = D;
        misclassifications = Misclassifications(leftTree) + Misclassifications(rightTree);
        features = D.getFeatureList();
        numberOfNodes = getNumberOfNodes();
    }

    public void setMisclassifications(int misses) {
        misclassifications = misses;
    }

    public void setFeature(Feature f) { splittingFeature = f;}

    public int leafMisclassification(DataSet D) {
        return Math.min(D.getDataset(1).size(),D.getDataset(-1).size());
    }

    public void split() {
        if (splittingFeature == null) {
            return;
        }
        Pair<DataSet, DataSet> dPair = Dl.splitOnFeature(splittingFeature, 0);
        leftTree = MT.classificationNode(dPair.getOne());
        rightTree = MT.classificationNode(dPair.getTwo());
    }

    public int getMisclassifications() {return misclassifications;}

    // Returns misclassifications, if it hasn't been calculated yet then it does so and stores it
    public static int Misclassifications(SpecializedTree T) {
        if (T == null) {
            return Integer.MAX_VALUE;
        }
        else if (T.getMisclassifications() != Integer.MAX_VALUE) {
            return T.getMisclassifications();
        }
        else {
            if (T.getLabel() == null) {
                return Misclassifications(T.getLeftTree()) + Misclassifications(T.getRightTree());
            }
            else {
                return T.getMisclassifications();
            }
        }
    }

    // Returns the amount of predicate nodes
    public int getNumberOfNodes() {
        if (numberOfNodes != 0) {
            return numberOfNodes;
        }
        if (splittingFeature == null) {
            return 0;
        }
        else {
            int retAmount = 1;
            if (leftTree != null) {
                retAmount += leftTree.getNumberOfNodes();
            }
            if (rightTree != null) {
                retAmount += rightTree.getNumberOfNodes();
            }
            return retAmount;
        }
    }

    public SpecializedTree getLeftTree() {return leftTree;}

    public SpecializedTree getRightTree() {return rightTree;}

    public String getLabel() {return label;}

    public String toString() {
        Queue<Quadruple<SpecializedTree, Integer,Integer,Integer>> nextTree = new ArrayDeque<>();
        String retString = "0";
        nextTree.offer(new Quadruple<>(this, 0,15,0));
        int currentDepth = 0;
        int currentWidth = 0;
        while(!nextTree.isEmpty()) {
            Quadruple<SpecializedTree, Integer,Integer,Integer> currentPair = nextTree.poll();
            if (currentPair.getTwo() > currentDepth) {
                retString += "\n" + currentPair.getTwo();
                currentDepth = currentPair.getTwo();
                currentWidth = 0;
            }
            while(currentWidth < currentPair.getThree()) {
                retString += " ";
                currentWidth++;
            }
            if(currentPair.getOne() == null) {
                retString += "NULL";
                continue;
            }
            if(currentPair.getOne().splittingFeature == null) {
                retString += "|label:" + currentPair.getOne().getLabel() + " misses:" + Misclassifications(currentPair.getOne()) + "|";
            }
            else {
                retString += "|split:" + currentPair.getOne().splittingFeature + " misses:" + Misclassifications(currentPair.getOne()) + "|";
            }
            nextTree.offer(new Quadruple<>(currentPair.getOne().getLeftTree(), currentDepth+1, currentPair.getThree() - (currentPair.getThree()/2)/(currentDepth+1),0));
            nextTree.offer(new Quadruple<>(currentPair.getOne().getRightTree(), currentDepth+1, currentPair.getThree() + (currentPair.getThree()/2)/(currentDepth+1), 0));
        }
        return retString;
    }
}
