package Solution;

import Provided.StoryTester;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class StoryTesterImpl implements StoryTester {
    @Override
    public void testOnInheritanceTree(String story, Class<?> testClass) throws Exception {
        try {
            String[] lines = story.split("\n");
            ArrayList<String> sentence = new ArrayList<>();
            ArrayList<Object> paramsArray = new ArrayList<>();
            // building the object
            Constructor<?> cons = testClass.getDeclaredConstructor();
            cons.setAccessible(true);
            Object testClassInst = cons.newInstance();
            for (String str : lines) {
              //  String stringWithoutParmas = str.replaceAll("\\&[^ ]* ", "");
                Method method = findKeyWord(stringWithoutParmas, testClass);
                paramsArray = findParameters(str);
                method.invoke(testClassInst,paramsArray);
            }
        }
        catch (Exception e){

        }
    }
    public String stringFormatForEqual(String str ){
        if(str.contains("and")){
            String[] new_arr = str.split("and");

        }
    }

    public ArrayList<Object> findParameters(String sentence){
        ArrayList<Object> paramsArray = new ArrayList<>();
        String[] arrString = sentence.split( " ");
        for(String str: arrString){
            if(str.startsWith("&")){
                String token = str.replace("&","");
                try
                {
                    Integer parm = Integer.parseInt(token);
                    paramsArray.add(parm);
                }
                // then its string
                catch(NumberFormatException er)
                {
                    paramsArray.add(token);
                }
            }
        }
        return paramsArray;
    }

    @Override
    public void testOnNestedClasses(String story, Class<?> testClass) throws Exception {
        //TODO: Implement
    }
    public Method findKeyWord(String str,  Class<?> testClass ) throws Exception{
        String[] array = str.split(" ", 3);
        if(array[0].equals("Given")){
            return getMethodsGivenAnnotation(str,testClass);
        }
        else if(array[0].equals("When")){
           return  getMethodsWhenAnnotation(str,testClass);
        }
        else if(array[0].equals("Then")){
            return getMethodsThenAnnotation(str,testClass);
        }
        else{
            // wrong parse maybe throw exception
            throw new Exception();
        }
    }






    public Method getMethodsGivenAnnotation(String annotation_value, Class<?> testClass){
        //get all methods in testCall set witch When annotation witch annotation_value
        return Arrays.stream(testClass.getMethods()).
                //filter annotation
                        filter(m->m.isAnnotationPresent(Given.class)).
                //filter value annotation
                        filter(m->(m.getAnnotation(Given.class).value().replaceAll("\\&[^ ]* ", "").
                        equals(annotation_value))).collect(Collectors.toList()).get(0);
    }
    public Method getMethodsWhenAnnotation(String annotation_value, Class<?> testClass){
        //get all methods in testCall set witch When annotation witch annotation_value
        return Arrays.stream(testClass.getMethods()).
                //filter annotation
                        filter(m->m.isAnnotationPresent(When.class)).
                //filter value annotation
                        filter(m->(m.getAnnotation(When.class).value().
                        replaceAll("\\&[^ ]* ", "").equals(annotation_value))).
                        collect(Collectors.toList()).get(0);
    }
    public Method getMethodsThenAnnotation(String annotation_value, Class<?> testClass){
        //get all methods in testCall set witch When annotation witch annotation_value
        return Arrays.stream(testClass.getMethods()).
                //filter annotation
                        filter(m->m.isAnnotationPresent(Then.class)).
                //filter value annotation
                        filter(m->(m.getAnnotation(Then.class).value().
                        replaceAll("\\&[^ ]* ", "").equals(annotation_value))).
                        collect(Collectors.toList()).get(0);
    }
}
