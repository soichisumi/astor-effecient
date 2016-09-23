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
import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.astor.core.setup.RandomManager;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import spoon.reflect.code.CtCodeElement;
import spoon.reflect.declaration.CtElement;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by s-sumi on 16/08/01.
 */
public class UniformEfficientIngredientSearch extends AstorCtSearchStrategy {
    public StatementRecommender sr;


    public UniformEfficientIngredientSearch(IngredientSpace space) throws Exception {
        super(space);
        sr = new StatementRecommender();
        logger.setLevel(Level.DEBUG);
    }

    public Map<String, Integer> indexes = new HashMap<>();

    private Logger logger = Logger.getLogger(UniformEfficientIngredientSearch.class.getName());


    /**
     * Listのすべてを使いきったかどうかはどうやってわかるんだ？
     * ->Searchはランダムな順に取り出すことのみを担当する。Strategyでattempt が　fix space sizeを超えたかどうかを判定する
     * まずシャッフル->予測結果に合致するものを先頭に->取り出す
     */
    protected CtCodeElement getNextStatementFromSpace(ModificationPoint location, AstorOperator operationType, boolean isScopePackage) {    //ちょっとやばいコードを書くぞ
        List<CtCodeElement> fixSpace = this.ingredientSpace.getIngredients(location.getCodeElement());

        String modifyClassName = location.getCtClass().getSimpleName();
        System.out.println("predict for: " + modifyClassName);

        Set<AstLocation> locations = sr.getStatements(Util.getSourceCodeFromClassName(modifyClassName), location);
        System.out.println("get ingredient from fixspace....");
        System.out.println("get ingredient for " + location.getCodeElement().toString() + " " + operationType.toString());
        int res = locations == null ?
                fixSpace.size() :
                findNextStatement(fixSpace, locations,
                        modifyClassName +
                                location.getCodeElement().getPosition().getLine() + "," +
                                location.getCodeElement().getPosition().getColumn() + "," +
                                location.getCodeElement().getPosition().getEndLine() + "," +
                                location.getCodeElement().getPosition().getEndColumn() + "," + operationType.toString());
       // if (location.getCodeElement().getPosition().getLine() == 138 && operationType.toString().toLowerCase().contains("replace"))
       //     dumpStatements(fixSpace, locations);
        if (res == fixSpace.size()) {  //indexがsizeと等しいなら検索が終わったとする。その時はspaceからランダムに選択
            System.out.println("use default search");
            this.isRecommendedStmt = false;
            int size = fixSpace.size();
            res = RandomManager.nextInt(size);
        } else {
            this.isRecommendedStmt = true;
            System.out.println("use prediction based search");
        }
        System.out.println("use :" + fixSpace.get(res).toString());
        return fixSpace.get(res);
    }

    protected CtCodeElement getNextStatementFromSpaceSameFirst(ModificationPoint location, AstorOperator operationType, boolean isScopePackage) {    //ちょっとやばいコードを書くぞ
        List<CtCodeElement> fixSpace = this.ingredientSpace.getIngredients(location.getCodeElement());
        fixSpace=sortFixSpace(fixSpace);
        String modifyClassName = location.getCtClass().getSimpleName();
        System.out.println("predict for: " + modifyClassName);

        Set<AstLocation> locations = sr.getStatements(Util.getSourceCodeFromClassName(modifyClassName), location);
        System.out.println("get ingredient from fixspace....");
        System.out.println("get ingredient for " + location.getCodeElement().toString() + " " + operationType.toString());
        int res = locations == null ?
                fixSpace.size() :
                findNextStatement(fixSpace, locations,
                        modifyClassName +
                                location.getCodeElement().getPosition().getLine() + "," +
                                location.getCodeElement().getPosition().getColumn() + "," +
                                location.getCodeElement().getPosition().getEndLine() + "," +
                                location.getCodeElement().getPosition().getEndColumn() + "," + operationType.toString());
        // if (location.getCodeElement().getPosition().getLine() == 138 && operationType.toString().toLowerCase().contains("replace"))
        //     dumpStatements(fixSpace, locations);
        if (res == fixSpace.size()) {  //indexがsizeと等しいなら検索が終わったとする。その時はspaceからランダムに選択
            System.out.println("use default search");
            this.isRecommendedStmt = false;
            int size = fixSpace.size();
            res = RandomManager.nextInt(size);
        } else {
            this.isRecommendedStmt = true;
            System.out.println("use prediction based search");
        }
        System.out.println("use :" + fixSpace.get(res).toString());
        return fixSpace.get(res);
    }
    List<CtCodeElement> sortFixSpace(List<CtCodeElement> fixSpace){ //onaji bun wo matomete ooinowo ueni suru
        Map<CtCodeElement,Integer> map=new HashMap<>();
        for (CtCodeElement e:fixSpace){
            Integer v=map.getOrDefault(e,0)+1;
            map.put(e,v);
        }
        List<Pair<CtCodeElement,Integer>> list= map.entrySet().stream().map(e -> new ImmutablePair<>(e.getKey(), e.getValue())).collect(Collectors.toList());
        Collections.sort(list, (o1, o2) -> o2.getValue()-o1.getValue());//descending order
        return list.stream().map(Pair::getKey).collect(Collectors.toList());
    }

