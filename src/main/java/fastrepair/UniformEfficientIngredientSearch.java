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
    protected CtCodeElement getNextStatementFromSpace(ModificationPoint location) {    //ちょっとやばいコードを書くぞ
        List<CtCodeElement> fixSpace = this.ingredientSpace.getIngredients(location.getCodeElement());
        String modifyClassName = location.getCtClass().getSimpleName();
        SourcePosition modifyPosition = location.getCodeElement().getPosition();
        List<AstLocation> locations = sr.getStatements(Util.getSourceCodeFromClassName(modifyClassName), modifyPosition);
        int res=findNextStatement(fixSpace,locations,modifyClassName); //resは
        if(res==fixSpace.size()) {  //indexがsizeと等しいなら検索が終わったとする。その時はspaceからランダムに選択
            int size = fixSpace.size();
            res = RandomManager.nextInt(size);
        }
        return fixSpace.get(res);
    }

    protected int findNextStatement(List<CtCodeElement> space, List<AstLocation> locations, String className) {
        Integer index=indexes.getOrDefault(className,0);
        while(index<space.size()){
            if(isRecommended(space.get(index),locations))
                break;
            index++;
        }
        indexes.put(className,index);
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
        return element.getPosition().getLine()==recommended.startLine &&
                element.getPosition().getColumn()==recommended.startColumn &&
                element.getPosition().getEndLine()==recommended.endLine &&
                element.getPosition().getEndColumn()==recommended.endColumn;
    }



    /**
     * Return a cloned CtStatement from the fix space in a randomly way
     *
     * @return
     */
    protected CtCodeElement getNextElementFromSpace(ModificationPoint location) {
        CtCodeElement originalPicked = getNextStatementFromSpace(location);
        CtCodeElement cloned = MutationSupporter.clone(originalPicked);
        return cloned;
    }

    protected CtCodeElement getNextElementFromSpace(ModificationPoint location, String type) {
        List<CtCodeElement> elements = this.ingredientSpace.getIngredients(location.getCodeElement(), type);
        if (elements == null)
            return null;
        return getNextStatementFromSpace(location);
    }

    @Override
    public Ingredient getFixIngredient(ModificationPoint modificationPoint, AstorOperator operationType) {

        String type = null;
        if (operationType instanceof ReplaceOp) {
            type = modificationPoint.getCodeElement().getClass().getSimpleName();
        }

        CtElement selectedIngredient = null;
        if (type == null) {
            selectedIngredient = this.getNextElementFromSpace(modificationPoint);
        } else {
            selectedIngredient = this.getNextElementFromSpace(modificationPoint, type);
        }

        return new Ingredient(selectedIngredient, null);

    }

}
