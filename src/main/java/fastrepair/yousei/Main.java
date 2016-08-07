package fastrepair.yousei;


import fastrepair.yousei.experiment.ExperimentExecuter;

/**
 * @author s-sumi
 */
public class Main {
	//public static final String reposPath="testdata/thrift";
	public static final String reposPath="F:\\dataset\\genp\\fbc\\fbc";//ぬるぽ出たらパスが違うことを疑って
	public static final String[] reposPaths={
	//		"F:\\dataset\\genp\\cpython-hg",
			"F:\\dataset\\genp\\fbc\\fbc",
			"F:\\dataset\\genp\\gzip"
	//		"F:\\dataset\\genp\\lighthttpd\\lighttpd1.4",
	//		"F:\\dataset\\genp\\lighthttpd\\lighttpd2",
	//		"F:\\dataset\\genp\\php\\php-src"
	};
	public static final String[] resPath={
	//		"cpython",
			"fbc",
			"gzip"
	//		"lighttpd1.4",
	//		"lighttpd2",
	//		"php"
	};

	public static final String[] reposPaths4J = {
			"exprepos/commons-math"
	};
	public static final String[] resPath4J = {
			"commons-math"
	};

	public static void main(String[] args)throws Exception{
		ExperimentExecuter ee=new ExperimentExecuter();
		ee.setReposPaths(reposPaths4J);
		ee.setResPaths(resPath4J);
		ee.execute();
		System.out.println("fin");
	}

	/**
	 * RepositoryAnalyzer ra=new RepositoryAnalyzer(reposPath);
	 * ra.analyzeRepository();
	 */

	/*
		String filePath="testdata/hello.c";
		String classPath="lib";
		String outputPath="";
		CppSourceAnalyzer analyzer=new CppSourceAnalyzer(filePath,classPath,outputPath);
		analyzer.analyzeFile();
	*/

}
