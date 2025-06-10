package org.multiplier;

import java.io.Serializable;
import java.util.concurrent.Callable;

public class RowMultiplicationTask implements Callable<RowResult>, Serializable {

    private final double[] rowA;
    private final double[][] matrixB;
    private final int rowIndex;

    public RowMultiplicationTask(double[] rowA, double[][] matrixB, int rowIndex) {
        this.rowA = rowA;
        this.matrixB = matrixB;
        this.rowIndex = rowIndex;
    }

    @Override
    public RowResult call() {
        int colsB = matrixB[0].length;
        int colsA = rowA.length;
        double[] resultRow = new double[colsB];

        for (int j = 0; j < colsB; j++) {
            for (int k = 0; k < colsA; k++) {
                resultRow[j] += rowA[k] * matrixB[k][j];
            }
        }

        return new RowResult(rowIndex, resultRow);
    }
}

