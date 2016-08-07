package fastrepair.yousei.propose;

import fastrepair.yousei.GeneralUtil;
import fastrepair.yousei.experiment.ChangeAnalyzer;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Using Repository and bug_revision_id, return next vector
 * Created by s-sumi on 16/07/23.
 */
public class SourceVectorPredictor {    //similar to ChangeSizePredictor

    private String bugRevisionId;
    private String bugSource;
    private Repository repository;

    private List<List<Integer>> preVectors = new ArrayList<>();
    private List<List<Integer>> postVectors = new ArrayList<>();

    public SourceVectorPredictor(String bugRevisionId, String bugSource, Repository repository) {
        this.bugRevisionId = bugRevisionId;
        this.bugSource = bugSource;
        this.repository = repository;
    }

    public List<Double> getNextVector() throws Exception{
        RevWalk rw = GeneralUtil.getInitializedRevWalk(repository, RevSort.REVERSE);
        RevCommit commit = rw.next();
        commit = rw.next();

        while (commit != null) {
            if (commit.getParentCount() >= 1 )
                updateGenealogy(commit, ".java");

            if(GeneralUtil.isTargetRevision(this.bugRevisionId,commit)) {   //use history up to the given bug
                break;
            }

            commit = rw.next();
        }
        File learningDataArff=Util.genealogy2Arff4VectorPrediction(this.preVectors,this.postVectors);
        File bugSourceArff=Util.createSouceArff4VectorPrediction(this.bugSource);
        return Util.vectoredPrediction(learningDataArff,bugSourceArff);
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
