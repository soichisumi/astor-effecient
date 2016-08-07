package fastrepair.yousei.experiment;

import fastrepair.yousei.GeneralUtil;
import fastrepair.yousei.util.Util;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.Instances;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by s-sumi on 2016/06/17.
 */
public class CustomizedCrossValidation {

    public int num_classified = 0;
    public int num_correct = 0;
    public int num_incorrect = 0;
    public int num_classifiedArray[] = new int[6];
    public int num_correctArray[] = new int[6];
    public boolean randomized = true;
    public int dist[];


    public CustomizedCrossValidation() {
        super();
        Arrays.fill(num_classifiedArray, 0);
        Arrays.fill(num_correctArray, 0);
    }

    /**
     * 予測器を交差検証する
     *
     * @param classifier   予測器
     * @param filteredData 学習データと検証データ
     * @param numFolds     データを何分割するか
     * @param random       シード new Random(int seed)を与える.nullならシャッフルしない
     * @throws Exception
     */
    public String evaluate(Classifier classifier, Instances filteredData, int numFolds, Random random, boolean updown) throws Exception {
        // Do the folds
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numFolds; i++) {
            Instances train;
            if (randomized) {
                train = filteredData.trainCV(numFolds, i, random);
            } else {
                train = filteredData.trainCV(numFolds, i);
            }
            Classifier copiedClassifier = AbstractClassifier.makeCopy(classifier);
            if (copiedClassifier.getClass().getSimpleName().contains("SVM") && !GeneralUtil.isPredictable(train)) {
                num_classified = train.numInstances();
                num_correct = num_classified;
                num_incorrect = num_classified - num_correct;
            } else {
                copiedClassifier.buildClassifier(train);
                Instances test = filteredData.testCV(numFolds, i);
                test.setClassIndex(filteredData.classIndex());
                evaluateModel(copiedClassifier, test, sb, updown);
            }

        }
        sb.append("classified: ");
        sb.append(num_classified);
        sb.append("\n");

        sb.append("correct: ");
        sb.append(num_correct);
        sb.append("\n");

        sb.append("incorrect: ");
        sb.append(num_incorrect);
        sb.append("\n");

        sb.append("precision: ");
        sb.append((double) num_correct / (double) num_classified);
        sb.append("\n");


        return sb.toString();
    }

    //与えられるclassifierは既にビルドされているものとする
    public void evaluateModel(Classifier classifier, Instances test, StringBuilder sb, boolean updown) throws Exception {
        if (test.classIndex() < 0)
            throw new Exception("please set ClassIndex to test data");

        for (int i = 0; i < test.numInstances(); i++) {
            if (GeneralUtil.judgeResult(classifier.classifyInstance(test.instance(i)),
                                    test.instance(i).value(test.classIndex()),
                                    updown)) {
                num_correct++;
            } else {
                num_incorrect++;
            }
            num_classified++;
        }
    }

    //まずfiltereddataを作ってその後fold
    public void vectoredPrediction(Classifier classifier, File data, int numFolds, Random random, boolean updown) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(data));
        Instances instances = new Instances(br);
        int num = instances.numAttributes() / 2;
        int numAllInstance = instances.numInstances();
        br.close();

        this.dist = Util.getDistanceOfChanges(instances);

        List<Instances> filteredData = GeneralUtil.getFilteredData(data,num);


        for (int i = 0; i < numFolds; i++) { //交差検証
            List<Classifier> classifiers = new ArrayList<>();
            List<Instances> testDatas = new ArrayList<>();

            int numTestDataInstances = 0;

            for (int j = 0; j < num; j++) {         //各ノードに対する予測器を構築
                Instances train;
                if (randomized) {
                    train = filteredData.get(j).trainCV(numFolds, i, random);
                } else {
                    train = filteredData.get(j).trainCV(numFolds, i);
                }

                Classifier copied = AbstractClassifier.makeCopy(classifier);
                if (copied.getClass().getSimpleName().contains("SVM") && !GeneralUtil.isPredictable(train)) {
                    classifiers.add(null);
                    testDatas.add(null);
                } else {
                    copied.buildClassifier(train);
                    Instances test = filteredData.get(j).testCV(numFolds, i);
                    test.setClassIndex(train.classIndex());
                    classifiers.add(copied);
                    testDatas.add(test);
                }

                if (numTestDataInstances == 0)
                    numTestDataInstances = filteredData.get(0).testCV(numFolds, i).numInstances();

            }
            vectoredEvaluateModel(classifiers, testDatas, numTestDataInstances, num, numFolds, i, numAllInstance, updown);//精度確認．正解数などを記録
        }
    }

    public void vectoredEvaluateModel(List<Classifier> classifiers, List<Instances> testDatas, int numInstances, int numAttribute
            , int numFolds, int numFold, int numAllInstance, boolean updown) throws Exception {

        for (int i = 0; i < numInstances; i++) {
            boolean correct = true;
            for (int j = 0; j < numAttribute; j++) {

                if (classifiers.get(j) == null)//svmかつclassvalueの変化が無かったときにnullになる．そのときは必ず正解するので飛ばす
                    continue;

                if (!GeneralUtil.judgeResult(
                        classifiers.get(j).classifyInstance(testDatas.get(j).instance(i)),
                        testDatas.get(j).instance(i).value(testDatas.get(j).classIndex()),
                        updown))
                    correct=false;

            }
            if (correct) {
                num_correct++;
            } else {
                num_incorrect++;
            }
            num_classified++;
            if (!randomized) {
                num_classifiedArray[dist[Util.getInstanceNum(numFolds, numFold, i, numAllInstance)]]++;
                if (correct) {
                    num_correctArray[dist[Util.getInstanceNum(numFolds, numFold, i, numAllInstance)]]++;
                }
            }
        }
    }
}
