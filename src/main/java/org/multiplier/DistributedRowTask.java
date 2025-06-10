package org.multiplier;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.map.IMap;

import java.io.Serializable;
import java.util.concurrent.Callable;

public class DistributedRowTask implements Callable<RowResult>, Serializable {
    private final int rowIndex;
    private final double[] rowA;
    private final String matrixBMapName;

    public DistributedRowTask(int rowIndex, double[] rowA, String matrixBMapName) {
        this.rowIndex = rowIndex;
        this.rowA = rowA;
        this.matrixBMapName = matrixBMapName;
    }

    @Override
    public RowResult call() {
        HazelcastInstance hz = Hazelcast.getHazelcastInstanceByName("hazelcast-instance");
        IMap<String, double[][]> matrixMap = hz.getMap(matrixBMapName);
        double[][] matrixB = matrixMap.get("B");

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
