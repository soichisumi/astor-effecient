package fastrepair;

import org.apache.commons.lang3.tuple.Pair;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import spoon.reflect.declaration.CtElement;

import java.util.*;

/**
 * singleton
 * Created by s-sumi on 16/07/30.
 */
public class Class2IndexConvSingleton {
    private static Class2IndexConvSingleton instance=new Class2IndexConvSingleton();

    private final List<Pair<Class<? extends CtElement>,Integer>> dictionary=new ArrayList<>();
    private List<Class<? extends CtElement>> classList = new ArrayList<>();
    public static int NODE_NUMBER;

    private Class2IndexConvSingleton(){
        //exists to defeat instantiation.
        Set<Class<?>> allClasses = getReflections("spoon.reflect").getSubTypesOf(Object.class);
        allClasses.addAll(getReflections("spoon.support.reflect").getSubTypesOf(Object.class));

        for(Class clazz : allClasses){
            if(CtElement.class.isAssignableFrom(clazz) &&
                    clazz.getSimpleName().toLowerCase().endsWith("impl")){
                if(!classList.contains(clazz))
                    classList.add(clazz);
                else{
                    System.out.println(clazz.getSimpleName()+"is duplicate");
                }
            }
        }

        Collections.sort(classList,
                (a,b)-> a.getSimpleName().compareTo(b.getSimpleName()));    //simplenameでソート

        int i=0;
        for(Class<? extends CtElement> s: classList){
            dictionary.add(Pair.of(s,i));   i++;
        }
        NODE_NUMBER=dictionary.size();
    }

    public static Class2IndexConvSingleton getInstance() { return instance; }

    private static Reflections getReflections(String prefix){
        //get all class from package    //http://stackoverflow.com/questions/520328/can-you-find-alasdfl-classes-in-a-package-using-reflection
        List<ClassLoader> classLoadersList = new LinkedList<ClassLoader>();
        classLoadersList.add(ClasspathHelper.contextClassLoader());
        classLoadersList.add(ClasspathHelper.staticClassLoader());

        return new Reflections(new ConfigurationBuilder()
                .setScanners(new SubTypesScanner(false /* don't exclude Object.class */), new ResourcesScanner())
                .setUrls(ClasspathHelper.forClassLoader(classLoadersList.toArray(new ClassLoader[0])))
                .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix(prefix))));//implはspoon.support.reflect
    }

    protected Class<?> getClassFromIndex(int i){
        return dictionary.get(i).getLeft();
    }
    protected int getNodeNumber(){return NODE_NUMBER;}
    protected List<Class<? extends CtElement>> getClasses(){ return classList; }

}
