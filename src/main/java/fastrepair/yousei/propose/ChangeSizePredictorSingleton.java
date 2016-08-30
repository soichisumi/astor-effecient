package fastrepair.yousei.propose;

import fastrepair.yousei.GeneralUtil;
import fastrepair.yousei.experiment.ChangeAnalyzer;
import fr.inria.astor.core.setup.ConfigurationProperties;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;
import sun.security.jca.GetInstance;
import weka.classifiers.lazy.IBk;
import weka.core.Instances;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static fastrepair.yousei.GeneralUtil.getInstances;

/**
 * Created by s-sumi on 16/07/25.
 */
public class ChangeSizePredictorSingleton {
    private String bugRevisionId;
    private Repository repository;

    private List<List<Integer>> preVectors = new ArrayList<>();
    private List<List<Integer>> postVectors = new ArrayList<>();
    public int[] changeSizeNum=new int[GeneralUtil.SMALLTHRESHOLD+1];

    private Instances learningData=null;
    private IBk ibk=null;

    private static ChangeSizePredictorSingleton instance=null;

    public static ChangeSizePredictorSingleton getInstance(Repository repository){
        if(instance==null){
            instance=new ChangeSizePredictorSingleton(repository);
        }
        return instance;
    }
    public boolean nextChangeSizeIsSmall(String bugSource){
        int res;
        try {
            File bugSourceArff = Util.createSourceArff4SizePrediction(bugSource);
            Instances bugData=getInstances(bugSourceArff);
            bugData=Util.makeSameAttrData(learningData,bugData);
            res = Math.round((long)ibk.classifyInstance(bugData.instance(0)));//ex: 13.999 ->14 , 12.11->12

        } catch (Exception e) {
            throw new RuntimeException(e.toString());
        }
        return res==1;  //1..small 0...big
    }

    private ChangeSizePredictorSingleton(Repository repository){
        this.bugRevisionId = ConfigurationProperties.properties.getProperty("bugRevisionId");
        this.repository = repository;
        Arrays.fill(changeSizeNum,0);
        try {
            RevWalk rw = GeneralUtil.getInitializedRevWalk(repository, RevSort.REVERSE);
            RevCommit commit = rw.next();
            commit = rw.next();

            while (commit != null) {
                if (commit.getParentCount() >= 1)
                    updateGenealogy(commit, ".java");

                if (GeneralUtil.isTargetRevision(this.bugRevisionId, commit)) {   //use history up to the given bug
                    break;
                }

                commit = rw.next();
            }

            updateChangeSizes(this.preVectors, this.postVectors);
            printChangeSizes();

            File learningDataArff = Util.genealogy2Arff4SizePrediction(this.preVectors, this.postVectors); //

            this.learningData=getInstances(learningDataArff);
            learningData.setClassIndex(learningData.numAttributes()-1);  //index starts with 0
            learningData = GeneralUtil.useFilter(learningData);

            //in Murakami's thesis: we use k=1 because k=1 can produce results in the shortest time
            this.ibk=new IBk();
            ibk.setOptions("-K 1".split(" "));
            ibk.buildClassifier(learningData);

        }catch (Exception e){
            throw new RuntimeException();
        }
    }

    public void printChangeSizes(){
        System.out.println();
        System.out.println("ChangeSizes are:");
        int all=0;
        for(int i=0;i<this.changeSizeNum.length;i++){
            System.out.println(String.valueOf(i)+": "+String.valueOf(this.changeSizeNum[i]));
            all+=this.changeSizeNum[i];
        }
        System.out.println("all small change: " + all);
        System.out.println();
    }

    public void updateChangeSizes(List<List<Integer>> preVectors,List<List<Integer>> postVectors){
        for(int i=0;i<preVectors.size();i++){
            if(!GeneralUtil.diffIsBig(preVectors.get(i),postVectors.get(i),GeneralUtil.SMALLTHRESHOLD)){
                this.changeSizeNum[GeneralUtil.getDiffSize(preVectors.get(i),postVectors.get(i))]++;
            }
        }
    }

    public void updateGenealogy(RevCommit newRev, String suffix) throws Exception {
        RevCommit oldRev = newRev.getParent(0);

        AbstractTreeIterator oldTreeIterator = ChangeAnalyzer.prepareTreeParser(repository,
                oldRev.getId().getName());
        AbstractTreeIterator newTreeIterator = ChangeAnalyzer.prepareTreeParser(repository,
                newRev.getId().getName());
        List<DiffEntry> diff = new Git(repository).diff().setOldTree(oldTreeIterator)
                .setNewTree(newTreeIterator)
                .setPathFilter(PathSuffixFilter.create(suffix))
                .call();

        for (DiffEntry entry : diff) {
            if (entry.getChangeType() == DiffEntry.ChangeType.MODIFY) {
                ObjectLoader olold;
                ByteArrayOutputStream bosold = new ByteArrayOutputStream();
                String oldSource;

                if (!entry.getNewId().toObjectId().equals(ObjectId.zeroId())) { // OLDが存在するか
                    olold = repository.open(entry.getNewId().toObjectId());
                    olold.copyTo(bosold);
                    oldSource = bosold.toString();
                } else {
                    continue;
                }

                ObjectLoader olnew;
                ByteArrayOutputStream bosnew = new ByteArrayOutputStream();
                String newSource;
                if (!entry.getNewId().toObjectId().equals(ObjectId.zeroId())) { // NEWが存在するか
                    olnew = repository.open(entry.getNewId().toObjectId());
                    olnew.copyTo(bosnew);
                    newSource = bosnew.toString();
                } else {
                    continue;
                }

                if (Objects.equals(oldSource, "") || Objects.equals(newSource, "")) //ソースの修正なら解析対象とする
                    continue;

                preVectors.add(GeneralUtil.getSourceVector4Java(oldSource, ".java"));
                postVectors.add(GeneralUtil.getSourceVector4Java(newSource, ".java"));

            }
        }

    }


}
