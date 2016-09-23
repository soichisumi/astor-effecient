package fastrepair.yousei.propose;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import fastrepair.yousei.GeneralUtil;
import fastrepair.yousei.propose.stmtcollector.AstCollectorVisitor;
import fastrepair.yousei.propose.stmtcollector.AstLocation;
import fastrepair.yousei.propose.stmtcollector.AstVector;
import fastrepair.yousei.util.NodeClasses4Java;
import fr.inria.astor.core.setup.ConfigurationProperties;
import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import spoon.reflect.cu.SourcePosition;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.lazy.IBk;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static fastrepair.yousei.GeneralUtil.*;


/**
 * Created by s-sumi on 16/07/23.
 * 提案手法用のUtil集
 */
public class Util {

    public static int smallchange=0;

    public static String getSourceCodeFromClassName(String className){
        List<Path> filesinFolder=null;
        try {
            filesinFolder = Files.walk(Paths.get(ConfigurationProperties.properties.getProperty("location")))
                                .filter(Files::isRegularFile)
                                .filter(p->p.endsWith(className+".java"))
                                .collect(Collectors.toList());
        }catch (Exception e){
            e.printStackTrace();
            return "";
        }
        if (filesinFolder.size()!=1)
            System.out.println("there is some same file");

        try {
            return new String(Files.readAllBytes(filesinFolder.get(0)));
        } catch (IOException e) {
            throw new RuntimeException("exception");
        }
//        StringBuilder sb=new StringBuilder();
//        try(Stream<String> stream=Files.lines(filesinFolder.get(0))){
//            stream.forEach(sb::append);
//        }catch (IOException e){
//            e.printStackTrace();
//        }
//        return sb.toString();
    }
    public static String getSourceCodeFromQualifiedName(String qualifiedName){
        List<Path> filesinFolder=null;
        try {
            filesinFolder = Files.walk(Paths.get(ConfigurationProperties.properties.getProperty("location")))
                    .filter(Files::isRegularFile)
                    .filter(p->p.endsWith(qualifiedName.replace(".","/")+".java"))
                    .collect(Collectors.toList());
        }catch (Exception e){
            e.printStackTrace();
            return "";
        }
        if (filesinFolder.size()!=1)
            System.out.println("there is some same file");

        try {
            return new String(Files.readAllBytes(filesinFolder.get(0)));
        } catch (IOException e) {
            throw new RuntimeException("exception");
        }
//        StringBuilder sb=new StringBuilder();
//        try(Stream<String> stream=Files.lines(filesinFolder.get(0))){
//            stream.forEach(sb::append);
//        }catch (IOException e){
//            e.printStackTrace();
//        }
//        return sb.toString();
    }

