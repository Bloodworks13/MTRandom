import java.util.*;

public class SpecializedTree {
    DataSet Dl;
    Feature splittingFeature;
    SpecializedTree leftTree;
    SpecializedTree rightTree;
    double misclassifications;
    int numberOfNodes;
    String label;
    ArrayList<Feature> features;
    RandomizationFunction probabilityFunction;
    ArrayList<SpecializedTree> parentsLeft;
    ArrayList<SpecializedTree> parentsRight;

    public SpecializedTree(DataSet D, Feature splittingFeature ,SpecializedTree left, SpecializedTree right, String label, double spread) {
        this.splittingFeature = splittingFeature;
        leftTree = left;
        rightTree = right;
        this.label = label;
        this.Dl = D;
        misclassifications = Misclassifications(leftTree) + Misclassifications(rightTree);
        features = D.getFeatureList();
        numberOfNodes = getNumberOfNodes();
        if (splittingFeature == null) {

        }
        else {
            probabilityFunction = new RandomizationFunction(splittingFeature.getValue(),spread,splittingFeature.getMaxValue());
        }
//        parentsLeft = new ArrayList<>();
//        parentsRight = new ArrayList<>();
//        if (left != null) {
//            left.addParentLeft(this);
//        }
//        if (right != null) {
//            right.addParentRight(this);
//        }
    }


    public void setMisclassifications(double misses) {
        misclassifications = misses;
    }

    public void setFeature(Feature f) { splittingFeature = f;}

    public int leafMisclassification(DataSet D) {
        return Math.min(D.getDataset(1).size(),D.getDataset(-1).size());
    }

    public double getMisclassifications() {return misclassifications;}

    // Returns misclassifications, if it hasn't been calculated yet then it does so and stores it
    public static double Misclassifications(SpecializedTree T) {
        if (T == null) {
            return Double.MAX_VALUE;
        }
        return T.getMisclassifications();
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

//    public ArrayList<SpecializedTree> getParentsLeft() {
//        return parentsLeft;
//    }
//
//    public ArrayList<SpecializedTree> getParentsRight() {
//        return parentsRight;
//    }

    public DataSet getDataset() {
        return Dl;
    }

    public RandomizationFunction getProbabilityFunction() {
        return getProbabilityFunction();
    }

    public Feature getSplittingFeature() {
        return splittingFeature;
    }

//    public void addParentLeft(SpecializedTree par) {
//        parentsLeft.add(par);
//        if (leftTree != null) {
//            leftTree.addParentLeft(par);
//        }
//        if (rightTree != null) {
//            rightTree.addParentLeft(par);
//        }
//    }
//
//    public void addParentRight(SpecializedTree par) {
//        parentsRight.add(par);
//        if (leftTree != null) {
//            leftTree.addParentRight(par);
//        }
//        if (rightTree != null) {
//            rightTree.addParentRight(par);
//        }
//    }

    public String evaluate(DataPoint d) {
        ArrayList<Feature> fv = d.getFeatureList();
        if(leftTree == null || rightTree == null) {
            return label;
        }
        Feature comp = fv.get(splittingFeature.getIndex());
        double p = probabilityFunction.giveProbability(comp.getValue());
        if(Math.random() > p) {
            return rightTree.evaluate(d);
        }
        else {
            return leftTree.evaluate(d);
        }
    }

    public double evaluateDataset(DataSet d) {
        double misses = 0;

        for (DataPoint dp : d.getDataPoints()) {
            if(evaluate(dp).equals(dp.getLabel())) {

            }
            else {
                misses++;
            }
        }

        return misses;
    }

    public double evaluateDatasetWithAttack(DataSet d, String type) {
        double probMiss = 0;
        int maxPertubation = 10;
        ArrayList<Feature> attackableFeatures = new ArrayList<>();
        Stack<SpecializedTree> treeStack = new Stack<>();

        treeStack.push(this);

        while(!treeStack.isEmpty()) {
            SpecializedTree current = treeStack.pop();
            if(current.getLeftTree() != null) {
                treeStack.push(current.getLeftTree());
            }
            if(current.getRightTree() != null) {
                treeStack.push(current.getRightTree());
            }
            if(current.getLeftTree() != null && current.getRightTree() != null) {
                Feature splitF = current.getSplittingFeature();
                if(splitF == null) {
                    continue;
                }
                if(splitF.getMaxValue() != 1) {
                    boolean contains = false;
                    for(Feature f : attackableFeatures) {
                        if(f.getName().equals(splitF.getName())) {
                            contains = true;
                        }
                    }
                    if(!contains) {
                        attackableFeatures.add(splitF);
                    }
                }
            }
        }
        System.out.println(attackableFeatures);

        for (DataPoint dp : d.getDataPoints()) {
            double max = 0;
            double currentProb = calcProbCorrect(dp, 1);
            ArrayList<Feature> tempFeatures = new ArrayList<>();
            for(Feature af : attackableFeatures) {
                tempFeatures.add(dp.getFeatureList().get(af.getIndex()).clone());
            }
            for (int x = 0; x < attackableFeatures.size(); x++) {
                Feature attackFeature = attackableFeatures.get(x);
                int featureValue = dp.getFeatureList().get(attackFeature.getIndex()).getValue();
                for(int y = 0; y <= maxPertubation; y++) {
                    int attackValue = featureValue - y;
                    if(attackValue < 0) {
                        break;
                    }
                    dp.getFeatureList().get(attackFeature.getIndex()).setValue(attackValue);
                    double probDiff = currentProb - calcProbCorrect(dp, 1);

                    if(probDiff > max) {
                        max = probDiff;
                    }
                }
                for(int y = 0; y <= maxPertubation; y++) {
                    int attackValue = featureValue + y;
                    if(attackValue > attackFeature.getMaxValue()) {
                        break;
                    }
                    dp.getFeatureList().get(attackFeature.getIndex()).setValue(attackValue);
                    double probDiff = 1 - calcProbCorrect(dp, 1);

                    if(probDiff > max) {
                        max = probDiff;
                    }
                }
            }
            for (Feature tf : tempFeatures) {
                dp.getFeatureList().set(tf.getIndex(), tf);
            }
            probMiss += max;
        }
        return probMiss;
    }

    public double calcProbCorrect(DataPoint dp, double prob) {
        if(leftTree == null || rightTree == null) {
            if(label.equals(dp.getLabel())) {
                return prob;
            }
            else {
                return 0;
            }
        }


        double p = probabilityFunction.giveProbability(dp.getFeatureList().get(splittingFeature.getIndex()).getValue());

        return leftTree.calcProbCorrect(dp, prob * p) + rightTree.calcProbCorrect(dp, prob * (1-p));
    }
}
