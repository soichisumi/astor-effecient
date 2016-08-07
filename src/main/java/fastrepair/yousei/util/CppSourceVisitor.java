package fastrepair.yousei.util;

import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.c.*;
import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTCompoundStatementExpression;
import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.gnu.c.ICASTKnRFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.gnu.c.IGCCASTArrayRangeDesignator;
import org.eclipse.cdt.core.dom.ast.gnu.c.IGCCASTSimpleDeclSpecifier;


import java.util.HashMap;
import java.util.Map;

//全てのノードに対して処理を記述しておいて
//想定外のが存在すればException

/**
 * @author s-sumi
 */
public class CppSourceVisitor extends ASTVisitor {
	public Map<String, Integer> counter;
	public CppSourceVisitor() {
		// TODO 自動生成されたコンストラクター・スタブ
		super(true);
		counter=new HashMap<String, Integer>();
	}
	public int visit(IASTArrayDeclarator node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTArrayModifier node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTArraySubscriptExpression node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTASMDeclaration node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTAttribute node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTAttributeOwner node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTBinaryExpression node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTBinaryTypeIdExpression node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTBreakStatement node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTCaseStatement node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTCastExpression node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTComment node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTCompositeTypeSpecifier node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTCompoundStatement node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTConditionalExpression node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTContinueStatement node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTDeclaration node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTDeclarationListOwner node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTDeclarationStatement node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTDeclarator node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTDeclSpecifier node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTDefaultStatement node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTDoStatement node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTElaboratedTypeSpecifier node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTEnumerationSpecifier node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTEnumerationSpecifier.IASTEnumerator node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTEqualsInitializer node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTExpression node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTExpressionList node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTExpressionStatement node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTFieldDeclarator node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTFieldReference node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTForStatement node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTFunctionCallExpression node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTFunctionDeclarator node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTFunctionDefinition node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTFunctionStyleMacroParameter node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTGotoStatement node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTIdExpression node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTIfStatement node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTImplicitName node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTImplicitNameOwner node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTInitializer node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTInitializerClause node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTInitializerExpression node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTInitializerList node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTLabelStatement node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTLiteralExpression node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTName node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTNamedTypeSpecifier node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTNullStatement node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTParameterDeclaration node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTPointer node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTPointerOperator node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTPreprocessorElifStatement node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTPreprocessorElseStatement node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTPreprocessorEndifStatement node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTPreprocessorErrorStatement node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTPreprocessorFunctionStyleMacroDefinition node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTPreprocessorIfdefStatement node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTPreprocessorIfndefStatement node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTPreprocessorIfStatement node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTPreprocessorIncludeStatement node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTPreprocessorMacroDefinition node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTPreprocessorMacroExpansion node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTPreprocessorObjectStyleMacroDefinition node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTPreprocessorPragmaStatement node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTPreprocessorStatement node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTPreprocessorUndefStatement node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTProblem node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTProblemDeclaration node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTProblemExpression node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTProblemStatement node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTProblemTypeId node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTReturnStatement node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTSimpleDeclaration node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTSimpleDeclSpecifier node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTStandardFunctionDeclarator node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTStatement node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTSwitchStatement node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTToken node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTTokenList node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTTranslationUnit node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTTypeId node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTTypeIdExpression node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTTypeIdInitializerExpression node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTUnaryExpression node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IASTWhileStatement node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(ICASTArrayDesignator node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(ICASTArrayModifier node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(ICASTCompositeTypeSpecifier node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(ICASTDeclSpecifier node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(ICASTDesignatedInitializer node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(ICASTDesignator node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(ICASTElaboratedTypeSpecifier node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(ICASTEnumerationSpecifier node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(ICASTFieldDesignator node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(ICASTKnRFunctionDeclarator node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(ICASTPointer node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(ICASTSimpleDeclSpecifier node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(ICASTTypedefNameSpecifier node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(ICASTTypeIdInitializerExpression node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}

	//
	//CPPのノードは対象としない
	//
	public int visit(IGCCASTArrayRangeDesignator node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IGCCASTSimpleDeclSpecifier node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IGNUASTCompoundStatementExpression node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IGNUASTTypeIdExpression node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}
	public int visit(IGNUASTUnaryExpression node){
		counter.merge(node.getClass().getSimpleName(), 1, (a,b) -> a + b );
		//System.out.println(node.getClass().getSimpleName());
		return ASTVisitor.PROCESS_CONTINUE;
	}

}
