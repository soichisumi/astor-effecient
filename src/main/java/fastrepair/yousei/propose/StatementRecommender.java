package fastrepair.yousei.propose;

import fastrepair.yousei.propose.stmtcollector.AstLocation;
import fr.inria.astor.core.setup.ConfigurationProperties;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import spoon.reflect.cu.*;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * Created by s-sumi on 16/07/23.
 */
public class StatementRecommender {
    private String reposPath;
    /**
     * 短縮形でもフルでもよい
     */
    private String bugRevisionId;
    private Repository repository;

    public StatementRecommender() throws Exception {

        this.reposPath = ConfigurationProperties.properties.getProperty("reposPath");
        this.bugRevisionId=ConfigurationProperties.properties.getProperty("bugRevisionId");

        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        this.repository = builder
                //.setGitDir(new File(reposPath + "/" + Constants.DOT_GIT))
                .setGitDir(new File(reposPath))
                .readEnvironment()
                .findGitDir()
                .build();
    }


    public List<AstLocation> getStatements(String bugSourceCode) {
        return getStatements(bugSourceCode,null);
    }

    //SourcePosition==nullならadd or removeとして扱う
    public List<AstLocation> getStatements(String bugSourceCode,SourcePosition sp) {

        System.out.println("process " + reposPath);

        System.out.println("predict the size of next change");
        ChangeSizePredictorSingleton csp=ChangeSizePredictorSingleton.getInstance(this.repository);
        try {
            if(csp.nextChangeSizeIsSmall(bugSourceCode)){
                System.out.println("OK. next change is small\n");

                System.out.println("predict next source");
                SourceVectorPredictorSingleton svp=SourceVectorPredictorSingleton.getInstance(this.repository);
                List<Double> resDouble=svp.getNextVector(bugSourceCode);
                List<Integer> resInt= resDouble.stream().map(d -> (int) Math.round(d)).collect(Collectors.toList());
                List<AstLocation> locations =Util.getASTLocations(bugSourceCode,Util.toAstVector(resInt),sp);//予測結果-元の状態ベクトル+変更箇所の状態ベクトルをもつ文の集合を返す
                System.out.println("prediction done");
                System.out.println("prediction size is: "+locations.size());
                return locations;

            }else{
                System.out.println("next change is Big. conduct ASTOR default search");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }



}
