package fastrepair.yousei.util;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by s-sumi on 2016/07/12.
 */
public class JavaSourceAnalyzer {

    private String filePath;	//基本これのみ必要
    private String classPath;
    private String outputPath;

    public JavaSourceAnalyzer(String filePath, String classPath,
                              String outputPath) {
        super();
        this.filePath = filePath;
        this.classPath = classPath;
        this.outputPath = outputPath;
    }
    public List<Integer> analyzeFile() throws Exception{
        /*StringBuilder source = new StringBuilder(); //なんかOutObBounds出たぞ
        try(FileReader fr = new FileReader(filePath)) {
            int c;
            while ((c = fr.read()) >= 0) {
                source.append((char)c);
            }
        }*/
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        StringBuffer source = new StringBuffer();
        String st = null;
        while ((st = reader.readLine()) != null) {
            source.append(st + System.getProperty("line.separator"));
        }
        reader.close();
        ASTParser parser = ASTParser.newParser(AST.JLS4);
        parser.setSource(source.toString().toCharArray());
        CompilationUnit unit = (CompilationUnit) parser
                .createAST(null);

        JavaSourceVisitor visitor = new JavaSourceVisitor();
        unit.accept(visitor);
        return new ArrayList<>(visitor.vector);
    }
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }



}
