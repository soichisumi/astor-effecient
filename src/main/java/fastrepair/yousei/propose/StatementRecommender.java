package fastrepair.yousei.propose;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Multiset;
import fastrepair.yousei.GeneralUtil;
import fastrepair.yousei.propose.stmtcollector.AstCollectorVisitor;
import fastrepair.yousei.propose.stmtcollector.AstLocation;
import fastrepair.yousei.propose.stmtcollector.AstVector;
import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.setup.ConfigurationProperties;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import spoon.reflect.cu.*;
import spoon.reflect.cu.CompilationUnit;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static fastrepair.yousei.propose.Util.*;

/**
 * Created by s-sumi on 16/07/23.
 */
public class StatementRecommender {
    private String reposPath;
    /**
     * 短縮形でもフルでもよい
     */
    private String bugRevisionId;
    private Repository repository;

    private Map<String, Multimap<AstVector, AstLocation>> ingredients = new HashMap<>();//Package,<vector,location>
    private Multimap<AstVector, AstLocation> globalIngredients = null;

    public StatementRecommender() throws Exception {

        this.reposPath = ConfigurationProperties.properties.getProperty("reposPath");
        this.bugRevisionId = ConfigurationProperties.properties.getProperty("bugRevisionId");

        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        this.repository = builder
                //.setGitDir(new File(reposPath + "/" + Constants.DOT_GIT))
                .setGitDir(new File(reposPath))
                .readEnvironment()
                .findGitDir()
                .build();
        if("global".equals(ConfigurationProperties.getProperty("scope"))){
            initGlobalIngredients();
        }
    }


    public Set<AstLocation> getStatements(String bugSourceCode) {
        return getStatements(bugSourceCode, null);
    }

