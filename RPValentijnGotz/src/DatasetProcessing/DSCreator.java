package DatasetProcessing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;



public class DSCreator {
    ArrayList<TempInstance> instanceList = new ArrayList<>();
    HashMap<Integer,Double> maxValues = new HashMap<>();

    public static void main(String[] args) {
        DSCreator dsc = new DSCreator();
        String ret = dsc.parseFile(new File("Datasets/Unprocessed/australiancredit.dat"), new File("Datasets/Unprocessed/australiancredittypes.names"));
        System.out.println(dsc.getInstanceList().get(0).getFv().size());
        dsc.removeMissingData();
        dsc.createDummys();
        dsc.discretizeData(11);
        dsc.toFile();
        System.out.println(dsc.getInstanceList().get(0).getFv().size());
    }

    public String parseFile(File inp, File dataTypes) {
        try {
            StringBuilder sb = new StringBuilder();
            Scanner sc = new Scanner(inp);
            sc.useDelimiter(" ");
            int dataCount = 0;
            int featureCount = 0;
            ArrayList<Boolean> validData = new ArrayList<>();
            ArrayList<String> types = parseTypes(dataTypes);
            ArrayList<TempInstance> ti = new ArrayList();

            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                Scanner lineScanner = new Scanner(line);
                lineScanner.useDelimiter(" ");
                dataCount++;
                ArrayList<TempFeature> fv = new ArrayList<>();
                String label = "";
                if (featureCount == 0) {
                    while(lineScanner.hasNext()) {

                        String feature = lineScanner.next();
                        if(!lineScanner.hasNext()) {
                            label = feature;
                            break;
                        }
                        fv.add(new TempFeature(feature, "f" + featureCount, types.get(featureCount)));
                        if (feature.equals("?")) {
                            validData.add(false);
                        }
                        else {
                            validData.add(true);
                        }
//                        System.out.println(lineScanner.next());
                        featureCount++;

                    }
                }

                int count = 0;

                while(lineScanner.hasNext()) {
                    String feature = lineScanner.next();

                    if(!lineScanner.hasNext()) {
                        label = feature;
                        break;
                    }

                    fv.add(new TempFeature(feature, "f" + count, types.get(count)));
                    if(feature.equals("?")) {
                        validData.set(count, false);
                    }

                    count++;
                }
                ti.add(new TempInstance(fv, label));
            }
            instanceList = ti;
            sc = new Scanner(inp);
            ArrayList<String> types2 = new ArrayList<>();
            for (int x = 0; x < types.size(); x++) {
                if(validData.get(x)) {
                    types2.add(types.get(x));
                }
            }

            while(sc.hasNextLine()) {
                String line = sc.nextLine();
                Scanner lineScanner = new Scanner(line);
                lineScanner.useDelimiter(" ");

                int count = 0;
                while(lineScanner.hasNext() && count < featureCount) {
                    String feature = lineScanner.next();
                    if(validData.get(count)) {
                        sb.append(feature);
                        sb.append(",");
                    }
                    count++;
                }
                sb.deleteCharAt(sb.length()-1);
                sb.append("\n");
            }

            String retString = createFinalString(sb.toString(), types2);

            return retString;
        }
        catch (FileNotFoundException e) {
            System.out.println("File not found");
        }
        return "";
    }

    public ArrayList<String> parseTypes(File inp) {
        ArrayList<String> retList = new ArrayList<>();
        try {
            Scanner sc = new Scanner(inp);
            sc.useDelimiter(" ");

            while(sc.hasNext()) {
                retList.add(sc.next());
            }
        }
        catch (FileNotFoundException e) {
            System.out.println("No such file found");
        }
        return retList;
    }

    public String createFinalString(String inp, ArrayList<String> dataTypes) {
        Scanner sc = new Scanner(inp);
        sc.useDelimiter(" ");
        HashMap<Integer, ArrayList<String>> typesSeen = new HashMap<>();
        StringBuilder retString = new StringBuilder();
        for (int x = 0; x < dataTypes.size(); x++) {
            typesSeen.put(x, new ArrayList<>());
        }
        while(sc.hasNextLine()) {
            Scanner line = new Scanner(sc.nextLine());
            line.useDelimiter(" ");
            int count = 0;
            while(line.hasNext()) {
                String feature = line.next();
                if(count >= dataTypes.size()) {
                    break;
                }
                switch (dataTypes.get(count)) {
                    case "n":
                        ArrayList<String> typeList = typesSeen.get(count);
                        if(!typeList.contains(feature)) {
                            typeList.add(feature);
                        }
                        break;
                    case "o":
                        break;
                    case "c":
                        break;
                    default:
                }
                count++;
            }
        }

        sc = new Scanner(inp);
        sc.useDelimiter(" ");

        while(sc.hasNextLine()) {
            Scanner line = new Scanner(sc.nextLine());
            line.useDelimiter(" ");
            int count = 0;
            while(line.hasNext()) {
                String feature = line.next();
                if(count >= dataTypes.size()) {
//                    retString.deleteCharAt(retString.length()-1);
                    retString.append(feature + "\n");
                    break;
                }
                switch (dataTypes.get(count)) {
                    case "n":
                        ArrayList<String> nClasses = typesSeen.get(count);
//                        System.out.println(nClasses.size());
                        for(String value : nClasses) {
                            if(feature.equals(value)) {
                                retString.append("1 ");
                            }
                            else {
                                retString.append("0 ");
                            }
                        }
                        break;
                    case "o":
                        retString.append(feature + " ");
                        break;
                    case "c":
                        retString.append(feature + " ");
                        break;
                    default:
                        break;
                }
                count++;
            }
        }

        return retString.toString();
    }

    public void removeMissingData() {
        if(instanceList.isEmpty()) {
            return;
        }

        ArrayList<Boolean> missingList = new ArrayList<>();
        for (int x = 0; x < instanceList.get(0).getFv().size(); x++) {
            missingList.add(false);
        }

        for(int x = 0; x < instanceList.size(); x++) {
            ArrayList<TempFeature> fv = instanceList.get(x).getFv();
            for (int y = 0; y < fv.size(); y++) {
                if(fv.get(y).getValue().equals("?")) {
                    missingList.set(y, true);
                }
            }
        }
        for(int x = 0; x < instanceList.size(); x++) {
            ArrayList<TempFeature> fv = instanceList.get(x).getFv();
            int offset = 0;
            int size = fv.size();
            for (int y = 0; y < size; y++) {
                if(missingList.get(y) == true) {
                    fv.remove(y - offset);
                    offset++;
                }
            }
        }
    }

    public void createDummys() {
        if (instanceList.isEmpty()) {
            return;
        }
        HashMap<Integer, ArrayList<String>> typesSeen = new HashMap<>();
        for (int x = 0; x < instanceList.get(0).getFv().size(); x++) {
            typesSeen.put(x, new ArrayList<>());
        }
        for(int x = 0; x < instanceList.size(); x++) {
            ArrayList<TempFeature> fv = instanceList.get(x).getFv();
            for(int y = 0 ; y < fv.size(); y++) {
                ArrayList<String> types = typesSeen.get(y);
                if(!types.contains(fv.get(y).getValue())) {
                    types.add(fv.get(y).getValue());
                }
            }
        }

        for(int x = 0; x < instanceList.size(); x++) {
            ArrayList<TempFeature> fv = instanceList.get(x).getFv();
            int size = fv.size();
            int offset = 0;

            for(int y = 0 ; y < size; y++) {
                TempFeature feature = fv.get(y - offset);
                if(feature.getType().equals("n")) {
                    String value = feature.getValue();
                    ArrayList<String> types = typesSeen.get(y);
                    for(int z = 0; z < types.size() && types.size() > 1; z++) {
                        if(value.equals(types.get(z))) {
                            fv.add(new TempFeature("" + 1, feature.getName() + "." + z, feature.getType()));
                        }
                        else {
                            fv.add(new TempFeature("" + 0, feature.getName() + "." + z, feature.getType()));
                        }
                    }
                    fv.remove(y-offset);
                    offset++;
                }
            }
        }
    }

    public void discretizeData(int bins) {
        if (instanceList.isEmpty()) {
            return;
        }
        HashMap<Integer, Double> min = new HashMap<>();
        HashMap<Integer, Double> max = new HashMap<>();

        for (int x = 0; x < instanceList.get(0).getFv().size(); x++) {
            min.put(x, Double.MAX_VALUE);
            max.put(x, Double.MIN_VALUE);
        }

        for(int x = 0; x < instanceList.size(); x++) {
            ArrayList<TempFeature> fv = instanceList.get(x).getFv();
            for(int y = 0; y < fv.size(); y++) {
                double value = Double.parseDouble(fv.get(y).getValue());
                if(value < min.get(y)) {
                    min.put(y, value);
                }
                if (value > max.get(y)) {
                    max.put(y, value);
                }
            }
        }
        maxValues = max;

        for(int x = 0; x < instanceList.size(); x++) {
            ArrayList<TempFeature> fv = instanceList.get(x).getFv();
            for(int y = 0; y < fv.size(); y++) {
                if(fv.get(y).getType().equals("c")) {
                    double value = Double.parseDouble(fv.get(y).getValue());
                    double low = min.get(y);
                    double high = max.get(y);
                    double step = (high - low) / bins;
                    step += step / 1000;
                    maxValues.put(y, high);

                    for (int z = 0; z < bins; z++) {
                        if(low + step * z <= value && value <= low + step * (z+1)) {
                            fv.get(y).setValue(Integer.toString(z));
                            break;
                        }
                    }
                }
            }
        }
        ArrayList<TempFeature> fv = instanceList.get(0).getFv();
        for (int x = 0; x < fv.size(); x++) {
            if(fv.get(x).getType().equals("c")) {
                maxValues.put(x, (double) bins-1);
            }
        }
    }

    public void toFile() {

        try {
            PrintWriter pw = new PrintWriter("test2.txt");
            StringBuilder ret = new StringBuilder();
            for(int x = 0; x < instanceList.get(0).getFv().size(); x++) {
                ret.append(maxValues.get(x).intValue() + " ");
            }
//            ret.append("\n");
            pw.println(ret.toString());
            for(int x = 0; x < instanceList.size(); x++) {
                ret = new StringBuilder();
                ret.append(instanceList.get(x).getLabel() + " ");
                ArrayList<TempFeature> fv = instanceList.get(x).getFv();
                for (int y = 0; y < fv.size(); y++) {
                    ret.append(fv.get(y).getValue() + " ");
                }

                if(x == instanceList.size()-1) {
//                ret.append(fv.get(fv.size()-1).getValue());
//                for (int y = 0; y < fv.size(); y++) {
//                    ret.append(fv.get(fv.size()-1).getValue());
//                   System.out.println(fv.get(y).getValue() + " ");
//                }
                }
                else {
//                    ret.append("\n");
                }
                System.out.println(x);
                pw.println(ret.toString());
            }
            pw.flush();
            pw.close();
//            pw.println(ret.toString());
        }
        catch (FileNotFoundException e) {

        }
    }

    public ArrayList<TempInstance> getInstanceList() {
        return instanceList;
    }
}
