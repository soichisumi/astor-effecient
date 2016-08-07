package fastrepair;

import spoon.reflect.declaration.CtElement;
import spoon.reflect.visitor.CtScanner;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * retrieve an ast vector from an element
 * 使わない
 * Created by s-sumi on 16/07/30.
 */
public class AstVectorRetriever extends CtScanner{
    private final Map<Class<? extends CtElement>,Integer> vector=new HashMap<>();
    private final int NODE_NUMBER=Class2IndexConvSingleton.getInstance().getNodeNumber();
    @Override
    public void scan(CtElement element) {
        if(element != null) {
            vector.merge(element.getClass(),1,(a,b)->a+b);
            element.accept(this);
        }
    }
    public SpoonAstVector getVector(){
        int[] vec=new int[NODE_NUMBER];
        for(int i=0;i<NODE_NUMBER;i++){
            Class<?> astClass=Class2IndexConvSingleton.getInstance().getClassFromIndex(i);
            vec[i]=this.vector.getOrDefault(astClass,0);
        }
        return new SpoonAstVector(vec);
    }
}