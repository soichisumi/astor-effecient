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
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.functions.LinearRegression;
import weka.core.Instance;
import weka.core.Instances;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Using Repository and bug_revision_id, return next vector
 * Created by s-sumi on 16/07/23.
 */
public class SourceVectorPredictorSingleton {    //similar to ChangeSizePredictorSingleton

    private String bugRevisionId;
    private Repository repository;

    private List<List<Integer>> preVectors = new ArrayList<>();
    private List<List<Integer>> postVectors = new ArrayList<>();

    private List<Instances> filteredLearningData=null;
    private List<Classifier> classifiers=null;
    private int numAttribute;

    private static SourceVectorPredictorSingleton instance=null;

    public static SourceVectorPredictorSingleton getInstance(Repository repository){
        if(instance==null)
            instance=new SourceVectorPredictorSingleton(ConfigurationProperties.properties.getProperty("bugRevisionId"),repository);
        return instance;
    }

    private SourceVectorPredictorSingleton(String bugRevisionId, Repository repository) {
        this.bugRevisionId = bugRevisionId;
        this.repository = repository;

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
            File learningDataArff = Util.genealogy2Arff4VectorPrediction(this.preVectors, this.postVectors);


            this.numAttribute = Util.getNumAttribute(learningDataArff);
            this.filteredLearningData= GeneralUtil.getFilteredData(learningDataArff, numAttribute);

            LinearRegression lr = new LinearRegression();
            String[] options = "-S 0".split(" ");
            lr.setOptions(options);

            this.classifiers = new ArrayList<>();
            for (int i = 0; i < numAttribute; i++) {         //各ノードに対する予測器を構築
                Classifier copied = AbstractClassifier.makeCopy(lr);
                copied.buildClassifier(this.filteredLearningData.get(i));
                classifiers.add(copied);
            }

            classifiers.forEach((c)-> System.out.println(c.toString())); //debug

        }catch (Exception e){
            throw new RuntimeException();
        }
    }

    synchronized public List<Double> getNextVector(String bugSource) throws Exception{
        File bugSourceArff = Util.createSouceArff4VectorPrediction(bugSource);
        List<Instance> attrSelectedData=Util.getAttrSelectedData(bugSourceArff,this.filteredLearningData);
        List<Double> res=new ArrayList<>();
        for (int i = 0; i < this.numAttribute; i++) {
            res.add(
                    this.classifiers.get(i).classifyInstance(attrSelectedData.get(i))
            );
        }
        return res;
    }


    public void updateGenealogy(RevCommit newRev, String suffix) throws Exception {
        RevCommit oldRev = newRev.getParent(0);

        AbstractTreeIterator oldTreeIterator = ChangeAnalyzer.prepareTreeParser(this.repository,
                oldRev.getId().getName());
        AbstractTreeIterator newTreeIterator = ChangeAnalyzer.prepareTreeParser(this.repository,
                newRev.getId().getName());
        List<DiffEntry> diff = new Git(this.repository).diff().setOldTree(oldTreeIterator)
                .setNewTree(newTreeIterator)
                .setPathFilter(PathSuffixFilter.create(suffix))
                .call();

        for (DiffEntry entry : diff) {
            if (entry.getChangeType() == DiffEntry.ChangeType.MODIFY) {
                ObjectLoader olold;
                ByteArrayOutputStream bosold = new ByteArrayOutputStream();
                String oldSource;

                if (!entry.getNewId().toObjectId().equals(ObjectId.zeroId())) { // OLDが存在するか
                    olold = this.repository.open(entry.getNewId().toObjectId());
                    olold.copyTo(bosold);
                    oldSource = bosold.toString();
                } else {
                    continue;
                }

                ObjectLoader olnew;
                ByteArrayOutputStream bosnew = new ByteArrayOutputStream();
                String newSource;
                if (!entry.getNewId().toObjectId().equals(ObjectId.zeroId())) { // NEWが存在するか
                    olnew = this.repository.open(entry.getNewId().toObjectId());
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
