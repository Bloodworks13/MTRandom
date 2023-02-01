package DatasetProcessing;

public class TempFeature {
    String value;
    String name;
    String type;

    public TempFeature(String value, String name, String type) {
        this.value = value;
        this.name = name;
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String toString() {
        return name + ":" + type + ":" + value;
    }
}
