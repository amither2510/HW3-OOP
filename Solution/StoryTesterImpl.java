package Solution;

import Provided.*;
import junit.framework.ComparisonFailure;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StoryTesterImpl implements StoryTester {

    @Override
    public void testOnInheritanceTree(String story, Class<?> testClass) throws Exception {
        if (story==null || testClass==null) throw new IllegalArgumentException();
        TestClassBackUp backUp = new TestClassBackUp(testClass);
        StoryTestExceptionImpl mangeStory = new StoryTestExceptionImpl();
        try {
            Constructor<?> cons = testClass.getDeclaredConstructor();
            cons.setAccessible(true);
            Object testClassInst = cons.newInstance();
            CallerFunctionBody(story,testClassInst,mangeStory,backUp);
        }
        catch (WordNotFoundException e){
           throw e;
        }
    }

    @Override
    public void testOnNestedClasses(String story, Class<?> testClass) throws Exception {
        if (story == null || testClass == null) throw new IllegalArgumentException();
        Constructor<?> cons = testClass.getDeclaredConstructor();
        cons.setAccessible(true);
        Object testClassInst = cons.newInstance();
        try {
            testOnInheritanceTree(story, testClass);
        } catch (GivenNotFoundException e) {
            if (testClass.equals(Object.class)) {
                throw new GivenNotFoundException();
            }
            try {
                Class<?>[] innerClasses = testClass.getDeclaredClasses();
                if(InvokeSafelyTheInnerClasses(story, innerClasses, testClassInst)){
                    testOnNestedClasses(story, testClass.getSuperclass());
                }
            }
            catch (SucessForInnerException e4){
                //good innerfunction suceess
                return;
            }
            catch ( StoryTestExceptionImpl e3) {
                throw e3;
            }
        }
    }


    /**
     * back up the object for saving data
     * @param s its the keyword (When,Given,then)
     * @param backUp - the Object we save the backUp
     * @param testClassInst - the instance of the object we are working on
     * @throws Exception
     */
    private void backUpObject(String s, TestClassBackUp backUp, Object testClassInst) throws Exception {
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

    /**
     * This is the main function that run Sentence of type 2 and also if there fails she record them int the
     * ExceptionStory that we are returning if there fails.
     * @param subSentence - sentence of type 2
     * @param method - the method we wish to invoke
     * @param testClassInst - instance of the object we are using
     * @param mangeStory - the ExceptionStory that use to mange the fails in the then Sentence.
     * @param line - this param used for saving ops for making string in and out
     * @param backUp - the backUp obj
     * @return return true if we failed in then sen else false
     */
    private boolean runSentence(String[] subSentence, Method method, Object testClassInst,
                                StoryTestExceptionImpl mangeStory, String line,TestClassBackUp backUp) {
        boolean lastChance = false;
        int count = 0;
        ArrayList<String> allParams = new ArrayList<>();
        ArrayList<String> allExp = new ArrayList<>();
        for (String sub : subSentence) {
            count++;
            Object[] paramsArray = findParameters(sub);
            for (Object param: paramsArray){
                allParams.add(param.toString());
            }
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
                org.junit.ComparisonFailure e1 = (org.junit.ComparisonFailure) e.getCause();
                allExp.add(e1.getActual().toString());
                if(lastChance){
                    mangeStory.setNumberFailures();
                    if(mangeStory.getStoryExpected().isEmpty()){
                        for (String param: allParams){
                            mangeStory.setActualValues(param);
                        }
                    }
                    if(mangeStory.getTestResult().isEmpty()){
                        for(String exp: allExp){
                            mangeStory.setExpectedValues(exp);
                        }
                    }
                    mangeStory.setStoryFailed(line);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * find the parms in sentence
     * @param sentence - the sentence
     * @return return arr of the parms
     */
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


    /**
     * Find the Method we wish to invoke from the Annotation
     * @param str - the string that represent the Annotation
     * @param testClass - the class that we check.
     * @return the method
     * @throws WordNotFoundException
     */
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
        } catch (IndexOutOfBoundsException e) {
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

    /**
     * Clean the params from sentence use for equal the sen in the Annotation value.
     * @param s - the string with the params
     * @return string without params
     */
    private String cleanParam(String s) {
        if(!s.contains(" ")){
            return s;
        }
        String[] str = s.split(" and ");
        return String.join(" and ",Arrays.stream(str).map(m->m.substring(0, m.lastIndexOf(" "))).
                collect(Collectors.toList()));
    }

    /**
     * Invoke innerClass in safe way and search recursively in inner class inside the inner class.
     * @param story - the story we checks
     * @param innerClasses - arr of innerclass of the warped class
     * @param fatherObject - the instance of the father object used for creating inner class
     * @return true if we need keep searching in the superclass
     * @throws Exception
     */
    private boolean InvokeSafelyTheInnerClasses(String story ,Class<?>[] innerClasses, Object fatherObject) throws Exception {
        for(Class<?> innerClass :innerClasses){
            Constructor<?> cons = innerClass.getDeclaredConstructor(fatherObject.getClass());
            cons.setAccessible(true);
            Object innerObject = cons.newInstance(fatherObject);
            if(InvokeAndRunAssigment(story,innerClass,fatherObject)){
                InvokeSafelyTheInnerClasses(story,innerObject.getClass().getDeclaredClasses(),innerObject);
            }
        }
        return true;
    }

    /**
     * used for the inner class method for creating instance of the innerclass and wrap the function that search in
     * @param story - the story we searching
     * @param testClass - the test class we check
     * @param instFather - the father class we need for inst
     * @return true if GivenNotFoundException
     * @throws Exception
     */
    private Boolean InvokeAndRunAssigment(String story, Class<?> testClass,Object instFather) throws Exception{
        if (story==null || testClass==null) throw new IllegalArgumentException();
        TestClassBackUp backUp = new TestClassBackUp(testClass,instFather.getClass(),instFather);
        StoryTestExceptionImpl mangeStory = new StoryTestExceptionImpl();
        try {
            Constructor<?> cons = testClass.getDeclaredConstructor(instFather.getClass());
            cons.setAccessible(true);
            Object testClassInst = cons.newInstance(instFather);
            CallerFunctionBody(story,testClassInst,mangeStory,backUp);
            throw new SucessForInnerException();
        }
        catch (GivenNotFoundException e){
            return true;
        }
        catch (WhenNotFoundException | ThenNotFoundException e1){
            throw e1;
        }
    }

    /**
     * The Working body of that mange all the 1st and 2st sentence
     * @param story - the story we searching
     * @param testClassInst -  the test class we check
     * @param mangeStory - the StoryTestException for mange the fails
     * @param backUp -  used for backup the object
     * @throws Exception
     */
   private void CallerFunctionBody(String story,Object testClassInst, StoryTestExceptionImpl mangeStory,TestClassBackUp backUp
    ) throws Exception{
        String[] lines = story.split("\n");
        for (String str : lines) {
            String[] subSentence = str.split(" or ");
            Method method = findMethodFrom2TypeSentence(subSentence[0], testClassInst.getClass());
            backUpObject(subSentence[0].split(" ",2)[0],backUp,testClassInst);
            try{
                if(runSentence(subSentence,method,testClassInst,mangeStory,str,backUp)){
                    testClassInst= backUp.getObject_backup();
                }
            }catch (ComparisonFailure e){
                testClassInst= backUp.getObject_backup();
            }
        }
        if(mangeStory.getNumFail() >0 ){
            throw mangeStory;
        }
    }

    /**
     * Find the methods withe associated annotationss
     * @param annotation_value - the value (Given,Then,When)
     * @param testClass - the class we use
     * @return the method we found
     */


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
}
