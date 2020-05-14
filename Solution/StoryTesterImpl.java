package Solution;

import Provided.StoryTester;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class StoryTesterImpl implements StoryTester {
    @Override
    public void testOnInheritanceTree(String story, Class<?> testClass) throws Exception {
        //TODO: Implement
    }

    @Override
    public void testOnNestedClasses(String story, Class<?> testClass) throws Exception {
        //TODO: Implement
    }
    public ArrayList<String> getGivenValueAndParam(String sentence){
       // System.out.println(Arrays.toString(sentence.split(" ", 3)));
        ArrayList<String> value_param = new ArrayList<>();
        String[] array = sentence.split(" ", 3);
        if(array[0].equals("Given")) {
            value_param.add(array[1].toString() + "&age");
            value_param.add(array[2]);
        }
        return value_param;
    }
    public Method getMethodsGivenAnnotation(String annotation_value, Class<?> testClass){
        //get all methods in testCall set witch When annotation witch annotation_value
        return Arrays.stream(testClass.getMethods()).
                //filter annotation
                        filter(m->m.isAnnotationPresent(Given.class)).
                //filter value annotation
                        filter(m->(m.getAnnotation(Given.class).value()[0].equals(annotation_value))).
                        collect(Collectors.toList()).get(0);
    }
    public Method getMethodsWhenAnnotation(String annotation_value, Class<?> testClass){
        //get all methods in testCall set witch When annotation witch annotation_value
        return Arrays.stream(testClass.getMethods()).
                //filter annotation
                        filter(m->m.isAnnotationPresent(When.class)).
                //filter value annotation
                        filter(m->(m.getAnnotation(When.class).value()[0].equals(annotation_value))).
                        collect(Collectors.toList()).get(0);
    }
    public Method getMethodsThenAnnotation(String annotation_value, Class<?> testClass){
        //get all methods in testCall set witch When annotation witch annotation_value
        return Arrays.stream(testClass.getMethods()).
                //filter annotation
                        filter(m->m.isAnnotationPresent(Then.class)).
                //filter value annotation
                        filter(m->(m.getAnnotation(Then.class).value()[0].equals(annotation_value))).
                        collect(Collectors.toList()).get(0);
    }
}
