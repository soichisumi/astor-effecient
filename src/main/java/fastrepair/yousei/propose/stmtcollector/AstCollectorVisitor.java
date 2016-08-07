package fastrepair.yousei.propose.stmtcollector;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Statement;

/**
 * @author n-ogura
 */
public class AstCollectorVisitor extends ASTVisitor {
    private final String path;
    private final CompilationUnit unit;
    public final Multimap<AstVector, AstLocation> asts;

    public AstCollectorVisitor(String path, CompilationUnit unit) {
        this.path = path;
        this.unit = unit;
        this.asts = MultimapBuilder.hashKeys().arrayListValues().build();
    }

    public void preVisit(ASTNode node) {
        if (node instanceof Statement) {
            AstVectorVisitor visitor = new AstVectorVisitor();
            Statement statement = (Statement) node;
            statement.accept(visitor);
            AstVector vector = visitor.getVector();
            asts.put(vector, getLocation(node));
        }
    }

    private AstLocation getLocation(ASTNode node) {
        int startLine = unit.getLineNumber(node.getStartPosition());
        int startColumn = unit.getColumnNumber(node.getStartPosition());
        int endLine = unit.getLineNumber(node.getStartPosition() + node.getLength());
        int endColumn = unit.getColumnNumber(node.getStartPosition() + node.getLength());
        return new AstLocation(path, startLine, startColumn, endLine, endColumn, node.getClass().getSimpleName());
    }
}