    protected CtCodeElement getNextStatementFromSpaceSorted(ModificationPoint location, AstorOperator operationType, boolean isScopePackage) {    //ちょっとやばいコードを書くぞ
        List<CtCodeElement> fixSpace = this.ingredientSpace.getIngredients(location.getCodeElement());

        String modifyClassName = location.getCtClass().getSimpleName();
        System.out.println("predict for: " + modifyClassName);
        int res=0;
        for (int dist = 0; dist < 4; dist++) {
            Set<AstLocation> locations = sr.getStatementsDistend(Util.getSourceCodeFromClassName(modifyClassName), location,dist);
            System.out.println("get ingredient from fixspace....");
            System.out.println("get ingredient for " + location.getCodeElement().toString() + " " + operationType.toString());
            res = locations == null ?
                    fixSpace.size() :
                    findNextStatement(fixSpace, locations,
                            String.valueOf(dist) +
                            modifyClassName +
                                    location.getCodeElement().getPosition().getLine() + "," +
                                    location.getCodeElement().getPosition().getColumn() + "," +
                                    location.getCodeElement().getPosition().getEndLine() + "," +
                                    location.getCodeElement().getPosition().getEndColumn() + "," + operationType.toString());
            //if (location.getCodeElement().getPosition().getLine() == 138 && operationType.toString().toLowerCase().contains("replace"))
            //    dumpStatements(fixSpace, locations);
            if (res == fixSpace.size()) {  //indexがsizeと等しいなら検索が終わったとする。その時はspaceからランダムに選択
                System.out.println("use default search");
                this.isRecommendedStmt = false;
                int size = fixSpace.size();
                res = RandomManager.nextInt(size);
            } else {
                this.isRecommendedStmt = true;
                System.out.println("use prediction based search");
                break;
            }
        }
        System.out.println("use :" + fixSpace.get(res).toString());
        return fixSpace.get(res);
    }
    protected CtCodeElement getNextStatementFromSpaceSortedAndSameFirst(ModificationPoint location, AstorOperator operationType, boolean isScopePackage) {    //ちょっとやばいコードを書くぞ
        List<CtCodeElement> fixSpace = this.ingredientSpace.getIngredients(location.getCodeElement());
        fixSpace=sortFixSpace(fixSpace);
        String modifyClassName = location.getCtClass().getSimpleName();
        System.out.println("predict for: " + modifyClassName);
        int res=0;
        for (int dist = 0; dist < 4; dist++) {
            Set<AstLocation> locations = sr.getStatementsDistend(Util.getSourceCodeFromClassName(modifyClassName), location,dist);
            System.out.println("get ingredient from fixspace....");
            System.out.println("get ingredient for " + location.getCodeElement().toString() + " " + operationType.toString());
            res = locations == null ?
                    fixSpace.size() :
                    findNextStatement(fixSpace, locations,
                            String.valueOf(dist) +
                                    modifyClassName +
                                    location.getCodeElement().getPosition().getLine() + "," +
                                    location.getCodeElement().getPosition().getColumn() + "," +
                                    location.getCodeElement().getPosition().getEndLine() + "," +
                                    location.getCodeElement().getPosition().getEndColumn() + "," + operationType.toString());
            //if (location.getCodeElement().getPosition().getLine() == 138 && operationType.toString().toLowerCase().contains("replace"))
            //    dumpStatements(fixSpace, locations);
            if (res == fixSpace.size()) {  //indexがsizeと等しいなら検索が終わったとする。その時はspaceからランダムに選択
                System.out.println("use default search");
                this.isRecommendedStmt = false;
                int size = fixSpace.size();
                res = RandomManager.nextInt(size);
            } else {
                this.isRecommendedStmt = true;
                System.out.println("use prediction based search");
                break;
            }
        }
        System.out.println("use :" + fixSpace.get(res).toString());
        return fixSpace.get(res);
    }

