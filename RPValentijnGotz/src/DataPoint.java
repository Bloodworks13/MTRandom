import java.util.ArrayList;
import java.util.Objects;

public class DataPoint {
    ArrayList<Feature> featureList;
    ArrayList<Feature> featureListPositive;
    String label;
    int id;
    private int hashCode;

    public DataPoint(ArrayList<Feature> features, String label, int id) {
        featureList = features;
        this.label = label;
        featureListPositive = onlyPositive();
        this.id = id;
        this.hashCode = Objects.hash(features, label,id);
    }

    public ArrayList<Feature> getFeatureList() { return featureList;}

    public ArrayList<Feature> getFeatureListPositive() { return featureListPositive;}

    public String getLabel() {return label;}

    public int getId() {
        return id;
    }

    public String toString() {
        return "Label: "  + label + " Features: " + featureList.toString();
    }

    public ArrayList<Feature> onlyPositive() {
        ArrayList<Feature> nFeatureList = new ArrayList<>();
        for (Feature f : featureList) {
            if (f.getValue() == 1) {
                nFeatureList.add(f);
            }
        }

        return nFeatureList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        DataPoint that = (DataPoint) o;

        if(!label.equals(that.getLabel())) {
            return false;
        }

        ArrayList<Feature> otherFeatures = that.getFeatureList();

        if (otherFeatures.size() != featureList.size()) {
            return  false;
        }

        for (int x = 0; x < featureList.size(); x++) {
            if(!featureList.get(x).equals(otherFeatures.get(x))) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }
}
