package DiscreteMurTree;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

public class DSParser {

    public DataSet parseFile(File inp) {
        try {
            Scanner sc = new Scanner(inp);
            HashSet<DataPoint> dataPoints = new HashSet<>();
            ArrayList<Feature> globFeatureList = new ArrayList<>();
            int id = 0;
            while (sc.hasNextLine()) {

                String next = sc.nextLine();
                String label;
                if (next.charAt(0) == '0') {
                    label = "-";
                }
                else {
                    label = "+";
                }

                ArrayList<Feature> fv = new ArrayList<>();
                int featureCount = 0;
                char[] charArr = next.toCharArray();
                for (int x = 1; x < charArr.length; x++) {
                    char c = charArr[x];
                    if (c == '0') {
                        Feature tf = new Feature("F" + featureCount, 0, featureCount);
                        fv.add(tf);
                        featureCount++;
                    }
                    else if (c == '1') {
                        Feature tf = new Feature("F" + featureCount, 1,featureCount);
                        fv.add(tf);
                        featureCount++;
                    }
                }
                if(globFeatureList.isEmpty()) {
                    for(Feature f : fv) {
                        globFeatureList.add(new Feature(f.getName(), 1, f.index));
                    }
                }
                dataPoints.add(new DataPoint(fv, label, id));
                id++;
            }
            return new DataSet(dataPoints, globFeatureList);
        }
        catch (FileNotFoundException e) {
            System.out.println("File not found");
        }
        return null;
    }
}
