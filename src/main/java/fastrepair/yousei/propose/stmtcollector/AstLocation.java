package fastrepair.yousei.propose.stmtcollector;import java.util.Objects;

/**
 * @author n-ogura
 */
public class AstLocation {
    public final String path;
    public final int startLine;
    public final int startColumn;
    public final int endLine;
    public final int endColumn;
    public final String rootElement;

    public AstLocation(String path, int startLine, int startColumn, int endLine, int endColumn, String rootElement) {
        this.path = path;
        this.startLine = startLine;
        this.startColumn = startColumn;
        this.endLine = endLine;
        this.endColumn = endColumn;
        this.rootElement = rootElement;
    }

    @Override
    public int hashCode() {
        return path.hashCode() ^ startLine ^ startColumn ^ endLine ^ endColumn;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof AstLocation) {
            AstLocation otherLocation = (AstLocation) other;
            return Objects.equals(path, otherLocation.path) &&
                    startLine == otherLocation.startLine &&
                    startColumn == otherLocation.startColumn &&
                    endLine == otherLocation.endLine &&
                    endColumn == otherLocation.endColumn &&
                    Objects.equals(rootElement, otherLocation.rootElement);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return String.format("[%s <%d:%d-%d:%d> %s]", path, startLine, startColumn, endLine, endColumn, rootElement);
    }

}
