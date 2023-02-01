package DiscreteMurTree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

public class DataSet {
    HashSet<DataPoint> dataPoints;
    HashSet<DataPoint> dataPointsPlus;
    HashSet<DataPoint> dataPointsMinus;
    ArrayList<Feature> featureList;
    private int hashCode;

    public DataSet(HashSet<DataPoint> dataPoints) {
        this.dataPoints = dataPoints;
        featureList = new ArrayList<>();
        if (dataPoints.size() > 0) {
            ArrayList<Feature> temp = new ArrayList<>();
            for (DataPoint dat : dataPoints) {
                temp = dat.getFeatureList();
                break;
            }

            for(Feature f : temp) {
                featureList.add(new Feature(f.getName(), -1, f.index, f.getMaxValue()));
            }
        }
        this.hashCode = Objects.hash(dataPoints);
    }

    public DataSet(HashSet<DataPoint> dataPoints, ArrayList<Feature> featureList) {
        this.dataPoints = dataPoints;
        this.featureList = featureList;
        this.hashCode = Objects.hash(dataPoints);
        dataPointsPlus = null;
        dataPointsMinus = null;
    }

    public HashSet<DataPoint> getDataPoints() {return dataPoints;}

    public ArrayList<Feature> getFeatureList() {return featureList;}

    // Splits the dataset on the given feature, if the boolean right is true
    public Pair<DataSet,DataSet> splitOnFeature(Feature f) {
        HashSet<DataPoint> listSplitLeft = new HashSet<>();
        HashSet<DataPoint> listSplitRight = new HashSet<>();
        for(DataPoint d : dataPoints) {
            // if the index of the feature is stored, directly access it
            if (f.index != -1) {
                Feature comp = d.getFeatureList().get(f.index);
                if (comp.getValue() <= f.getValue()) {
                    listSplitLeft.add(d);
                }
                else {
                    listSplitRight.add(d);
                }
            }
            // If no index is saved for a feature, find it by iterating the feature list, should not happen
            else {
                System.out.println("alarm?");
//                for(Feature comp : d.getFeatureList()) {
//                    if(comp.getName().equals(f.getName())) {
//                        if(comp.getValue() == f.getValue()) {
//                            listSplitLeft.add(d);
//                        }
//                        break;
//                    }
//                }
            }
        }
        return new Pair<>(new DataSet(listSplitLeft, featureList), new DataSet(listSplitRight, featureList));
    }

    // Splits the dataset on the given feature, if the boolean right is true
    public Pair<DataSet,DataSet> splitOnFeature(Feature f, int value) {
        HashSet<DataPoint> listSplitLeft = new HashSet<>();
        HashSet<DataPoint> listSplitRight = new HashSet<>();
        for(DataPoint d : dataPoints) {
            // if the index of the feature is stored, directly access it
            if (f.index != -1) {
                Feature comp = d.getFeatureList().get(f.index);
                if (comp.getValue() <= value) {
                    listSplitLeft.add(d);
                }
                else {
                    listSplitRight.add(d);
                }
            }
            // If no index is saved for a feature, find it by iterating the feature list, should not happen
            else {
                System.out.println("alarm?");
//                for(Feature comp : d.getFeatureList()) {
//                    if(comp.getName().equals(f.getName())) {
//                        if(comp.getValue() == f.getValue()) {
//                            listSplitLeft.add(d);
//                        }
//                        break;
//                    }
//                }
            }
        }
        return new Pair<>(new DataSet(listSplitLeft, featureList), new DataSet(listSplitRight, featureList));
    }

    public DataSet hasLabel(String label, boolean complement) {
        if(dataPointsPlus != null && label.equals("+")) {
            return new DataSet(dataPointsPlus, featureList);
        }
        if(dataPointsMinus != null && label.equals("-")) {
            return new DataSet(dataPointsMinus, featureList);
        }
        HashSet<DataPoint> pointsWithLabel = new HashSet<>();
        for (DataPoint p : dataPoints) {
            if (!complement && p.getLabel().equals(label)) {
                pointsWithLabel.add(p);
            }
            else if (complement && !p.getLabel().equals(label)) {
                pointsWithLabel.add(p);
            }
        }
        DataSet retDataset = new DataSet(pointsWithLabel, featureList);
        if(label == "+") {
            dataPointsPlus = pointsWithLabel;
        }
        if(label == "-") {
            dataPointsMinus = pointsWithLabel;
        }
        return retDataset;
    }

    public DataSet getDataset(int x) {
        if (x == 0) {
            return this;
        }

        HashSet<DataPoint> listSplit = new HashSet<>();

        if (x == 1) {
            for(DataPoint d : dataPoints) {
                if (d.getLabel() == "+") {
                    listSplit.add(d);
                }
            }
        }
        else if(x == -1) {
            for(DataPoint d : dataPoints) {
                if (d.getLabel() == "-") {
                    listSplit.add(d);
                }
            }
        }
        return new DataSet(listSplit, featureList);
    }

    public DataSet setExclude(DataSet other) {
        HashSet<DataPoint> points = (HashSet<DataPoint>) dataPoints.clone();
        points.removeAll(other.getDataPoints());
        return new DataSet(points, featureList);
    }

    public int size() {
        return dataPoints.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        DataSet that = (DataSet) o;

        if (dataPoints.size() != that.getDataPoints().size()) {
            return false;
        }

        HashSet<DataPoint> otherPoints = that.getDataPoints();

        if(!dataPoints.equals(otherPoints)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public String toString() {
        return "" + dataPoints.size();
    }
}