    //SourcePosition==nullならadd or removeとして扱う
    public Set<AstLocation> getStatements(String bugSourceCode, ModificationPoint location) {

        System.out.println("process " + reposPath);

        System.out.println("predict the size of next change");
        ChangeSizePredictorSingleton csp = ChangeSizePredictorSingleton.getInstance(this.repository);
        try {
            if (csp.nextChangeSizeIsSmall(bugSourceCode)) {
                System.out.println("OK. next change is small\n");

                System.out.println("predict next source");
                SourceVectorPredictorSingleton svp = SourceVectorPredictorSingleton.getInstance(this.repository);
                List<Double> resDouble = svp.getNextVector(bugSourceCode);
                List<Integer> resInt = resDouble.stream().map(d -> (int) Math.round(d)).collect(Collectors.toList());/*
                Set<AstLocation> locations = "local".equals(ConfigurationProperties.getProperty("scope")) ?
                        Util.getASTLocations(bugSourceCode, Util.toAstVector(resInt), location.getCodeElement().getPosition()) :
                        getASTLocationsFromPackage(Util.toAstVector(resInt), location);//予測結果-元の状態ベクトル+変更箇所の状態ベクトルをもつ文の集合を返す.文の定義はFastAstor側*/
                Set<AstLocation> locations = null;
                switch (ConfigurationProperties.getProperty("scope")) {
                    case "local":
                        locations = Util.getASTLocations(bugSourceCode, Util.toAstVector(resInt), location.getCodeElement().getPosition());
                        break;
                    case "package":
                        locations = getASTLocationsFromPackage(Util.toAstVector(resInt), location);
                        break;
                    case "global":
                        locations=getASTLocationsFromGlobal(Util.toAstVector(resInt),location);
                        break;
                    default:
                        throw new RuntimeException("scope property only accepts local, package and global");
                }
                System.out.println("prediction done");
                System.out.println("prediction size is: " + locations.size());
                return locations;

            } else {
                System.out.println("next change is Big. conduct ASTOR default search");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Set<AstLocation> getStatementsDistend(String bugSourceCode, ModificationPoint location, int dist) {

        System.out.println("process " + reposPath);

        System.out.println("predict the size of next change");
        ChangeSizePredictorSingleton csp = ChangeSizePredictorSingleton.getInstance(this.repository);
        try {
            if (csp.nextChangeSizeIsSmall(bugSourceCode)) {
                System.out.println("OK. next change is small\n");

                System.out.println("predict next source");
                SourceVectorPredictorSingleton svp = SourceVectorPredictorSingleton.getInstance(this.repository);
                List<Double> resDouble = svp.getNextVector(bugSourceCode);
                List<Integer> resInt = resDouble.stream().map(d -> (int) Math.round(d)).collect(Collectors.toList());/*
                Set<AstLocation> locations = "local".equals(ConfigurationProperties.getProperty("scope")) ?
                        Util.getASTLocations(bugSourceCode, Util.toAstVector(resInt), location.getCodeElement().getPosition()) :
                        getASTLocationsFromPackage(Util.toAstVector(resInt), location);//予測結果-元の状態ベクトル+変更箇所の状態ベクトルをもつ文の集合を返す.文の定義はFastAstor側*/
                Set<AstLocation> locations = null;
                switch (ConfigurationProperties.getProperty("scope")) {
                    case "local":
                        locations = Util.getASTLocationsDisted(bugSourceCode, Util.toAstVector(resInt), location.getCodeElement().getPosition(),dist);
                        break;
                    case "package":
                        locations = getASTLocationsFromPackageDistened(Util.toAstVector(resInt), location,dist);
                        break;
                    case "global":
                        locations=getASTLocationsFromGlobalDistend(Util.toAstVector(resInt),location,dist);
                        break;
                    default:
                        throw new RuntimeException("scope property only accepts local, package and global");
                }
                System.out.println("prediction done");
                System.out.println("prediction size is: " + locations.size());
                return locations;

            } else {
                System.out.println("next change is Big. conduct ASTOR default search");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 予測結果 - 元の状態ベクトル + 変更箇所の状態ベクトルをもつ文の集合を返す
     *
     * @param vector
     * @return
     */
    public Set<AstLocation> getASTLocationsFromPackage(AstVector vector, ModificationPoint location) throws Exception {   //boolean packageと、そのパース結果を追加
        List<CtType<?>> affected = location.getProgramVariant().getAffectedClasses();
        Set<String> packages = new HashSet<>(); //K:package V:statementの情報の集合
        for (CtType<?> ing : affected) {

            CtPackage p = ing.getParent(CtPackage.class);
            packages.add(p.toString());
            if (this.ingredients.containsKey(p.toString()))
                continue;

            this.ingredients.put(p.toString(), MultimapBuilder.hashKeys().arrayListValues().build());
            for (CtType<?> t : p.getTypes()) {
                String source = Util.getSourceCodeFromClassName(t.getSimpleName());
                ASTParser parser = ASTParser.newParser(AST.JLS8);
                parser.setSource(source.toCharArray());
                org.eclipse.jdt.core.dom.CompilationUnit unit = (org.eclipse.jdt.core.dom.CompilationUnit) parser.createAST(null);/*example ではnew NullProgressMonitor(). jdt.core3.10にしかない*/
                AstCollectorVisitor visitor = new AstCollectorVisitor(source, unit);
                unit.accept(visitor);
                if (unit.getProblems().length > 0) System.out.println("compilation problem occurred");

                Multimap<AstVector,AstLocation> m = ingredients.get(p.toString());
                for (Map.Entry<AstVector, AstLocation> a : visitor.asts.entries()) {
                    m.put(a.getKey(), a.getValue());
                }
            }
        }

        String source = Util.getSourceCodeFromClassName(location.getCtClass().getSimpleName());
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        parser.setSource(Util.getSourceCodeFromClassName(location.getCtClass().getSimpleName()).toCharArray());
        org.eclipse.jdt.core.dom.CompilationUnit unit = (org.eclipse.jdt.core.dom.CompilationUnit) parser.createAST(null);/*example ではnew NullProgressMonitor(). jdt.core3.10にしかない*/
        AstCollectorVisitor visitor = new AstCollectorVisitor(source, unit);
        unit.accept(visitor);
        int[] res = getNextAstVector(vector, location, source, visitor);

        Set<AstLocation> statements = new HashSet<>();
        for (CtType c : affected) {
            CtPackage p = c.getParent(CtPackage.class);
            for (Map.Entry<AstVector, AstLocation> e : this.ingredients.get(p.toString()).entries()) {
                if (getDistance(e.getKey(), res) <= 3) {
                    statements.add(e.getValue());
                }
            }
        }

        return statements;
    }
    public Set<AstLocation> getASTLocationsFromPackageDistened(AstVector vector, ModificationPoint location,int dist) throws Exception {   //boolean packageと、そのパース結果を追加
        List<CtType<?>> affected = location.getProgramVariant().getAffectedClasses();
        Set<String> packages = new HashSet<>(); //K:package V:statementの情報の集合
        for (CtType<?> ing : affected) {

            CtPackage p = ing.getParent(CtPackage.class);
            packages.add(p.toString());
            if (this.ingredients.containsKey(p.toString()))
                continue;

            this.ingredients.put(p.toString(), MultimapBuilder.hashKeys().arrayListValues().build());
            for (CtType<?> t : p.getTypes()) {
                String source = Util.getSourceCodeFromClassName(t.getSimpleName());
                ASTParser parser = ASTParser.newParser(AST.JLS8);
                parser.setSource(source.toCharArray());
                org.eclipse.jdt.core.dom.CompilationUnit unit = (org.eclipse.jdt.core.dom.CompilationUnit) parser.createAST(null);/*example ではnew NullProgressMonitor(). jdt.core3.10にしかない*/
                AstCollectorVisitor visitor = new AstCollectorVisitor(source, unit);
                unit.accept(visitor);
                if (unit.getProblems().length > 0) System.out.println("compilation problem occurred");

                Multimap<AstVector,AstLocation> m = ingredients.get(p.toString());
                for (Map.Entry<AstVector, AstLocation> a : visitor.asts.entries()) {
                    m.put(a.getKey(), a.getValue());
                }
            }
        }

        String source = Util.getSourceCodeFromClassName(location.getCtClass().getSimpleName());
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        parser.setSource(Util.getSourceCodeFromClassName(location.getCtClass().getSimpleName()).toCharArray());
        org.eclipse.jdt.core.dom.CompilationUnit unit = (org.eclipse.jdt.core.dom.CompilationUnit) parser.createAST(null);/*example ではnew NullProgressMonitor(). jdt.core3.10にしかない*/
        AstCollectorVisitor visitor = new AstCollectorVisitor(source, unit);
        unit.accept(visitor);
        int[] res = getNextAstVector(vector, location, source, visitor);

        Set<AstLocation> statements = new HashSet<>();
        for (CtType c : affected) {
            CtPackage p = c.getParent(CtPackage.class);
            for (Map.Entry<AstVector, AstLocation> e : this.ingredients.get(p.toString()).entries()) {
                if (getDistance(e.getKey(), res) == dist) {
                    statements.add(e.getValue());
                }
            }
        }

        return statements;
    }


    public Set<AstLocation> getASTLocationsFromGlobal(AstVector vector, ModificationPoint location) throws Exception {   //boolean packageと、そのパース結果を追加
        String source = Util.getSourceCodeFromClassName(location.getCtClass().getSimpleName());
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        parser.setSource(Util.getSourceCodeFromClassName(location.getCtClass().getSimpleName()).toCharArray());
        org.eclipse.jdt.core.dom.CompilationUnit unit = (org.eclipse.jdt.core.dom.CompilationUnit) parser.createAST(null);/*example ではnew NullProgressMonitor(). jdt.core3.10にしかない*/
        AstCollectorVisitor visitor = new AstCollectorVisitor(source, unit);
        unit.accept(visitor);
        int[] res = getNextAstVector(vector, location, source, visitor);

        Set<AstLocation> statements = new HashSet<>();
        for (Map.Entry<AstVector, AstLocation> e : this.globalIngredients.entries()) {
            if (getDistance(e.getKey(), res) <= 3) {
                statements.add(e.getValue());
            }
        }

        return statements;
    }
    public Set<AstLocation> getASTLocationsFromGlobalDistend(AstVector vector, ModificationPoint location,int dist) throws Exception {   //boolean packageと、そのパース結果を追加
        String source = Util.getSourceCodeFromClassName(location.getCtClass().getSimpleName());
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        parser.setSource(Util.getSourceCodeFromClassName(location.getCtClass().getSimpleName()).toCharArray());
        org.eclipse.jdt.core.dom.CompilationUnit unit = (org.eclipse.jdt.core.dom.CompilationUnit) parser.createAST(null);/*example ではnew NullProgressMonitor(). jdt.core3.10にしかない*/
        AstCollectorVisitor visitor = new AstCollectorVisitor(source, unit);
        unit.accept(visitor);
        int[] res = getNextAstVector(vector, location, source, visitor);

        Set<AstLocation> statements = new HashSet<>();
        for (Map.Entry<AstVector, AstLocation> e : this.globalIngredients.entries()) {
            if (getDistance(e.getKey(), res) == dist) {
                statements.add(e.getValue());
            }
        }

        return statements;
    }

    private int[] getNextAstVector(AstVector vector, ModificationPoint location, String source, AstCollectorVisitor visitor) throws Exception {
        int[] res = new int[92];
        {
            List<Map.Entry<AstVector, AstLocation>> vectors = new ArrayList<>();//replaceする文とおなじ行の文のリスト(複数存在する可能性がある)
            SourcePosition sp = location.getCodeElement().getPosition();
            if (sp != null)
                visitor.asts.entries().forEach((e) -> {
                    if (sameLine(e.getValue(), sp))
                        vectors.add(e);
                });
            //if(vectors.size()==0)return null;
            vectors.sort((a, b) -> statementPlausibility(a.getValue(), sp) - statementPlausibility(b.getValue(), sp));//入れ替える文と思われる文を探すためにソート

            List<Integer> original = GeneralUtil.getSourceVector4Java(source, ".java");
            int[] predicted = vector.getArray();/*ArrayUtils.toPrimitive(original.toArray(new Integer[original.size()]));*/
            int[] replaced = vectors.get(0).getKey().getArray();

            for (int i = 0; i < vector.getArray().length; i++) {
                res[i] =/*predicted[i]-original.get(i)+*/replaced[i];
                if (res[i] < 0)
                    res[i] = 0;
            }
        }
        return res;
    }

    void initGlobalIngredients() {
        this.globalIngredients = MultimapBuilder.hashKeys().arrayListValues().build();
        List<Path> filesinFolder = null;
        try {
            filesinFolder = Files.walk(Paths.get(ConfigurationProperties.properties.getProperty("location")))
                    .filter(Files::isRegularFile)
                    .filter(p -> p.endsWith(".java"))
                    .collect(Collectors.toList());
            for (Path p : filesinFolder) {
                Multimap<AstVector, AstLocation> m = getAsts(new String(Files.readAllBytes(p)));
                this.globalIngredients.putAll(m);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }

    }

    boolean allPackagesContained(List<CtType<?>> affected) {
        for (CtType<?> c : affected) {
            CtPackage p = c.getParent(CtPackage.class);
            if (!this.ingredients.containsKey(p.toString()))
                return false;
        }
        return true;
    }

    Multimap<AstVector, AstLocation> getAsts(String sourceCode) {
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        parser.setSource(sourceCode.toCharArray());
        org.eclipse.jdt.core.dom.CompilationUnit unit = (org.eclipse.jdt.core.dom.CompilationUnit) parser.createAST(null);/*example ではnew NullProgressMonitor(). jdt.core3.10にしかない*/
        AstCollectorVisitor visitor = new AstCollectorVisitor(sourceCode, unit);
        unit.accept(visitor);
        return visitor.asts;
    }


}
