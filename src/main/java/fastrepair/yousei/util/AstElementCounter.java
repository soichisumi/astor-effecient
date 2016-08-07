package fastrepair.yousei.util;

import org.eclipse.cdt.core.dom.ast.ASTGenericVisitor;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTAmbiguousNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


//実験によって変えること
//inIgnoreList内のignoreListの切り替え
//hasIgnoreWordsを使うかどうかの切り替え
public class AstElementCounter extends ASTGenericVisitor {
    public final Map<String, Integer> elements = new HashMap<>();
    private boolean ignoreSomeNodes = false;
    private String[] ignoreList = {
            "CASTName",
            "CASTIdExpression",
            "CASTLiteralExpression",
            "CASTUnaryExpression",
            "CASTTypeId"
    };
    private String[] ignoreList2 = {
            "CASTName",
            "CASTIdExpression",
            "CASTLiteralExpression",
            "CASTUnaryExpression",
            "CASTTypeId",
            "CASTTypedefName"
    };
    private String[] ignoreWords = {
            "Specifier",
            "BinaryExpression"
    };

    public AstElementCounter() {
        super(true);
        this.shouldVisitAmbiguousNodes = true;
    }

    public AstElementCounter(boolean ignore) {
        super(true);
        this.shouldVisitAmbiguousNodes = true;
        this.ignoreSomeNodes = ignore;
    }

    @Override
    protected int genericVisit(IASTNode node) {
        if (!(this.ignoreSomeNodes && (inIgnoreList(node) || hasIgnoreWord(node))))//(ignoreの設定がされていて，ノードの種類がignoreListに入っている)が満たされないなら
            elements.merge(node.getClass().getSimpleName(), 1, (a, b) -> a + b);
        return ASTVisitor.PROCESS_CONTINUE;
    }

    @Override
    public int visit(ASTAmbiguousNode node) {
        if (!(this.ignoreSomeNodes && (inIgnoreList(node) || hasIgnoreWord(node))))
            elements.merge(node.getClass().getSimpleName(), 1, (a, b) -> a + b);

        for (IASTNode child : node.getNodes()) {
            child.accept(this);
        }
        return ASTVisitor.PROCESS_CONTINUE;
    }

    public boolean inIgnoreList(IASTNode node) {
        boolean inIgnoreList = false;
        String nodeName = node.getClass().getSimpleName();
        for (String s : this.ignoreList2) {
            if (Objects.equals(s, nodeName))
                inIgnoreList = true;
        }
        return inIgnoreList;
    }

    public boolean inIgnoreList(ASTAmbiguousNode node) {
        boolean inIgnoreList = false;
        String nodeName = node.getClass().getSimpleName();
        for (String s : this.ignoreList2) {
            if (Objects.equals(s, nodeName))
                inIgnoreList = true;
        }
        return inIgnoreList;
    }

    public boolean hasIgnoreWord(IASTNode node) {
        boolean hasIgnoreWord = false;
        for (String s : this.ignoreWords) {
            if (node.getClass().getSimpleName().contains(s))
                hasIgnoreWord = true;
        }
        return hasIgnoreWord;
    }

    public boolean isIgnoreSomeNodes() {
        return ignoreSomeNodes;
    }

    public String[] getIgnoreList() {
        return ignoreList;
    }

    public String[] getIgnoreList2() {
        return ignoreList2;
    }

    public String[] getIgnoreWords() {
        return ignoreWords;
    }
}
