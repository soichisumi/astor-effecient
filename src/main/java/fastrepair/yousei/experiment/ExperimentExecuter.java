package fastrepair.yousei.experiment;

/**
 * 与えられたリポジトリへのパスの集合に対して実験を行い，
 * 各実験の結果を出力する
 * Created by s-sumi on 2016/06/24.
 */
public class ExperimentExecuter {
    public String[] reposPaths;
    public String[] resPaths;

    public void execute() throws Exception {
        if (reposPaths == null || resPaths == null || reposPaths.length != resPaths.length)
            throw new Exception("error about reposPaths or resPaths");

        for (int i = 0; i < reposPaths.length; i++) {
            System.out.println("processing "+resPaths[i]+" ...");
            //RepositoryAnalyzer ra = new RepositoryAnalyzer(reposPaths[i]);
            //ra.analyzeRepository(resPaths[i]);
            //RepositoryAnalyzerForBugfix rafb=new RepositoryAnalyzerForBugfix(reposPaths[i]);
            //rafb.analyzeRepository(resPaths[i]);
            RepositoryAnalyzer4Java ra4j = new RepositoryAnalyzer4Java(reposPaths[i]);
            ra4j.analyzeRepository(resPaths[i]);
            System.out.println("done");
            System.out.println();
        }
    }

    public void setReposPaths(String[] reposPaths) {
        this.reposPaths = reposPaths;
    }

    public void setResPaths(String[] resPaths) {
        this.resPaths = resPaths;
    }
}
