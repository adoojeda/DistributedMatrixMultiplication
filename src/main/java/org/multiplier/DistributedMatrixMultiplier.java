package org.multiplier;

import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.HazelcastInstance;

import java.util.*;
import java.util.concurrent.*;

public class DistributedMatrixMultiplier implements MatrixMultiplier {

    private final IExecutorService executorService;
    private final HazelcastInstance hazelcastInstance;
    private final String matrixBMapName = "matrix-map";

    public DistributedMatrixMultiplier(IExecutorService executorService, HazelcastInstance hazelcastInstance) {
        this.executorService = executorService;
        this.hazelcastInstance = hazelcastInstance;
    }

    @Override
    public double[][] multiply(double[][] A, double[][] B) {
        int rowsA = A.length;
        int colsB = B[0].length;
        double[][] result = new double[rowsA][colsB];

        hazelcastInstance.getMap(matrixBMapName).put("B", B);

        List<Future<RowResult>> futures = new ArrayList<>();

        for (int i = 0; i < rowsA; i++) {
            DistributedRowTask task = new DistributedRowTask(i, A[i], matrixBMapName);
            futures.add(executorService.submit(task));
        }

        for (Future<RowResult> future : futures) {
            try {
                RowResult rowResult = future.get();
                result[rowResult.getRowIndex()] = rowResult.getRowData();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return result;
    }
}
