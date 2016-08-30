package fastrepair.yousei;

import fastrepair.yousei.util.CppSourceAnalyzer;
import fastrepair.yousei.util.JavaSourceAnalyzer;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.unsupervised.attribute.Remove;


import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by s-sumi on 16/07/24.
 */
public class GeneralUtil {
    public static final int SMALLTHRESHOLD = 5;
    public static final File workingDir = new File("WorkingDir");
    public static int smallchange = 0;       //Utilなのに非定数をメンバに持つ激ヤバ実装。不要。

    // Reverseで最古から最新へ
    public static RevWalk getInitializedRevWalk(Repository repo,
                                                RevSort revSort) throws IOException {
        RevWalk rw = new RevWalk(repo);
        AnyObjectId headId;

        headId = repo.resolve(Constants.HEAD);
        RevCommit root = rw.parseCommit(headId);
        rw.sort(revSort);
        rw.markStart(root); // この時点ではHeadをさしている．nextで最初のコミットが得られる．
        return rw;
    }

    public static Map<String, Integer> getSourceVector(String source) throws Exception{
        return getSourceVector(source, ".cpp");
    }

    public static Map<String, Integer> getSourceVector(String source, String suffix) throws Exception{
        File tmpFile = File.createTempFile("tmp", suffix, workingDir);
        BufferedWriter bw = new BufferedWriter(new FileWriter(tmpFile));
        bw.write(source);
        bw.close();
        CppSourceAnalyzer csa = new CppSourceAnalyzer("", "", "");
        csa.setFilePath(tmpFile.getAbsolutePath());
        Map<String, Integer> res = csa.analyzeFile();
        tmpFile.delete();
        return res;
    }

    public static List<Integer> getSourceVector4Java(String source, String suffix) throws Exception {
        File tmpFile = File.createTempFile("tmp", suffix, workingDir);
        BufferedWriter bw = new BufferedWriter(new FileWriter(tmpFile));
        bw.write(source);
        bw.close();
        JavaSourceAnalyzer jsa = new JavaSourceAnalyzer("", "", "");
        jsa.setFilePath(tmpFile.getAbsolutePath());
        List<Integer> res = jsa.analyzeFile();
        tmpFile.delete();
        return res;
    }

    public static void writeVector(BufferedWriter bw, List<Integer> vector) throws IOException {
        for (int i = 0; i < vector.size(); i++) {
            bw.write(vector.get(i).toString());
            if (i != vector.size() - 1)
                bw.write(",");
        }
    }

    /**
     * write '?' as many as vector elements
     * @param bw
     * @param vector
     * @throws IOException
     */
    public static void writeQuestions(BufferedWriter bw,List<Integer> vector)throws IOException{
        for(int i=0;i<vector.size();i++){
            bw.write("?");
            if(i!=vector.size() -1)
                bw.write(",");
        }
    }

    /**
     * ２つ目の状態ベクトルの
     *
     * @param i
     * @param instances
     * @return
     */
    public static Instances removeAttrWithoutI(int i, Instances instances) throws Exception {
        int num = instances.numAttributes() / 2;
        int counter = 0;

        Remove remove = new Remove();
        remove.setAttributeIndices("1-" + Integer.toString(num) + "," + Integer.toString(num + i + 1));
        remove.setInvertSelection(true);
        remove.setInputFormat(instances);

        return Filter.useFilter(instances, remove);
    }

    public static Instances useFilter(Instances data) throws Exception {
        Instances newData = Filter.useFilter(data, getAttrSelectFilter(data));
        newData.setClassIndex(newData.numAttributes() - 1);
        //if (newData.classIndex() == -1)
        //    newData.setClassIndex(predictNum);
        return newData;
    }

    public static Filter getAttrSelectFilter(Instances data) throws Exception {
        AttributeSelection filter = new AttributeSelection();
        CfsSubsetEval eval = new CfsSubsetEval();
        BestFirst search = new BestFirst();
        String[] options = {"-D", "1", "-N", "5"};
        search.setOptions(options);
        filter.setEvaluator(eval);
        filter.setInputFormat(data);
        return filter;
    }

    public static boolean diffIsBig(List<Integer> a, List<Integer> b, int threshold) {
        return getDiffSize(a,b) > threshold;
    }

    public static int getDiffSize(List<Integer> a,List<Integer> b){
        int counter = 0;
        for (int i = 0; i < a.size(); i++) {
            counter += Math.abs(a.get(i) - b.get(i));
        }
        return counter;
    }

    public static boolean isPredictable(Instances data) {
        boolean flag = false;
        double tmp = data.instance(0).classValue();
        for (int i = 1; i < data.numInstances(); i++) {
            if (tmp != data.instance(i).classValue()) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    public static boolean isBugfix(String commitMessage) {
        return commitMessage.toLowerCase().contains("bug") || commitMessage.toLowerCase().contains("fix");
    }

    public static boolean judgeResult(double predict, double actual, boolean updown) {
        List<Long> list = new ArrayList<>();
        list.add(Math.round(Math.ceil(predict)));//切り捨て，切り上げしてLongへ
        list.add(Math.round(Math.floor(predict)));
        return updown ?
                list.contains(Math.round(actual)) :
                Objects.equals(Math.round(predict), Math.round(actual));
    }

    /**
     * JDTのノード数×2のarffを受け取る。
     * 次の各ノードの数がどうなるかを予測する予測器構築のためのデータを作成する
     * @param arffData
     * @param numAttribute
     * @return
     * @throws Exception
     */
    public static List<Instances> getFilteredData(File arffData, int numAttribute) throws Exception {
        BufferedReader br;
        Instances instances;

        List<Instances> res=new ArrayList<>();

        for (int i = 0; i < numAttribute; i++) {
            br = new BufferedReader(new FileReader(arffData));
            instances = new Instances(br);
            instances = removeAttrWithoutI(i, instances);
            instances.setClassIndex(numAttribute);
            instances = useFilter(instances);
            res.add(instances);
        }

        return res;
    }
    public static boolean isTargetRevision(String revisionId,RevCommit commit){
        return commit.getName().startsWith(revisionId);
    }
    public static Instances getInstances(File arffData) throws Exception{
        BufferedReader br = new BufferedReader(new FileReader(arffData));
        Instances instances = new Instances(br);
        instances.setClassIndex(instances.numAttributes()-1);
        br.close();
        return instances;
    }
}
