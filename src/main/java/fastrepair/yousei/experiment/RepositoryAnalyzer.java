package fastrepair.yousei.experiment;

import fastrepair.yousei.GeneralUtil;
import fastrepair.yousei.util.CppSourceAnalyzer;
import fastrepair.yousei.util.Util;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;
import weka.classifiers.Classifier;

import weka.classifiers.functions.LinearRegression;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.trees.M5P;
import weka.classifiers.trees.RandomForest;
import weka.core.SelectedTag;


import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.util.*;

/**
 * Created by s-sumi on 2016/05/09.
 */
public class RepositoryAnalyzer {
    public final String[] classifierNames={
            "SVMReg",
            "LinearRegression",
            "RandomForest",
            "M5P",
            "MultilayerPerceptron"
    };
    public ChangeAnalyzer ca;
    public Repository repository;
    public Map<String, List<Map<String, Integer>>> genealogy = new HashMap<>();
    public List<List<Map<String, Integer>>> deletedGenealogies = new ArrayList<>();
    public List<Classifier> classifiers = new ArrayList<>();
    public int bugfixCounter=0;

    public RepositoryAnalyzer(String reposPath) throws Exception {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        this.repository = builder
                .setGitDir(new File(reposPath + "/" + Constants.DOT_GIT))
                .readEnvironment()
                .findGitDir()
                .build();
        this.ca = new ChangeAnalyzer();
        this.ca.setRepo(this.repository);
        initClassifiers();
    }

    public void analyzeRepository(String resultPath) throws Exception {

        RevWalk rw = GeneralUtil.getInitializedRevWalk(repository, RevSort.REVERSE);//最古
        RevCommit commit = rw.next();
        initGenealogy(commit);
        commit = rw.next();
        while (commit != null) {
            if (commit.getParentCount() >= 1) {
                updateGenealogy(commit);
            }
            commit = rw.next();
        }
        addDeleted2Genealogy();
        File f = Util.allGenealogy2Arff(genealogy);
        Util.predict(f, resultPath,false);
        Util.predict(f,resultPath,true);
        Util.vectoredPrediction(f,resultPath,false);
        Util.vectoredPrediction(f,resultPath,true);
        //Util.predictWithSomeClassifiers(f,resultPath,classifiers,false);
        //Util.vectoredPredictionWithSomeClassifiers(f,resultPath,classifiers,false);

        f.delete();

/*      Set<String> names=new HashSet<>();
        for(Map.Entry<String,List<Map<String,Integer>>> e:genealogy.entrySet()){
            for(Map<String,Integer> string:e.getValue()){
                names.addAll(string.keySet());
            }
            if(e.getValue().size()<2)
                continue;
            File f=Util.singleGenealogy2Arff(e.getValue());
            Util.predict(f);
            f.delete();
        }
        names.forEach(System.out::println);
        Util.enumNotFoundNodes(names);*/

    }




    public void initGenealogy(RevCommit firstCommit) throws Exception {
        initForGivenSuffix(firstCommit,".cpp");
        initForGivenSuffix(firstCommit,".c");
    }

    public void initForGivenSuffix(RevCommit firstCommit,String suffix)throws Exception{
        RevTree revTree = firstCommit.getTree();
        TreeWalk treeWalk = new TreeWalk(repository);
        treeWalk.addTree(revTree);
        treeWalk.setRecursive(true);
        treeWalk.setFilter(PathSuffixFilter.create(suffix));

        while (treeWalk.next()) {
            ObjectId objectId = treeWalk.getObjectId(0);
            ObjectLoader objectLoader = repository.open(objectId);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            objectLoader.copyTo(baos);

            List<Map<String, Integer>> fileGenealogy = new ArrayList<>();
            fileGenealogy.add(GeneralUtil.getSourceVector(baos.toString()));

            genealogy.put(treeWalk.getPathString(), fileGenealogy);  //mergeでもどっちでもいい
        }
    }


    //map返さなくてもいいかも
    /**
     * コミットを解析して，ファイルの系譜を作成する
     * @param newRev
     * @return
     * @throws Exception
     */
    public void updateGenealogy(RevCommit newRev) throws Exception {
        updateForGivenSuffix(newRev, ".cpp");
        updateForGivenSuffix(newRev, ".c");

    }

