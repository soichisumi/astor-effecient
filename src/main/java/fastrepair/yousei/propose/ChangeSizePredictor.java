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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Created by s-sumi on 16/07/25.
 */
public class ChangeSizePredictor {
    private String bugRevisionId;
    private String bugSource;
    private Repository repository;

    private List<List<Integer>> preVectors = new ArrayList<>();
    private List<List<Integer>> postVectors = new ArrayList<>();
    public int[] changeSizeNum=new int[GeneralUtil.SMALLTHRESHOLD+1];

    public ChangeSizePredictor(String bugRevisionId, String bugSource, Repository repository) {
        this.bugRevisionId = bugRevisionId;
        this.bugSource = bugSource;
        this.repository = repository;
        Arrays.fill(changeSizeNum,0);
    }

    /**
     * assume arff like (attr,attr.....(only one vector) , big or small)
     * @return return true if next change is big
     */
    public boolean nextChangeIsBig() throws Exception{
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
        updateChangeSizes(this.preVectors,this.postVectors);
        File learningDataArff=Util.genealogy2Arff4SizePrediction(this.preVectors,this.postVectors); //
        File bugSourceArff=Util.createSourceArff4SizePrediction(this.bugSource);
        return Util.predictSizeObNextChange(learningDataArff,bugSourceArff);
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
