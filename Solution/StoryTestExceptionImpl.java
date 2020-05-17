package Solution;

import Provided.StoryTestException;

import java.util.ArrayList;
import java.util.List;

public class StoryTestExceptionImpl extends StoryTestException {
    private String storyFailed;
    private List<String> actualValues;
    private List<String> expectedValues;
    private int numberFailures;
    private boolean setFirst;

    public StoryTestExceptionImpl() {
        actualValues = new ArrayList<>();
        expectedValues = new ArrayList<>();
        numberFailures =0;
        storyFailed =null;
        setFirst= true;

    }

    public void setStoryFailed(String storyFailed) {
        if(setFirst) {
            this.storyFailed = storyFailed;
        }
        setFirst=false;
    }

    public void setActualValues(String actualValues) {
        this.actualValues.add(actualValues);
    }

    public void setExpectedValues(String expectedValues) {
        this.expectedValues.add(expectedValues);
    }

    public void setNumberFailures() {
        this.numberFailures++;
    }

    @Override
    public String getSentence() {
        return storyFailed;
    }

    @Override
    public List<String> getStoryExpected() {
        return expectedValues;
    }

    @Override
    public List<String> getTestResult() {
        return actualValues;
    }

    @Override
    public int getNumFail() {
        return numberFailures;
    }
}