    public void updateForGivenSuffix(RevCommit newRev,String suffix) throws Exception{
        File workingDir = new File("WorkingDir");
        CppSourceAnalyzer csa = new CppSourceAnalyzer("", "", "");
        String source;

        RevCommit oldRev = newRev.getParent(0);

        //-----------------まずはCPPについて--------------------------------//

        AbstractTreeIterator oldTreeIterator = ChangeAnalyzer.prepareTreeParser(repository,
                oldRev.getId().getName());
        AbstractTreeIterator newTreeIterator = ChangeAnalyzer.prepareTreeParser(repository,
                newRev.getId().getName());
        List<DiffEntry> diff = new Git(repository).diff().setOldTree(oldTreeIterator)
                .setNewTree(newTreeIterator)
                .setPathFilter(PathSuffixFilter.create(suffix))
                .call();

        //削除系を先に．リネーム，追加を後に処理
        diff.sort(Util::compareDiffEntries);

        //誠に遺憾ながらcdtはStringを元にASTを構築してくれないので，
        //一旦StringからFileを作成して解析する．終わったら削除
        for (DiffEntry entry : diff) {
            if (entry.getChangeType() == DiffEntry.ChangeType.DELETE) {
                //if(genealogy.remove(entry.getOldPath())==null)
                //    throw new Exception();//add忘れがあるということ
                List<Map<String,Integer>> tmp=genealogy.get(entry.getOldPath());
                if(tmp!=null)
                    deletedGenealogies.add(tmp);
                genealogy.remove(entry.getOldPath());

            } else if (entry.getChangeType() == DiffEntry.ChangeType.RENAME) {
                List<Map<String, Integer>> tmp = genealogy.get(entry.getOldPath());
                genealogy.remove(entry.getOldPath());
                genealogy.put(entry.getNewPath(), tmp);
            } else if (entry.getChangeType() == DiffEntry.ChangeType.MODIFY) {
                ObjectLoader olnew;
                ByteArrayOutputStream bosnew = new ByteArrayOutputStream();

                if (!entry.getNewId().toObjectId().equals(ObjectId.zeroId())) { // NEWが存在するか
                    olnew = repository.open(entry.getNewId().toObjectId()); // ソースを読み込んで，コメントなどを消去
                    olnew.copyTo(bosnew);
                    source = bosnew.toString();
                } else {
                    continue;
                }

                if (Objects.equals(source, ""))
                    continue;

                File tmpFile = File.createTempFile("new", suffix, workingDir);
                BufferedWriter bw = new BufferedWriter(new FileWriter(tmpFile));
                bw.write(source);
                bw.close();


                csa.setFilePath(tmpFile.getAbsolutePath());
                Map<String, Integer> res = csa.analyzeFile();
                tmpFile.delete();

                if (Objects.equals(entry.getOldPath(), entry.getNewPath())) {
                    List<Map<String, Integer>> tmp = genealogy.get(entry.getOldPath());
                    if(tmp==null)
                        tmp=new ArrayList<>();
                    tmp.add(res);
                    genealogy.put(entry.getOldPath(), tmp);
                } else {
                    List<Map<String, Integer>> tmp = genealogy.get(entry.getOldPath());
                    if(tmp==null)
                        tmp=new ArrayList<>();
                    tmp.add(res);
                    genealogy.remove(entry.getOldPath());
                    genealogy.put(entry.getNewPath(), tmp);
                }

            } else if (entry.getChangeType() == DiffEntry.ChangeType.ADD) {
                ObjectLoader olnew;
                ByteArrayOutputStream bosnew = new ByteArrayOutputStream();

                if (!entry.getNewId().toObjectId().equals(ObjectId.zeroId())) { // NEWが存在するか
                    olnew = repository.open(entry.getNewId().toObjectId()); // ソースを読み込んで，コメントなどを消去
                    olnew.copyTo(bosnew);
                    source = bosnew.toString();
                } else {
                    continue;
                }

                if (Objects.equals(source, ""))
                    continue;

                File tmpFile = File.createTempFile("new", suffix, workingDir);
                BufferedWriter bw = new BufferedWriter(new FileWriter(tmpFile));
                bw.write(source);
                bw.close();


                csa.setFilePath(tmpFile.getAbsolutePath());
                Map<String, Integer> res = csa.analyzeFile();
                tmpFile.delete();

                List<Map<String, Integer>> tmp = new ArrayList<>();

                tmp.add(res);
                genealogy.put(entry.getNewPath(), tmp);
            } else {      //copy
                List<Map<String, Integer>> tmp = new ArrayList<>(genealogy.get(entry.getOldPath()));
                genealogy.put(entry.getNewPath(), tmp);
            }
        }

    }

    /**
     * ソースファイルの修正の過程で削除されてしまったファイルの系譜を全てgenealogyに追加する
     * 系譜は2以上でないと学習データとして使えないが
     */
    public void addDeleted2Genealogy()throws Exception{
        String prefix="DeletedSourceFileHOGEEE";
        int size=deletedGenealogies.size();
        for(int i=0;i<size;i++){
            List<Map<String, Integer>> tmp=deletedGenealogies.get(i);
            if(genealogy.put(prefix+Integer.toString(i),tmp)!=null)
                throw new Exception("something is already here");
        }
    }

    private void initClassifiers()throws Exception{
        for(String s:this.classifierNames){
            if(Objects.equals(s,"LinearRegression")){
                LinearRegression lr = new LinearRegression();
                String[] options = {"-S", "0"};
                lr.setOptions(options);
                classifiers.add(lr);
            }else if(Objects.equals(s,"RandomForest")){
                RandomForest rf=new RandomForest();
               // String[] options={};
               // rf.setOptions(options);
                classifiers.add(rf);
            }else if(Objects.equals(s,"MultilayerPerceptron")){
                MultilayerPerceptron mlp=new MultilayerPerceptron();
                classifiers.add(mlp);
            }else if(Objects.equals(s,"M5P")){
                M5P m5p=new M5P();
                classifiers.add(m5p);
            }else{
                throw new Exception("undefined classifier is specified");
            }
        }
    }
}
