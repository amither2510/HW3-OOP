package Solution;

import Provided.*;
import junit.framework.ComparisonFailure;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class StoryTesterImpl implements StoryTester {
    @Override
    public void testOnInheritanceTree(String story, Class<?> testClass) throws Exception {
        if (story==null || testClass==null) throw new IllegalArgumentException();
        TestClassBackUp backUp = new TestClassBackUp(testClass);
        StoryTestExceptionImpl mangeStory = new StoryTestExceptionImpl();
        try {


            String[] lines = story.split("\n");
            // building the object
            Constructor<?> cons = testClass.getDeclaredConstructor();
            cons.setAccessible(true);
            Object testClassInst = cons.newInstance();

            for (String str : lines) {
                String[] subSentence = str.split(" or ");
                Method method = findMethodFrom2TypeSentence(subSentence[0], testClass);
                backUpObject(subSentence[0].split(" ",2)[0],backUp,testClassInst);
                try{
                    runSentence(subSentence,method,testClassInst,mangeStory,str);
                }catch (ComparisonFailure e){
                    testClassInst= backUp.getObject_backup();
                }
            }

        }
        catch (WordNotFoundException e){
            e.printStackTrace();
        }
        if(mangeStory.getNumFail() >0 ){
            //Todo to change
            throw new ComparisonFailure("a","b","c");
        }
    }

    private void backUpObject(String s, TestClassBackUp backUp, Object testClassInst) throws IllegalAccessException {
        if(s.equals("When")){
            if(!backUp.isFlag_backup()){
                backUp.setFlag_backup(true);
                backUp.backUpObject(testClassInst);
            }
        }
        if(s.equals("Then")){
            backUp.setFlag_backup(false);
        }
    }

    private void runSentence(String[] subSentence, Method method, Object testClassInst,StoryTestExceptionImpl mangeStory
    ,String line) {
        boolean lastChance = false;
        int count = 0;
        for (String sub : subSentence) {
            Object[] paramsArray = findParameters(sub);
            if(subSentence.length == count){
                lastChance = true;
            }
            try {
                method.setAccessible(true);
                method.invoke(testClassInst, paramsArray);
                break;  //if invoke sucsses break (else Then throw exception)
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                if(lastChance){
                    //todo check with yahav if true
                    // story failed all the then failed
                    ComparisonFailure e1 = (ComparisonFailure) e.getCause();
                    mangeStory.setNumberFailures();
                    mangeStory.setActualValues(e1.getActual());
                    mangeStory.setExpectedValues(e1.getExpected());
                    mangeStory.setStoryFailed(line);
                }
            }
            count++;

        }
    }


    public Object[] findParameters(String sentence){
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
        int i=0;
        Object[] retObj = new Object[paramsArray.size()];
        for(Object obj : paramsArray){
            retObj[i] = obj;
            i++;
        }
        return retObj;
    }



    //stt= keyWord and 2 type sentence
    public Method findMethodFrom2TypeSentence(String str,  Class<?> testClass ) throws WordNotFoundException{
        try {
            String[] array = str.split(" ", 2);
            String equal_str = cleanParam(array[1]);
            if (array[0].equals("Given")) {
                return getMethodsGivenAnnotation(equal_str, testClass);
            } else if (array[0].equals("When")) {
                return getMethodsWhenAnnotation(equal_str, testClass);
            } else{
                return getMethodsThenAnnotation(equal_str, testClass);
            }
        } catch (IndexOutOfBoundsException e) {  //TODO:: Exception
            String[] array = str.split(" ", 2);
            if(testClass.equals(Object.class)){
                if (array[0].equals("Given")) {
                    throw new GivenNotFoundException();
                } else if (array[0].equals("When")) {
                    throw new WhenNotFoundException();
                } else{
                    throw new ThenNotFoundException();
                }
            }
            return findMethodFrom2TypeSentence(str,testClass.getSuperclass());
        }
    }

    private String cleanParam(String s) {
        String[] str = s.split(" and ");
        return String.join(" and ",Arrays.stream(str).map(m->m.substring(0, m.lastIndexOf(" "))).
                collect(Collectors.toList()));
    }



    public Method getMethodsGivenAnnotation(String annotation_value, Class<?> testClass){
        //get all methods in testCall set witch When annotation witch annotation_value
        return Arrays.stream(testClass.getDeclaredMethods()).
                //filter annotation
                        filter(m->m.isAnnotationPresent(Given.class)).
                //filter value annotation
                        filter(m->(m.getAnnotation(Given.class).value().replaceAll(" \\&[^ ]*", "").
                        equals(annotation_value))).collect(Collectors.toList()).get(0);
    }
    public Method getMethodsWhenAnnotation(String annotation_value, Class<?> testClass){
        //get all methods in testCall set witch When annotation witch annotation_value
        return Arrays.stream(testClass.getDeclaredMethods()).
                //filter annotation
                        filter(m->m.isAnnotationPresent(When.class)).
                //filter value annotation
                        filter(m->(m.getAnnotation(When.class).value().
                        replaceAll(" \\&[^ ]*", "").equals(annotation_value))).
                        collect(Collectors.toList()).get(0);
    }
    public Method getMethodsThenAnnotation(String annotation_value, Class<?> testClass){
        //get all methods in testCall set witch When annotation witch annotation_value
        return Arrays.stream(testClass.getDeclaredMethods()).
                //filter annotation
                        filter(m->m.isAnnotationPresent(Then.class)).
                //filter value annotation
                        filter(m->(m.getAnnotation(Then.class).value().
                        replaceAll(" \\&[^ ]*", "").equals(annotation_value))).
                        collect(Collectors.toList()).get(0);
    }
    @Override
    public void testOnNestedClasses(String story, Class<?> testClass) throws Exception {
        if (story==null || testClass==null) throw new IllegalArgumentException();
        //TODO: Implement
    }
}
