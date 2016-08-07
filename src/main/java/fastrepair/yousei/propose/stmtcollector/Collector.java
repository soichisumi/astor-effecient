package fastrepair.yousei.propose.stmtcollector;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author n-ogura
 */
public class Collector {
    public static void main(String[] args) throws IOException {
        System.out.println("hello world");
        List<Path> targets = Files.walk(Paths.get("src/main/java"))
                .filter(e -> e.toFile().isFile())
                .filter(e -> e.toFile().getPath().endsWith(".java"))
                .collect(Collectors.toList());
        Multimap<AstVector, AstLocation> result = MultimapBuilder.hashKeys().arrayListValues().build();
        for (Path target : targets) {
            ASTParser parser = ASTParser.newParser(AST.JLS8);
            String content = new String(Files.readAllBytes(target));
            parser.setSource(content.toCharArray());
            CompilationUnit unit = (CompilationUnit)parser.createAST(new NullProgressMonitor());
            AstCollectorVisitor visitor = new AstCollectorVisitor(target.toFile().toString(), unit);
            unit.accept(visitor);//CompilationUnitでなくても
            for (Map.Entry<AstVector, AstLocation> a : visitor.asts.entries()) {
                result.put(a.getKey(), a.getValue());
            }
        }
        for (AstVector k : result.keys()) {
            System.out.println("--------");
            System.out.println(k);
            for (AstLocation v : result.get(k)) {
                System.out.println(v);
            }
        }
        int a = 1;
        int b = 2;
    }
}