    public void dumpStatements(List<CtCodeElement> fixSpace, Set<AstLocation> locations) {
        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(new File("Experiment/DumpStatements.txt"), true)));
            pw.write("statements in fixspace: " + fixSpace.size() + " \n");
            for (int i = 0; i < fixSpace.size(); i++) {
                pw.write(String.valueOf(i) + ": " + fixSpace.get(i).toString());
                pw.write("\n");
            }
            pw.write("\n\nstatements recommended: \n");
            int count = 0;
            for (int i = 0; i < fixSpace.size(); i++) {
                if (isRecommended(fixSpace.get(i), locations)) {
                    pw.write(String.valueOf(i) + ": " + fixSpace.get(i).toString());
                    pw.write("\n");
                    count++;
                }
            }
            pw.write("count=" + String.valueOf(count));
            pw.close();
            throw new RuntimeException();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected int findNextStatement(List<CtCodeElement> space, Set<AstLocation> locations, String className) {
        Integer index = indexes.getOrDefault(className, 0);
        while (index < space.size()) {
            if (isRecommended(space.get(index), locations)) {
                System.out.println("[ " + space.get(index).toString() + " ] is recommended statement");
                break;
            }
            index++;
        }
        indexes.put(className, index == space.size() ? space.size() : index + 1);
        return index;
    }

    protected int findNextStatement_distanceBased(List<CtCodeElement> space, Set<AstLocation> locations, String className) {
        Integer index = indexes.getOrDefault(className, 0);
        while (index < space.size()) {
            if (isRecommended(space.get(index), locations)) {
                System.out.println("[ " + space.get(index).toString() + " ] is recommended statement");
                break;
            }
            index++;
        }
        indexes.put(className, index == space.size() ? space.size() : index + 1);
        return index;
    }


    private boolean isRecommended(CtCodeElement codeElement, Set<AstLocation> recommended) {
        for (AstLocation l : recommended) {
            if (sameStatement(codeElement, l))
                return true;
        }
        return false;
    }

    private boolean sameStatement(CtCodeElement element, AstLocation recommended) {
        /*return element.getPosition().getLine()==recommended.startLine &&
                element.getPosition().getColumn()==recommended.startColumn &&
                element.getPosition().getEndLine()==recommended.endLine &&
                element.getPosition().getEndColumn()==recommended.endColumn;*/
        return element.getPosition().getLine() == recommended.startLine &&
                element.getPosition().getEndLine() == recommended.endLine;
    }


    /**
     * Return a cloned CtStatement from the fix space in a randomly way
     *
     * @return
     */
    //sorted denaitokiha KOTTI!!!!!!!!!!
    /*protected CtCodeElement getNextElementFromSpace(ModificationPoint location, AstorOperator operationType) {
        CtCodeElement originalPicked = getNextStatementFromSpace(location, operationType, "package".equals(ConfigurationProperties.properties.getProperty("scope")));
        CtCodeElement cloned = MutationSupporter.clone(originalPicked);
        return cloned;
    }

    protected CtCodeElement getNextElementFromSpace(ModificationPoint location, String type, AstorOperator operationType) {
        List<CtCodeElement> elements = this.ingredientSpace.getIngredients(location.getCodeElement(), type);
        if (elements == null)
            return null;
        return getNextStatementFromSpace(location, operationType, "package".equals(ConfigurationProperties.properties.getProperty("scope")));
    }*/
    //Sorted!!!!!!
    protected CtCodeElement getNextElementFromSpace(ModificationPoint location, AstorOperator operationType) {
        CtCodeElement originalPicked = getNextStatementFromSpaceSorted(location, operationType, "package".equals(ConfigurationProperties.properties.getProperty("scope")));
        CtCodeElement cloned = MutationSupporter.clone(originalPicked);
        return cloned;
    }

    protected CtCodeElement getNextElementFromSpace(ModificationPoint location, String type, AstorOperator operationType) {
        List<CtCodeElement> elements = this.ingredientSpace.getIngredients(location.getCodeElement(), type);
        if (elements == null)
            return null;
        return getNextStatementFromSpaceSorted(location, operationType, "package".equals(ConfigurationProperties.properties.getProperty("scope")));
    }

    @Override   //実際はこれを使っている
    public Ingredient getFixIngredient(ModificationPoint modificationPoint, AstorOperator operationType) {

        String type = null;
        if (operationType instanceof ReplaceOp) {
            type = modificationPoint.getCodeElement().getClass().getSimpleName();
        }

        CtElement selectedIngredient = null;
        if (type == null) {
            selectedIngredient = this.getNextElementFromSpace(modificationPoint, operationType);
        } else {
            selectedIngredient = this.getNextElementFromSpace(modificationPoint, type, operationType);
        }

        return new Ingredient(selectedIngredient, null);

    }

}
