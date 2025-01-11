package org.multiplier;

import java.util.concurrent.Callable;

public class MatrixMultiplicationTask implements Callable<Void> {

    private final double[][] A;
    private final double[][] B;
    private final int row;
    private final double[][] result;

    public MatrixMultiplicationTask(double[][] A, double[][] B, int row, double[][] result) {
        this.A = A;
        this.B = B;
        this.row = row;
        this.result = result;
    }

    @Override
    public Void call() throws Exception {
        int colsA = A[0].length;
        int colsB = B[0].length;

        // Realizar la multiplicación para una fila específica
        for (int j = 0; j < colsB; j++) {
            for (int k = 0; k < colsA; k++) {
                result[row][j] += A[row][k] * B[k][j];
            }
        }

        return null;
    }
}
