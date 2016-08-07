package fastrepair;

import java.nio.channels.Pipe;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by s-sumi on 16/07/31.
 */
public class SpoonAstVector {
    private final int[] vec;
    private final int hash;
    private final String string;

    public SpoonAstVector(int[] vector) {
        if (vector == null || vector.length != Class2IndexConvSingleton.NODE_NUMBER)
            throw new IllegalArgumentException();
        this.vec = Arrays.copyOf(vector, vector.length);
        this.hash = Arrays.hashCode(vector);
        string = "[" + Arrays.stream(vec).mapToObj(Integer::toString).collect(Collectors.joining(",")) + "]";
    }

    public int getElement(int i) { return vec[i]; }

    @Override
    public int hashCode(){return hash;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SpoonAstVector that = (SpoonAstVector) o;

        return hash == that.hash&&  Arrays.equals(vec, that.vec);

    }

    @Override
    public String toString() { return string; }
}
