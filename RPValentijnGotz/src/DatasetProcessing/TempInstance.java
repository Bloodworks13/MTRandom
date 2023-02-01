package DatasetProcessing;

import java.util.ArrayList;

public class TempInstance {
    ArrayList<TempFeature> fv = new ArrayList<>();
    String label = new String();

    public TempInstance(ArrayList<TempFeature> fv, String label) {
        this.fv = fv;
        this.label = label;
    }

    public ArrayList<TempFeature> getFv() {
        return fv;
    }

    public String getLabel() {
        return label;
    }

    public String toString() {
        return "l:" + label + " fv:" + fv.toString() + "\n";
    }
}
