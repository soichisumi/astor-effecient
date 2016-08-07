package fastrepair.yousei.propose.stmtcollector;import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author n-ogura
 */
public class AstVectorVisitor extends ASTVisitor {
    private final Map<Class<? extends ASTNode>, Integer> vector = new HashMap<>();
    public void preVisit(ASTNode node) {
        vector.merge(node.getClass(), 1, (a, b) -> a + b);
    }
    public AstVector getVector() {
        int[] vec = new int[92];
        for (int i = 0; i < 92; i++) {
            Class<?> astClass = ASTNode.nodeClassForType(i + 1);
            vec[i] = vector.getOrDefault(astClass, 0);
        }
        return new AstVector(vec);
    }
}
