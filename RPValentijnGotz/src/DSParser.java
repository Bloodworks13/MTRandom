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
            ArrayList<String> labelList = new ArrayList<>();
            int id = 0;
            ArrayList<Integer> maxValues = new ArrayList<>();
            String maxLine = sc.nextLine();
            Scanner maxSC = new Scanner(maxLine);
            while(maxSC.hasNext()) {
                String value = maxSC.next();
                maxValues.add(Integer.parseInt(value));
            }
            while (sc.hasNextLine()) {

                String next = sc.nextLine();
                String label;

                label = "" + next.charAt(0);
                if (!labelList.contains(label)) {
                    labelList.add(label);
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

            return new DataSet(dataPoints, globFeatureList, labelList);
        }
        catch (FileNotFoundException e) {
            System.out.println("File not found");
        }
        return null;
    }

    public DataSet parseFileDiscrete(File inp, double spread) {
        try {
            Scanner sc = new Scanner(inp);
            HashSet<DataPoint> dataPoints = new HashSet<>();
            ArrayList<Feature> globFeatureList = new ArrayList<>();
            ArrayList<String> labelList = new ArrayList<>();
            int id = 0;
            ArrayList<Integer> maxValues = new ArrayList<>();
            String maxLine = sc.nextLine();
            Scanner maxSC = new Scanner(maxLine);
            while(maxSC.hasNext()) {
                String value = maxSC.next();
                maxValues.add((int) Double.parseDouble(value));
//                System.out.println((int) Double.parseDouble(value));
            }
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                Scanner lineScanner = new Scanner(line);
                String label = "";
                ArrayList<Feature> fv = new ArrayList<>();
                int count = 0;
                while(lineScanner.hasNext()) {
                    if(label.equals("")) {
                        label = lineScanner.next();
                        if(!labelList.contains(label)) {
                            labelList.add(label);
                        }
                        continue;
                    }
                    fv.add(new Feature("F" + count,Integer.parseInt(lineScanner.next()), count, maxValues.get(count), spread));
                    count++;
                }
                if (globFeatureList.isEmpty()) {
                    for (Feature f : fv) {

                        globFeatureList.add(new Feature(f.getName(), -1, f.getIndex(),f.getMaxValue(), spread));
                    }
                }
                dataPoints.add(new DataPoint(fv, label, id));
                id++;
            }

            return new DataSet(dataPoints, globFeatureList, labelList);
        }
        catch (FileNotFoundException e) {
            System.out.println("File not found");
        }
        return null;
    }
}
