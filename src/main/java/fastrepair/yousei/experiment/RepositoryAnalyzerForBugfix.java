package fastrepair.yousei.experiment;


import fastrepair.yousei.GeneralUtil;
import fastrepair.yousei.util.Util;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static fastrepair.yousei.GeneralUtil.getInitializedRevWalk;

/**
 * Created by s-sumi on 2016/06/29.
 */
public class RepositoryAnalyzerForBugfix extends RepositoryAnalyzer {

    public List<Map<String, Integer>> preVector = new ArrayList<>();
    public List<Map<String, Integer>> postVector = new ArrayList<>();

    public RepositoryAnalyzerForBugfix(String reposPath) throws Exception {
        super(reposPath);
    }

    @Override
    public void analyzeRepository(String resultPath) throws Exception {
        RevWalk rw = getInitializedRevWalk(this.repository, RevSort.REVERSE);//最古
        RevCommit commit = rw.next();
        while (commit != null) {
            if (commit.getParentCount() >= 1 && GeneralUtil.isBugfix(commit.getFullMessage())) {
                updateGenealogy(commit);
            }
            commit = rw.next();
        }
        File f = Util.allGenealogy2Arff(preVector, postVector);
        Util.predict(f, resultPath, false);
        Util.predict(f, resultPath, true);
        Util.vectoredPrediction(f, resultPath, false);
        Util.vectoredPrediction(f, resultPath, true);
        //Util.predictWithSomeClassifiers(f,resultPath,classifiers,false);
        //Util.vectoredPredictionWithSomeClassifiers(f,resultPath,classifiers,false);
        f.delete();
    }

    //newRevは1つ以上の親コミットを持つこと
    @Override
    public void updateForGivenSuffix(RevCommit newRev, String suffix) throws Exception {
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

                preVector.add(GeneralUtil.getSourceVector(oldSource, suffix));
                postVector.add(GeneralUtil.getSourceVector(newSource, suffix));

            }
        }
    }

}
