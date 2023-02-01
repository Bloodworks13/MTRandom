import org.apache.commons.math3.distribution.LogisticDistribution;

public class RandomizationFunction {
    LogisticDistribution ld;
    int maxValue;

    public RandomizationFunction(int center, double spread, int maxValue) {
        if (spread == 0) {
            spread = 1 / Math.pow(10,20);
        }
        ld = new LogisticDistribution(center+0.001,spread);
        this.maxValue = maxValue;
    }

    public double giveProbability(int value) {
        if(maxValue==1) {
            if(value==0) {
                return 0;
            }
            else {
                return 1;
            }
        }
//        if (value == 0) {
//            return 0;
//        }
//        if (value == maxValue) {
//            return 1;
//        }
        return ld.cumulativeProbability(value);
    }
}
