package fastrepair.yousei.propose.stmtcollector;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author n-ogura
 */
public class AstVector {
    private final int[] vec;
    private final int hash;
    private final String string;

    public AstVector(int[] vector) {
        if (vector == null || vector.length != 92) {
            throw new IllegalArgumentException();
        }
        vec = Arrays.copyOf(vector, vector.length);
        hash = Arrays.hashCode(vec);
        string = "[" + Arrays.stream(vec).mapToObj(Integer::toString).collect(Collectors.joining(",")) + "]";
    }
    public int[] getArray(){return Arrays.copyOf(vec,vec.length);}

    public int getElement(int i) {
        return vec[i];
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof AstVector) {
            AstVector otherAstVector = (AstVector)other;
            return Arrays.equals(otherAstVector.vec, this.vec);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return string;
    }
}