    /**
     * 予測結果 - 元の状態ベクトル + 変更箇所の状態ベクトルをもつ文の集合を返す
     * @param source
     * @param vector
     * @param sp   NULLABLE
     * @return
     */
    public static Set<AstLocation> getASTLocations(String source, AstVector vector, SourcePosition sp) throws Exception{   //boolean packageと、そのパース結果を追加
        Multimap<AstVector, AstLocation> result = MultimapBuilder.hashKeys().arrayListValues().build();//ソースコードの文とその場所の集合
        ASTParser parser = ASTParser.newParser(AST.JLS8);

        parser.setSource(source.toCharArray());
        CompilationUnit unit = (CompilationUnit)parser.createAST(null);/*example ではnew NullProgressMonitor(). jdt.core3.10にしかない*/
        AstCollectorVisitor visitor = new AstCollectorVisitor(source, unit);
        unit.accept(visitor); if(unit.getProblems().length>0) System.out.println("Conpilation problem occured");
        for (Map.Entry<AstVector, AstLocation> a : visitor.asts.entries()) {
            result.put(a.getKey(), a.getValue());
        }
        int[] res = new int[92];
        {
            List<Map.Entry<AstVector, AstLocation>> vectors = new ArrayList<>();//replaceする文とおなじ行の文のリスト(複数存在する可能性がある)
            if (sp != null)
                visitor.asts.entries().forEach((e) -> {
                    if (sameLine(e.getValue(), sp))
                        vectors.add(e);
                });

            if(vectors.size()==0){
                System.out.println("perhaps couldn't parse source code."); return null;
            }

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
        //printVectors(original,predicted,replaced,res);  //finally is equal to replaced when original is equal to predicted

        Set<AstLocation> statements=new HashSet<>();
        //statements.addAll(result.get(new AstVector(res)));
        statements.addAll(getSimilarStatementsWithThreshold(visitor.asts,res,3));
        //getSimilarStatements()

        return statements;
    }
    public static Set<AstLocation> getASTLocationsDisted(String source, AstVector vector, SourcePosition sp,int dist) throws Exception{   //boolean packageと、そのパース結果を追加
        Multimap<AstVector, AstLocation> result = MultimapBuilder.hashKeys().arrayListValues().build();//ソースコードの文とその場所の集合
        ASTParser parser = ASTParser.newParser(AST.JLS8);

        parser.setSource(source.toCharArray());
        CompilationUnit unit = (CompilationUnit)parser.createAST(null);/*example ではnew NullProgressMonitor(). jdt.core3.10にしかない*/
        AstCollectorVisitor visitor = new AstCollectorVisitor(source, unit);
        unit.accept(visitor); if(unit.getProblems().length>0) System.out.println("Conpilation problem occured");
        for (Map.Entry<AstVector, AstLocation> a : visitor.asts.entries()) {
            result.put(a.getKey(), a.getValue());
        }
        int[] res = new int[92];
        {
            List<Map.Entry<AstVector, AstLocation>> vectors = new ArrayList<>();//replaceする文とおなじ行の文のリスト(複数存在する可能性がある)
            if (sp != null)
                visitor.asts.entries().forEach((e) -> {
                    if (sameLine(e.getValue(), sp))
                        vectors.add(e);
                });

            if(vectors.size()==0){
                System.out.println("perhaps couldn't parse source code."); return null;
            }

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
        //printVectors(original,predicted,replaced,res);  //finally is equal to replaced when original is equal to predicted

        Set<AstLocation> statements=new HashSet<>();
        //statements.addAll(result.get(new AstVector(res)));
        statements.addAll(getSimilarStatements(visitor.asts,res,dist));
        //getSimilarStatements()

        return statements;
    }
    public static boolean containStatementWhich(List<AstLocation> statements, int startLine, int endLine){
        boolean flag=false;
        for(int i=0;i<statements.size();i++){
            if(statements.get(i).startLine==startLine && statements.get(i).endLine==endLine){
                flag=true;
            }
        }
        return flag;
    }

    public static List<AstLocation> getSimilarStatementsRanged(Multimap<AstVector, AstLocation> asts, int[] query, int distance){
        List<AstLocation> res=new ArrayList<>();
        for(int i=1;i<=distance;i++){
            res.addAll(getSimilarStatements(asts,query,i));
        }
        return res;
    }

    public static List<AstLocation> getSimilarStatements(Multimap<AstVector, AstLocation> asts, int[] query, int distance){
        List<AstLocation> res=new ArrayList<>();
        for(Map.Entry<AstVector,AstLocation> e:asts.entries()){
            if(getDistance(e.getKey(),query)==distance)
                res.add(e.getValue());
        }
        return res;
    }
    public static List<AstLocation> getSimilarStatementsWithThreshold(Multimap<AstVector, AstLocation> asts, int[] query, int distance){
        List<AstLocation> res=new ArrayList<>();
        for(Map.Entry<AstVector,AstLocation> e:asts.entries()){
            if(getDistance(e.getKey(),query)<=distance)
                res.add(e.getValue());
        }
        return res;
    }
    public static int getDistance(AstVector a,int[] b){
        int res=0;
        int[] tmp=a.getArray();
        if(tmp.length!=b.length)
            throw new RuntimeException();
        for(int i=0;i<tmp.length;i++){
            res+=Math.abs(tmp[i]-b[i]);
        }
        return res;
    }

    public static void printVectors(List<Integer> ori,int[] pred,int[] replaced, int[] res){
        System.out.println("state vector discription:");
        System.out.print("original: \t");       printSingleVector(ori);
        System.out.print("predicted:\t");       printSingleVector(pred);
        System.out.print("replaced: \t");       printSingleVector(replaced);
        System.out.print("finally:  \t");       printSingleVector(res);
        System.out.println();
    }
    public static void printSingleVector(List<Integer> vector){
        StringBuilder sb=new StringBuilder();
        sb.append("[");
        for(int i=0;i<vector.size();i++){
            sb.append(vector.get(i));//append内でString.valueOfしてる
            if(i!=vector.size()-1)
                sb.append(",");
        }
        sb.append("]");
        System.out.println(sb.toString());
    }
    public static void printSingleVector(int[] vector){
        StringBuilder sb=new StringBuilder();
        sb.append("[");
        for(int i=0;i<vector.length;i++){
            sb.append(vector[i]);//append内でString.valueOfしてる
            if(i!=vector.length-1)
                sb.append(",");
        }
        sb.append("]");
        System.out.println(sb.toString());
    }


    public static int statementPlausibility(AstLocation loc,SourcePosition sp){ //0 is the best
        return Math.abs(sp.getColumn()-loc.startColumn)+Math.abs(sp.getEndColumn()-loc.endColumn);
    }

    public static boolean sameLine(AstLocation loc, SourcePosition position){
        return loc.startLine==position.getLine() && loc.endLine==position.getEndLine();
    }

    public static AstVector toAstVector(List<Integer> vector){
        if (vector == null || vector.size() != 92) {
            throw new IllegalArgumentException();
        }
        int[] res=new int[92];
        for (int i=0;i<res.length;i++){
            res[i]=vector.get(i);
        }
        return new AstVector(res);
    }


    //create arff having only 1 instance, maybe being all postvector ? is ok
    public static File createSouceArff4VectorPrediction(String source)throws Exception{
        NodeClasses4Java nc = new NodeClasses4Java();
        File tmpFile = File.createTempFile("bugSource4VectorPrediction", ".arff", GeneralUtil.workingDir);
        BufferedWriter bw = new BufferedWriter(new FileWriter(tmpFile));
        bw.write("@relation StateVector");
        bw.newLine();
        bw.newLine();
        for (Map.Entry<String, Integer> e : nc.dictionary4j.entrySet()) {
            bw.write("@attribute " + e.getKey() + " numeric");
            bw.newLine();
        }
        for (Map.Entry<String, Integer> e : nc.dictionary4j.entrySet()) {
            bw.write("@attribute " + e.getKey() + "2 numeric");
            bw.newLine();
        }
        bw.newLine();

        bw.write("@data");
        bw.newLine();

        List<Integer> sourceVector= GeneralUtil.getSourceVector4Java(source,".java");//4java ttearundakara javasikanakunai

        writeVector(bw, sourceVector);
        bw.write(",");
        writeQuestions(bw,sourceVector);
        bw.newLine();
        bw.close();
        return tmpFile;
    }

    public static File createSourceArff4SizePrediction(String source)throws Exception{
        NodeClasses4Java nc = new NodeClasses4Java();
        File tmpFile = File.createTempFile("bugSource4SizePrediction", ".arff", GeneralUtil.workingDir);
        BufferedWriter bw = new BufferedWriter(new FileWriter(tmpFile));
        bw.write("@relation StateVector");
        bw.newLine();
        bw.newLine();
        for (Map.Entry<String, Integer> e : nc.dictionary4j.entrySet()) {
            bw.write("@attribute " + e.getKey() + " numeric");
            bw.newLine();
        }

        bw.write("@attribute size {big,small}");
        bw.newLine();

        bw.newLine();

        bw.write("@data");
        bw.newLine();

        List<Integer> sourceVector=GeneralUtil.getSourceVector4Java(source,".java");//4java ttearundakara javasikanakunai

        writeVector(bw, sourceVector);
        bw.write(",big");
        bw.newLine();
        bw.close();
        return tmpFile;
    }

    public static File genealogy2Arff4VectorPrediction(List<List<Integer>> preVector, List<List<Integer>> postVector) throws Exception {
        smallchange=0;
        NodeClasses4Java nc = new NodeClasses4Java();
        File tmpFile = File.createTempFile("genealogy4VectorPrediction", ".arff", GeneralUtil.workingDir);
        BufferedWriter bw = new BufferedWriter(new FileWriter(tmpFile));
        bw.write("@relation StateVector");
        bw.newLine();
        bw.newLine();
        for (Map.Entry<String, Integer> e : nc.dictionary4j.entrySet()) {
            bw.write("@attribute " + e.getKey() + " numeric");
            bw.newLine();
        }
        for (Map.Entry<String, Integer> e : nc.dictionary4j.entrySet()) {
            bw.write("@attribute " + e.getKey() + "2 numeric");
            bw.newLine();
        }
        bw.newLine();

        bw.write("@data");
        bw.newLine();
        for (int i = 0; i < preVector.size(); i++) {
            if (GeneralUtil.diffIsBig(preVector.get(i), postVector.get(i), GeneralUtil.SMALLTHRESHOLD))
                continue;

            smallchange++;

            writeVector(bw, preVector.get(i));
            bw.write(",");
            writeVector(bw, postVector.get(i));
            bw.newLine();
        }

        bw.close();
        return tmpFile;
    }

    /**
     * returns Instances::numAttribute / 2 .
     * @param arffData
     * @return
     * @throws IOException
     */
    public static int getNumAttribute(File arffData) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(arffData));
        Instances instances = new Instances(br);
        int numAttribute = instances.numAttributes() / 2;//arff has 2 vector in an instance
        br.close();
        return numAttribute;
    }

