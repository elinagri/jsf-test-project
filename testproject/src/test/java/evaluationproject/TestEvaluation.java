package evaluationproject;

import static org.junit.Assert.*;

import org.junit.Test;


public class TestEvaluation {

	@Test
    public void testConcatenate() {
        EvaluationFB myClass = new EvaluationFB();

        String result = myClass.zeichenkettenVerknuepefen("one", "two");

        assertEquals("onetwo", result);

    }
	
}
