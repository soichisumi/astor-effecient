package fastrepair.yousei.util;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.gnu.c.GCCLanguage;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.*;
import org.eclipse.core.runtime.CoreException;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
//import org.eclipse.*;

/**
 * @author s-sumi
 */
public class CppSourceAnalyzer {
	private String filePath;	//基本これのみ必要
	private String classPath;
	private String outputPath;

	public CppSourceAnalyzer(String filePath, String classPath,
			String outputPath) {
		super();
		this.filePath = filePath;
		this.classPath = classPath;
		this.outputPath = outputPath;
	}
	public Map<String, Integer> analyzeFile() throws IOException,CoreException{
		StringBuilder source = new StringBuilder();
		try(FileReader fr = new FileReader(filePath)) {
			int c;
			while ((c = fr.read()) >= 0) {
				source.append((char)c);
			}
		}

		ILanguage language = GCCLanguage.getDefault();

		FileContent reader = FileContent.create(filePath, source.toString().toCharArray());

		Map<String, String> macroDefinitions = null;
		String[] includeSearchPath = null;
		IScannerInfo scanInfo = new ScannerInfo(macroDefinitions, includeSearchPath );

		IncludeFileContentProvider fileCreator = IncludeFileContentProvider.getEmptyFilesProvider();
		IIndex index = null;
		int options = 0;
		IParserLogService log = new DefaultLogService();

		IASTTranslationUnit translationUnit = language.getASTTranslationUnit(reader, scanInfo, fileCreator, index, options, log);
		AstElementCounter aec=new AstElementCounter(false);
		translationUnit.accept(aec);
		//CppSourceVisitor cppSourceVisitor=new CppSourceVisitor();
		//translationUnit.accept(cppSourceVisitor);
		//cppSourceVisitor.counter.forEach((k,v)-> System.out.printf("%s , %d%n",k, v));
		return new HashMap<>(aec.elements);
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public void setOutputPath(String outputPath) {
		this.outputPath = outputPath;
	}

}
