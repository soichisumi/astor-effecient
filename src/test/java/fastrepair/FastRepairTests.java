package fastrepair;

import static org.junit.Assert.*;
import org.junit.Test;
import spoon.reflect.declaration.CtElement;
import spoon.support.reflect.declaration.CtAnnotationImpl;
import spoon.support.reflect.declaration.CtElementImpl;

/**
 * Created by s-sumi on 16/07/30.
 */
public class FastRepairTests {
    @Test
    public void className2IndexConverterConstructTest() throws Exception{
        Class2IndexConvSingleton cnic=Class2IndexConvSingleton.getInstance();
        assertTrue(!cnic.getClasses().isEmpty());
        cnic.getClasses().forEach(System.out::println);
        System.out.println("size: "+cnic.getClasses().size());
    }
    @Test
    public void findImplClassTest(){
        CtAnnotationImpl a=new CtAnnotationImpl();
        System.out.println(CtElement.class.isAssignableFrom(a.getClass()));
    }
}

