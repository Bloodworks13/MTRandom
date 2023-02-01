import java.util.Objects;

public class Feature {
    String name;
    int value;
    int maxValue;
    private int hashCode;
    int index;
    double spread;
    RandomizationFunction rf;

    public Feature (String name, int value) {
        this.name = name;
        this.value = value;
        this.hashCode = Objects.hash(name, value);
        index = -1;
        maxValue = 1;
        rf = new RandomizationFunction(1,1,1);
    }

    public Feature (String name, int value, int index) {
        this(name, value);
        this.index = index;
    }

    public Feature(String name, int value, int index, int maxValue,double spr) {
        this(name, value, index);
        this.maxValue = maxValue;
        spread = spr;
        rf = new RandomizationFunction(value,spr,maxValue);
    }

    public String getName() {return name;}

    public int getValue() {return value;}

    public int getMaxValue() {return maxValue;}

    public int getIndex() {
        return index;
    }

    public double getSpread() { return spread;}

    public void setValue(int value) {this.value = value;}

    public void setIndex(int ind) {
        index = ind;
    }

    public double getProbability(int value) {
        return rf.giveProbability(value);
    }

    public Feature clone() {
        return new Feature(name, value, index, maxValue, spread);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Feature that = (Feature) o;
        return (name.equals(that.name) && value == that.value) || (name.equals(that.name) && (value == -1 || that.value == -1));
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public String toString() {return name + ":" + value+ "/" + maxValue;}
}