    /**
     * 各filteredDataに対して同じattributeを持つinstanceを作る
     * @param bugArff
     * @param filteredData
     * @return
     * @throws Exception
     */
    public static List<Instance> getAttrSelectedData(File bugArff,List<Instances> filteredData)throws Exception{
        List<Instances> tmp=new ArrayList<>();

        for(int i=0;i<filteredData.size();i++){
            BufferedReader br2 = new BufferedReader(new FileReader(bugArff));
            Instances bugInstances = new Instances(br2);
            br2.close();

            Instances filteredInstances=filteredData.get(i);
            tmp.add(makeSameAttrData(filteredInstances,bugInstances));
        }

        List<Instance> res=new ArrayList<>();
        for(Instances i:tmp){
            if(i.numInstances()!=1)
                throw new Exception("there is 2 or more instances. what happened.");

            res.add(i.instance(0));
        }

        return res;
    }

    /**
     * ２つのInstancesを受け取って、同じattribute名のattributeを残す
     * @param valid
     * @param manipulate
     * @return
     * @throws Exception
     */
    public static Instances makeSameAttrData(Instances valid,Instances manipulate)throws Exception{
        Instances copied=new Instances(manipulate);

        Remove remove=new Remove();
        StringBuilder remaining =new StringBuilder();
        for(int i=0;i<valid.numAttributes();i++){ //make a string like: 1,3,5,6,7-10
            /*remaining.append(
                    String.valueOf(getAttrNum(valid.attribute(i),copied)+1)
                            +",");*/
            for(int j=0;j<copied.numAttributes();j++){
                if(Objects.equals(valid.attribute(i).name(),copied.attribute(j).name())) {
                    if(remaining.length()!=0)//最初以外の数字の前にはカンマ
                        remaining.append(",");
                    remaining.append(Integer.toString(j + 1));
                }

            }
        }
        //remaining.append(copied.numAttributes());//add classindex
        remove.setAttributeIndices(remaining.toString());
        remove.setInvertSelection(true);    //written index will be selected
        remove.setInputFormat(copied);

        Instances res=Filter.useFilter(copied,remove);

        /*Add add=new Add();
        add.setAttributeIndex("last");
        add.setAttributeName(valid.attribute(valid.));*/

        res.setClassIndex(res.numAttributes()-1);
        if(valid.instance(0).numAttributes()!=res.instance(0).numAttributes()
                || !hasSameAttributes(valid.instance(0),res.instance(0)))
            throw new Exception("here is a bug");

        return res;
    }

