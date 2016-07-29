package fastrepair;

import spoon.reflect.declaration.CtElement;
import spoon.reflect.visitor.CtScanner;

import java.util.Collection;
import java.util.Iterator;

/**
 * retrieve an ast vector from an element
 * Created by s-sumi on 16/07/30.
 */
public class AstVectorRetriever extends CtScanner{
    @Override
    public void scan(CtElement element) {
        if(element != null) {
            element.accept(this);
        }

    }
}