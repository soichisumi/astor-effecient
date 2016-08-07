package fastrepair;

import org.eclipse.jdt.core.dom.BooleanLiteral;
import weka.core.Instance;

/**
 * Created by s-sumi on 16/08/05.
 */
public class Util4Tests {
    public static boolean hasSameAttributes(Instance a,Instance b){
        boolean flag=true;
        for(int i=0;i<a.numAttributes();i++){
            if(!a.attribute(i).equals(b.attribute(i))){ //attributeはちゃんとequalsを実装している
                flag=false;
                break;
            }
        }
        return flag;
    }
}
