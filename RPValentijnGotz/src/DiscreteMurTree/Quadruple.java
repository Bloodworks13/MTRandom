package DiscreteMurTree;

import java.util.Objects;

public class Quadruple<T1,T2,T3,T4> {
    T1 one;
    T2 two;
    T3 three;
    T4 four;
    private int hashCode;

    public Quadruple(T1 one, T2 two, T3 three, T4 four) {
        this.one = one;
        this.two = two;
        this.three = three;
        this.four = four;
        this.hashCode = Objects.hash(one, two, three, four);
    }

    public T1 getOne() {return one;}

    public T2 getTwo() {return two;}

    public T3 getThree() {return three;}

    public T4 getFour() {return four;}

    public void setOne(T1 one) {
        this.one = one;
    }

    public void setFour(T4 four) {
        this.four = four;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || o.getClass() != this.getClass()) {
            return false;
        }
        Quadruple<T1, T2, T3, T4> that = (Quadruple<T1, T2, T3, T4>) o;

        return (one.equals(that.getOne()) && (two == null || two.equals(that.getTwo())) && three.equals(that.getThree()) && four.equals(that.getFour()));
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return "(" + one + ", " + two + ", " + three + ", " + four +  ")";
    }
}
