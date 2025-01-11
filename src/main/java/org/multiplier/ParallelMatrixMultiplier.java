package org.multiplier;

import java.util.List;
import java.util.ArrayList;

import java.util.concurrent.*;

public class ParallelMatrixMultiplier implements MatrixMultiplier {

    @Override
    public double[][] multiply(double[][] A, double[][] B) {
        int rowsA = A.length;
        int colsA = A[0].length;
        int colsB = B[0].length;
        double[][] result = new double[rowsA][colsB];

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<Void>> futures = new ArrayList<>();

        for (int i = 0; i < rowsA; i++) {
            final int row = i;
            futures.add(executor.submit(() -> {
                for (int j = 0; j < colsB; j++) {
                    for (int k = 0; k < colsA; k++) {
                        result[row][j] += A[row][k] * B[k][j];
                    }
                }
                return null;
            }));
        }

        // Espera a que todos los hilos terminen
        for (Future<Void> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();
        return result;
    }
}
