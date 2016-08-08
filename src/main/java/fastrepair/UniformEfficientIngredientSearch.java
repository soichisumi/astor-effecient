package fastrepair;

import fastrepair.yousei.propose.StatementRecommender;
import fastrepair.yousei.propose.Util;
import fastrepair.yousei.propose.stmtcollector.AstLocation;
import fr.inria.astor.approaches.jgenprog.operators.ReplaceOp;
import fr.inria.astor.core.entities.Ingredient;
import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.loop.spaces.ingredients.IngredientSpace;
import fr.inria.astor.core.loop.spaces.ingredients.ingredientSearch.AstorCtSearchStrategy;
import fr.inria.astor.core.loop.spaces.operators.AstorOperator;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.setup.RandomManager;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LocationInfo;
import spoon.reflect.code.CtCodeElement;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtElement;
import spoon.template.Local;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by s-sumi on 16/08/01.
 */
public class UniformEfficientIngredientSearch extends AstorCtSearchStrategy {
    public StatementRecommender sr;

    public UniformEfficientIngredientSearch(IngredientSpace space, String reposPath, String bugRevisionId) throws Exception {
        super(space);
        sr = new StatementRecommender(reposPath, bugRevisionId);
    }

    public Map<String, Integer> indexes = new HashMap<>();

    private Logger logger = Logger.getLogger(UniformEfficientIngredientSearch.class.getName());


    /**
     * Listのすべてを使いきったかどうかはどうやってわかるんだ？
     * ->Searchはランダムな順に取り出すことのみを担当する。Strategyでattempt が　fix space sizeを超えたかどうかを判定する
     * まずシャッフル->予測結果に合致するものを先頭に->取り出す
     */
    protected CtCodeElement getNextStatementFromSpace(ModificationPoint location,AstorOperator operationType) {    //ちょっとやばいコードを書くぞ
        if (location.getCodeElement().getPosition().getLine()==72)
            System.out.println("point found");

        List<CtCodeElement> fixSpace = this.ingredientSpace.getIngredients(location.getCodeElement());

        SourcePosition modifyPosition = location.getCodeElement().getPosition();
        String modifyClassName = location.getCtClass().getSimpleName();
        System.out.println("predict for: "+modifyClassName);
        List<AstLocation> locations = sr.getStatements(Util.getSourceCodeFromClassName(modifyClassName), modifyPosition);
        System.out.println("get ingredient from fixspace....");
        System.out.println("get ingredient for "+ location.getCodeElement().toString()+" "+operationType.toString());
        int res= locations==null ?
                fixSpace.size() :
                findNextStatement(fixSpace,locations,
                        modifyClassName+
                        location.getCodeElement().getPosition().getLine()+","+
                        location.getCodeElement().getPosition().getColumn()+","+
                        location.getCodeElement().getPosition().getEndLine()+","+
                        location.getCodeElement().getPosition().getEndColumn()+","+operationType.toString());
        if(res==fixSpace.size()) {  //indexがsizeと等しいなら検索が終わったとする。その時はspaceからランダムに選択
            System.out.println("use default search");
            int size = fixSpace.size();
            res = RandomManager.nextInt(size);
        }
        System.out.println("use :"+  fixSpace.get(res).toString());
        return fixSpace.get(res);
    }

    protected int findNextStatement(List<CtCodeElement> space, List<AstLocation> locations, String className) {
        Integer index=indexes.getOrDefault(className,0);
        while(index<space.size()){
            if(isRecommended(space.get(index),locations)) {
                System.out.println("[ "+space.get(index).toString()+" ] is recommended statement");
                break;
            }
            index++;
        }
        indexes.put(className,index==space.size() ? space.size() : index+1);
        return index;
    }

    private boolean isRecommended(CtCodeElement codeElement,List<AstLocation> recommended){
        for(AstLocation l:recommended){
            if(sameStatement(codeElement,l))
                return true;
        }
        return false;
    }
    private boolean sameStatement(CtCodeElement element,AstLocation recommended){
        /*return element.getPosition().getLine()==recommended.startLine &&
                element.getPosition().getColumn()==recommended.startColumn &&
                element.getPosition().getEndLine()==recommended.endLine &&
                element.getPosition().getEndColumn()==recommended.endColumn;*/
        return element.getPosition().getLine()==recommended.startLine &&
                element.getPosition().getEndLine()==recommended.endLine;
    }



    /**
     * Return a cloned CtStatement from the fix space in a randomly way
     *
     * @return
     */
    protected CtCodeElement getNextElementFromSpace(ModificationPoint location,AstorOperator operationType) {
        CtCodeElement originalPicked = getNextStatementFromSpace(location,operationType);
        CtCodeElement cloned = MutationSupporter.clone(originalPicked);
        return cloned;
    }

    protected CtCodeElement getNextElementFromSpace(ModificationPoint location, String type,AstorOperator operationType) {
        List<CtCodeElement> elements = this.ingredientSpace.getIngredients(location.getCodeElement(), type);
        if (elements == null)
            return null;
        return getNextStatementFromSpace(location,operationType);
    }

    @Override
    public Ingredient getFixIngredient(ModificationPoint modificationPoint, AstorOperator operationType) {

        String type = null;
        if (operationType instanceof ReplaceOp) {
            type = modificationPoint.getCodeElement().getClass().getSimpleName();
        }

        CtElement selectedIngredient = null;
        if (type == null) {
            selectedIngredient = this.getNextElementFromSpace(modificationPoint,operationType);
        } else {
            selectedIngredient = this.getNextElementFromSpace(modificationPoint, type,operationType);
        }

        return new Ingredient(selectedIngredient, null);

    }

}
