package org.multiplier;

public class MatrixMultiplication {

    private final MatrixMultiplier matrixMultiplier;

    public MatrixMultiplication(MatrixMultiplier matrixMultiplier) {
        this.matrixMultiplier = matrixMultiplier;
    }

    public double[][] multiply(double[][] A, double[][] B) {
        return matrixMultiplier.multiply(A, B);
    }
}
