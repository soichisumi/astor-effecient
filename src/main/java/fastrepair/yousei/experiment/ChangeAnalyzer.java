package fastrepair.yousei.experiment;

import fastrepair.yousei.util.CppSourceAnalyzer;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by s-sumi on 2016/05/10.
 */
public class ChangeAnalyzer {
    private RevCommit oldRev;
    private RevCommit newRev;
    private Repository repo;
    private File workingDir = new File("WorkingDir");
    private CppSourceAnalyzer csa = new CppSourceAnalyzer("", "", "");

    public ChangeAnalyzer() {
        super();
    }


    public ChangeAnalyzer(RevCommit newRev, Repository repo) {
        this.repo = repo;
        this.newRev = newRev;
        this.oldRev = this.newRev.getParent(0);
    }

    public void analyzeChange() throws Exception{
        String oldsourceString;
        String newsourceString;

        AbstractTreeIterator oldTreeIterator = prepareTreeParser(repo,
                oldRev.getId().getName());
        AbstractTreeIterator newTreeIterator = prepareTreeParser(repo,
                newRev.getId().getName());
        List<DiffEntry> diff = new Git(repo).diff().setOldTree(oldTreeIterator)
                .setNewTree(newTreeIterator)
                .setPathFilter(PathSuffixFilter.create(".cpp"))
                .call();

        //誠に遺憾ながらcdtはStringを元にASTを構築してくれないので，
        //一旦StringからFileを作成して解析する．終わったら削除
        for (DiffEntry entry : diff) {
            ObjectLoader olold;
            ObjectLoader olnew;

            ByteArrayOutputStream bosold = new ByteArrayOutputStream();
            ByteArrayOutputStream bosnew = new ByteArrayOutputStream();

            if (entry.getChangeType() == DiffEntry.ChangeType.MODIFY) { // ソースの変更の場合のみ

                if (!(entry.getOldId().toObjectId().equals(ObjectId.zeroId()))) { // OLDが存在するか
                    olold = repo.open(entry.getOldId().toObjectId()); // ソースを読み込んで，コメントなどを消去
                    olold.copyTo(bosold);
                    oldsourceString = bosold.toString();

                } else {
                    oldsourceString = "";
                }

                if (!entry.getNewId().toObjectId().equals(ObjectId.zeroId())) { // NEWが存在するか
                    olnew = repo.open(entry.getNewId().toObjectId()); // ソースを読み込んで，コメントなどを消去
                    olnew.copyTo(bosnew);
                    newsourceString = bosnew.toString();
                } else {
                    newsourceString = "";
                }

                if (Objects.equals(oldsourceString, "") || Objects.equals(newsourceString, ""))
                    continue;

                File tmpFileOld = File.createTempFile("old", ".cpp", workingDir);
                BufferedWriter bw = new BufferedWriter(new FileWriter(tmpFileOld));
                bw.write(oldsourceString);
                bw.close();

                csa.setFilePath(tmpFileOld.getAbsolutePath());
                Map<String, Integer> resOld = csa.analyzeFile();

                File tmpFileNew = File.createTempFile("new", ".cpp", workingDir);
                bw = new BufferedWriter(new FileWriter(tmpFileNew));
                bw.write(newsourceString);
                bw.close();

                csa.setFilePath(tmpFileNew.getAbsolutePath());
                Map<String, Integer> resNew = csa.analyzeFile();

            }

        }
    }


    public static AbstractTreeIterator prepareTreeParser(
            Repository repository, String objectId) throws IOException,
            MissingObjectException, IncorrectObjectTypeException {
        // from the commit we can build the tree which allows us to construct
        // the TreeParser
        RevWalk walk = new RevWalk(repository);
        RevCommit commit = walk.parseCommit(ObjectId.fromString(objectId));
        RevTree tree = walk.parseTree(commit.getTree().getId());

        CanonicalTreeParser oldTreeParser = new CanonicalTreeParser();
        try (ObjectReader oldReader = repository.newObjectReader()) {
            oldTreeParser.reset(oldReader, tree.getId());
        }

        walk.dispose();

        return oldTreeParser;
    }

    public void setCommit(RevCommit newRev) {
        this.newRev = newRev;
        this.oldRev = this.newRev.getParent(0);
    }

    public void setRepo(Repository repo) {
        this.repo = repo;
    }
}
