package fastrepair.yousei.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ClangのASTダンプによって出力されたファイルをパースし、AST要素をカウントする
 */
public class ClangAstElementCounter {
    public final String astFilename;
    public final String targetFilename;

    public ClangAstElementCounter(String astFilename, String targetFilename) {
        this.astFilename = astFilename;
        this.targetFilename = targetFilename;
    }

    public Map<String, Integer> parseAst() throws IOException {
        Map<String, Integer> elements = new HashMap<>();
        String regex = "^[\\|\\-`\\s]*(\\w+)\\s(\\w*)\\s(?:prev\\s\\w+\\s)*<+(.+?)>";
        Pattern pattern = Pattern.compile(regex);
//        String test = "TranslationUnitDecl 0xca72c0 <<invalid sloc>> <invalid sloc>";
//        test = "|-FunctionDecl 0x2ec7850 prev 0x2ec76f0 <col:9, col:61> col:37 used printf 'int (const char *, ...)'";
//        Matcher mr = pattern.matcher(test);
//        if (mr.find()) {
//            System.out.println("match");
//            System.out.println(mr.group(1));
//            System.out.println(mr.group(3));
//        }
        boolean inTargetFile = false;
        for (String line : Files.readAllLines(Paths.get(astFilename))) {
            if (!line.contains("<<<NULL>>>")) {
                Matcher m = pattern.matcher(line);
                if (m.find()) {
                    if (m.group(3).contains(targetFilename)) {
                        inTargetFile = true;
                    }
                    if (inTargetFile) {
                        String astName = m.group(1);
                        elements.merge(astName, 1, (a , b) -> a + b );
                    }
                }
            }
        }
        return elements;
    }
}
