package com.example.phl;

import org.junit.Test;

import static org.junit.Assert.*;

import com.example.phl.data.Dataset;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void regression_isCorrect() {
        // weight
        double[] y = new double[] {50,60,70,80,100};
        // height, waist
        double[][] x = new double[5][];
        x[0] = new double[] {1,11};
        x[1] = new double[] {1,13};
        x[2] = new double[] {1,15};
        x[3] = new double[] {1,17};
        x[4] = new double[] {1,21};

        Dataset dataset = new Dataset(2);

        dataset.addAll(x, y);

        assertEquals(90, dataset.predict(new double[] {1,19}), 0.0001);
    }
}