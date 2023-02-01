package DiscreteMurTree;

import org.apache.commons.math3.distribution.LogisticDistribution;

public class RandomizationFunction {
    LogisticDistribution ld;
    int maxValue;

    public RandomizationFunction(int center, double spread, int maxValue) {
        ld = new LogisticDistribution(center,spread);
        this.maxValue = maxValue;
    }

    public double giveProbability(int value) {
        if (value == 0) {
            return 0;
        }
        if (value == maxValue) {
            return 1;
        }
        return ld.cumulativeProbability(value);
    }
}
