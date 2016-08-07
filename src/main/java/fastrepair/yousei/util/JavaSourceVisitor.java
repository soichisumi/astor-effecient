package fastrepair.yousei.util;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author s-sumi
 */
public class JavaSourceVisitor extends ASTVisitor {

    public final List<Integer> vector;
    public final int NODE_KINDS = 92;

    /**
     * コンストラクタ
     */
    public JavaSourceVisitor() {
        this.vector = new ArrayList<>(NODE_KINDS);
        for (int i = 0; i < NODE_KINDS; i++) {
            this.vector.add(0);
        }
    }

    @Override
    public void preVisit(ASTNode node) {
        final int nodeType = node.getNodeType();
        this.vector.set(nodeType - 1, this.vector.get(nodeType) + 1);
    }

}
