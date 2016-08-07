package fastrepair;

import static org.junit.Assert.*;

import fastrepair.yousei.GeneralUtil;
import fastrepair.yousei.propose.Util;
import org.junit.Test;
import spoon.reflect.declaration.CtElement;
import spoon.support.reflect.declaration.CtAnnotationImpl;
import spoon.support.reflect.declaration.CtElementImpl;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.functions.LinearRegression;
import weka.core.Instance;
import weka.core.Instances;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

    @Test
    public void makeSameDataTest01() throws Exception {
        File leaningData= new File(getClass()
                                .getClassLoader()
                                .getResource("genealogy4VectorPrediction.arff") //JDTのノード数×２のattribute
                                .getPath());
        File bugArffData = new File(getClass()
                                .getClassLoader()
                                .getResource("bugSource4VectorPrediction.arff")                  //JDTのノード数×2+1
                                .getPath());
        int numAttribute = Util.getNumAttribute(leaningData);

        List<Instances> filteredData = GeneralUtil.getFilteredData(leaningData, numAttribute);  //leaningData
        List<Instance> attrSelectedData=Util.getAttrSelectedData(bugArffData,filteredData);     //testData
        System.out.println("checking size of the lists....");
        assertEquals(filteredData.size(),attrSelectedData.size());
        System.out.println("passed\ncheck attribute name");
        for(int i=0;i<filteredData.size();i++){
            System.out.println("i="+String.valueOf(i)+"...");
            assertTrue(Util4Tests.hasSameAttributes(filteredData.get(i).instance(0),
                                                    attrSelectedData.get(i)));
        }


    }
}