    public static boolean hasSameAttributes(Instance a,Instance b){
        boolean flag=true;
        for(int i=0;i<a.numAttributes();i++){
            if(!a.attribute(i).equals(b.attribute(i))){ //attributeはちゃんとequalsを実装している
                flag=false;
                break;
            }
        }
        return flag;
    }

    public static Integer getAttrNum(Attribute attribute,Instances testee)throws Exception{//testee has n attribute and 1 classvalue
        for(int i=0;i<testee.numAttributes()-1;i++)
            if(Objects.equals(testee.attribute(i).name(),attribute.name()))
                return i;

        throw new Exception("an error occured");
    }


    /**
     * 全ての変更から変更の大きさ予測用の学習データを作成する
     * @param preVector
     * @param postVector
     * @return
     * @throws Exception
     */
    public static File genealogy2Arff4SizePrediction(List<List<Integer>> preVector, List<List<Integer>> postVector) throws Exception {
        NodeClasses4Java nc = new NodeClasses4Java();
        File tmpFile = File.createTempFile("genealogy4SizePrediction", ".arff", GeneralUtil.workingDir);
        BufferedWriter bw = new BufferedWriter(new FileWriter(tmpFile));
        bw.write("@relation StateVector");
        bw.newLine();
        bw.newLine();
        for (Map.Entry<String, Integer> e : nc.dictionary4j.entrySet()) {
            bw.write("@attribute " + e.getKey() + " numeric");
            bw.newLine();
        }
        bw.write("@attribute size {big,small}");
        bw.newLine();

        bw.newLine();

        bw.write("@data");
        bw.newLine();
        for (int i = 0; i < preVector.size(); i++) {
            writeVector(bw, preVector.get(i)); bw.write(",");
            //writeVector(bw, postVector.get(i)); bw.write(",");

            if(diffIsBig(preVector.get(i),postVector.get(i),GeneralUtil.SMALLTHRESHOLD)){
                bw.write("big");
            }else{
                bw.write("small");
            }
            bw.newLine();
        }

        bw.close();
        return tmpFile;
    }
}
