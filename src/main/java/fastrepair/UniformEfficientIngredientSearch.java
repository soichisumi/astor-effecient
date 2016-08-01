package fastrepair;

import fr.inria.astor.approaches.jgenprog.operators.ReplaceOp;
import fr.inria.astor.core.entities.Ingredient;
import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.loop.spaces.ingredients.IngredientSpace;
import fr.inria.astor.core.loop.spaces.ingredients.ingredientSearch.AstorCtSearchStrategy;
import fr.inria.astor.core.loop.spaces.ingredients.ingredientSearch.UniformRandomIngredientSearch;
import fr.inria.astor.core.loop.spaces.operators.AstorOperator;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.setup.RandomManager;
import org.apache.log4j.Logger;
import spoon.reflect.code.CtCodeElement;
import spoon.reflect.declaration.CtElement;

import java.util.List;

/**
 * Created by s-sumi on 16/08/01.
 */
public class UniformEfficientIngredientSearch extends AstorCtSearchStrategy {
    public UniformEfficientIngredientSearch(IngredientSpace space) {
        super(space);
    }

    private Logger logger = Logger.getLogger(UniformRandomIngredientSearch.class.getName());


    /**
     * Listのすべてを使いきったかどうかはどうやってわかるんだ？
     * ->Searchはランダムな順に取り出すことのみを担当する。Strategyでattempt が　fix space sizeを超えたかどうかを判定する。
     *
     * @param fixSpace
     * @return
     */
    protected CtCodeElement getNextStatementFromSpace(List<CtCodeElement> fixSpace) {	//これが大元

        int size = fixSpace.size();
        int index = RandomManager.nextInt(size);
        return fixSpace.get(index);

    }

    /**
     * Return a cloned CtStatement from the fix space in a randomly way
     *
     * @return
     */
    protected CtCodeElement getNextElementFromSpace(CtElement location) {
        CtCodeElement originalPicked = getNextStatementFromSpace(this.ingredientSpace.getIngredients(location));
        CtCodeElement cloned = MutationSupporter.clone(originalPicked); //why cloned
        return cloned;
    }

    protected CtCodeElement getNextElementFromSpace(CtElement location, String type) {
        List<CtCodeElement> elements = this.ingredientSpace.getIngredients(location, type);
        if (elements == null)
            return null;
        return getNextStatementFromSpace(elements);
    }

    @Override
    public Ingredient getFixIngredient(ModificationPoint modificationPoint, AstorOperator operationType) {

        String type = null;
        if (operationType instanceof ReplaceOp) {
            type = modificationPoint.getCodeElement().getClass().getSimpleName();
        }

        CtElement selectedIngredient = null;
        if (type == null) {
            selectedIngredient = this.getNextElementFromSpace(modificationPoint.getCodeElement());
        } else {
            selectedIngredient = this.getNextElementFromSpace(modificationPoint.getCodeElement(), type);
        }

        return new Ingredient(selectedIngredient, null);

    }

}
