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
                // Method method = findKeyWord(stringWithoutParmas, testClass);
                String[] subSentence = str.split(" or ");
                Method method = findMethodFrom2TypeSentence(subSentence[0], testClass);
                for (String sub : subSentence) {
                    paramsArray = findParameters(sub);
                    method.invoke(testClassInst, paramsArray);
                }
            }
        }
        catch (Exception e){

        }
    }

    public ArrayList<Object> findParameters(String sentence){
        ArrayList<Object> paramsArray = new ArrayList<>();
        String[] arrString = sentence.split( " and ");
        for(String str: arrString){
            String[] arr_str=str.split(" ");
                try
                {
                    Integer param = Integer.parseInt(arr_str[arr_str.length-1]);
                    paramsArray.add(param);
                }
                // then its string
                catch(NumberFormatException er)
                {
                    paramsArray.add(arr_str[arr_str.length-1]);
                }
            }
        return paramsArray;
    }

    @Override
    public void testOnNestedClasses(String story, Class<?> testClass) throws Exception {
        //TODO: Implement
    }

    //stt= keyWord and 2 type sentence
    public Method findMethodFrom2TypeSentence(String str,  Class<?> testClass ) throws Exception{
        try {
            String[] array = str.split(" ", 2);
            String equal_str = cleanParam(array[1]);
            if (array[0].equals("Given")) {
                return getMethodsGivenAnnotation(equal_str, testClass);
            } else if (array[0].equals("When")) {
                return getMethodsWhenAnnotation(equal_str, testClass);
            } else if (array[0].equals("Then")) {
                return getMethodsThenAnnotation(equal_str, testClass);
            } else {
                // wrong parse maybe throw exception
                throw new Exception();
            }
        } catch (Exception e) {  //TODO:: Exception
            findMethodFrom2TypeSentence(str,testClass.getSuperclass());
        }
    }

    private String cleanParam(String s) {
        String[] str = s.split(" and ");
        return String.join(" and ",Arrays.stream(str).map(m->m.substring(0, m.lastIndexOf(" "))).
                collect(Collectors.toList()));
    }



    public Method getMethodsGivenAnnotation(String annotation_value, Class<?> testClass){
        //get all methods in testCall set witch When annotation witch annotation_value
        return Arrays.stream(testClass.getMethods()).
                //filter annotation
                        filter(m->m.isAnnotationPresent(Given.class)).
                //filter value annotation
                        filter(m->(m.getAnnotation(Given.class).value().replaceAll(" \\&[^ ]*", "").
                        equals(annotation_value))).collect(Collectors.toList()).get(0);
    }
    public Method getMethodsWhenAnnotation(String annotation_value, Class<?> testClass){
        //get all methods in testCall set witch When annotation witch annotation_value
        return Arrays.stream(testClass.getMethods()).
                //filter annotation
                        filter(m->m.isAnnotationPresent(When.class)).
                //filter value annotation
                        filter(m->(m.getAnnotation(When.class).value().
                        replaceAll(" \\&[^ ]*", "").equals(annotation_value))).
                        collect(Collectors.toList()).get(0);
    }
    public Method getMethodsThenAnnotation(String annotation_value, Class<?> testClass){
        //get all methods in testCall set witch When annotation witch annotation_value
        return Arrays.stream(testClass.getMethods()).
                //filter annotation
                        filter(m->m.isAnnotationPresent(Then.class)).
                //filter value annotation
                        filter(m->(m.getAnnotation(Then.class).value().
                        replaceAll(" \\&[^ ]*", "").equals(annotation_value))).
                        collect(Collectors.toList()).get(0);
    }
}
