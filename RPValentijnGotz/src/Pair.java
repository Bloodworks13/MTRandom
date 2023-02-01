import java.util.Objects;

public class Pair <T1,T2> {
    T1 one;
    T2 two;
    private int hashCode;

    public Pair(T1 one, T2 two) {
        this.one = one;
        this.two = two;
        this.hashCode = Objects.hash(one, two);
    }

    public T1 getOne() {return one;}

    public T2 getTwo() {return two;}

    public void setOne(T1 one) {
        this.one = one;
    }

    public void setTwo(T2 two) {
        this.two = two;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || o.getClass() != this.getClass()) {
            return false;
        }
        Pair<T1, T2> that = (Pair<T1, T2>) o;

        return (one.equals(that.getOne()) && two.equals(that.getTwo()));
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return "(" + one + ", " + two + ")";
    }
}
