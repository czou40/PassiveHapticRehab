package com.example.phl.data.spasticity.data_collection;

import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RawDataset {
    private final int numFeatures;
    private List<Double[]> x = new ArrayList<>();
    private List<Double> y = new ArrayList<>();

    private MultipleLinearRegression regression = new MultipleLinearRegression();

    private boolean isRegressionCalculated = false;

    private static RawDataset instance;

    public RawDataset(int numFeatures) {
        this.numFeatures = numFeatures;
    }

    public int size() {
        return x.size();
    }
    public void add(Double[] x, Double y) {
        if (x.length != numFeatures) {
            throw new IllegalArgumentException("x must have length " + numFeatures);
        }
        this.x.add(x);
        this.y.add(y);
        this.isRegressionCalculated = false;
    }

    public void add(double[] x, double y) {
        Double[] newX = new Double[x.length];
        Arrays.setAll(newX, i -> x[i]);
        add(newX, y);
        this.isRegressionCalculated = false;
    }

    public void addAll(List<Double[]> x, List<Double> y) {
        if (x.size() != y.size()) {
            throw new IllegalArgumentException("x and y must have the same size");
        }
        this.x.addAll(x);
        this.y.addAll(y);
        this.isRegressionCalculated = false;
    }

    public void addAll(double[][] x, double[] y) {
        if (x.length != y.length) {
            throw new IllegalArgumentException("x and y must have the same size");
        }
        for (int i = 0; i < x.length; i++) {
            add(x[i], y[i]);
        }
        this.isRegressionCalculated = false;
    }

    public void addAll(Double[][] x, Double[] y) {
        if (x.length != y.length) {
            throw new IllegalArgumentException("x and y must have the same size");
        }
        this.x.addAll(Arrays.asList(x));
        this.y.addAll(Arrays.asList(y));
        this.isRegressionCalculated = false;
    }


    private void calculateRegression() {
        double[][] x = new double[this.x.size()][numFeatures];
        double[] y = new double[this.y.size()];
        for (int i = 0; i < this.x.size(); i++) {
            for (int j = 0; j < numFeatures; j++) {
                x[i][j] = this.x.get(i)[j];
            }
            y[i] = this.y.get(i);
        }
        regression.newSampleData(y, x);
        isRegressionCalculated = true;
    }

    public double predict(Double[] x) {
        if (!isRegressionCalculated) {
            calculateRegression();
            isRegressionCalculated = true;
        }
        return regression.predict(x);
    }

    public double predict(double[] x) {
        Double[] newX = new Double[x.length];
        Arrays.setAll(newX, i -> x[i]);
        return predict(newX);
    }

    /**
     * This method is used in the simplified version.
     * The phone only collects the data when the phone is placed on the unaffected hand and when the phone is placed on the affected hand.
     * Only two samples are collected.
     * We just return the ratio of the two samples. The higher the ratio, the better.
     * @return
     */
    public double getScore() {
        if (x.size() != 2) {
            throw new IllegalStateException("RawDataset must have exactly 2 samples");
        }
        double sum = 0.0;
        for (int i = 0; i < numFeatures; i++) {
            if (x.get(0)[i] != 0.0) {
                sum += x.get(1)[i] / x.get(0)[i];
            }
        }
        double score = sum / numFeatures;
        return score * 100.0;
    }

    public static RawDataset initializeInstance(int numFeatures) {
        if (instance == null) {
            instance = new RawDataset(numFeatures);
        }
        return instance;
    }

    public static void reset(int numFeatures) {
        instance = new RawDataset(numFeatures);
    }

    public static RawDataset getInstance() {
        if (instance == null) {
            throw new IllegalStateException("RawDataset instance not initialized");
        }
        return instance;
    }

    public List<Double[]> getX() {
        return Collections.unmodifiableList(x);
    }

    public List<Double> getY() {
        return Collections.unmodifiableList(y);
    }

    private static class MultipleLinearRegression extends OLSMultipleLinearRegression {

        RealVector b = null;

        public MultipleLinearRegression() {
            super();
        }

        @Override
        public void newSampleData(double[] y, double[][] x) throws MathIllegalArgumentException {
            super.newSampleData(y, x);
            this.b = calculateBeta();
        }

        @Override
        public void newSampleData(double[] data, int nobs, int nvars) {
            super.newSampleData(data, nobs, nvars);
            this.b = calculateBeta();
        }

        public double predict(Double[] x) {
            RealVector vectorX = new ArrayRealVector(new double[]{1.0});
            vectorX = vectorX.append(new ArrayRealVector(x));
            return vectorX.dotProduct(b);
        }


    }
}
