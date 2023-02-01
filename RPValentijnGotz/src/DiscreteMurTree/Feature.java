package DiscreteMurTree;

import java.util.Objects;

public class Feature {
    String name;
    int value;
    int maxValue;
    private int hashCode;
    int index;

    public Feature (String name, int value) {
        this.name = name;
        this.value = value;
        this.hashCode = Objects.hash(name, value);
        index = -1;
        maxValue = 1;
    }

    public Feature (String name, int value, int index) {
        this(name, value);
        this.index = index;
    }

    public Feature(String name, int value, int index, int maxValue) {
        this(name, value, index);
        this.maxValue = maxValue;
    }

    public String getName() {return name;}

    public int getValue() {return value;}

    public int getMaxValue() {return maxValue;}

    public void setValue(int value) {this.value = value;}

    public void setIndex(int ind) {
        index = ind;
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
