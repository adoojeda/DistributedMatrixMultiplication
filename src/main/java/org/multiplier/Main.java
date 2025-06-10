package org.multiplier;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;

public class Main {
    public static void main(String[] args) {
        int[] sizes = {128, 256, 512, 1024, 2048, 4096, 8192};

        for (int size : sizes) {
            double[][] A = generateMatrix(size);
            double[][] B = generateMatrix(size);

            System.out.println("--------------------------------------------------");

            MatrixMultiplication basicMultiplication = new MatrixMultiplication(new BasicMatrixMultiplier());
            Runtime runtime = Runtime.getRuntime();
            long memBefore = runtime.totalMemory() - runtime.freeMemory();
            double cpuBefore = getCpuLoad();
            long startTime = System.nanoTime();
            double[][] resultBasic = basicMultiplication.multiply(A, B);
            long endTime = System.nanoTime();
            double cpuAfter = getCpuLoad();
            long memAfter = runtime.totalMemory() - runtime.freeMemory();
            System.out.println("Basic version time (" + size + "x" + size + "): " + (endTime - startTime) + " nanoseconds");
            System.out.println("Basic memory used: " + ((memAfter - memBefore) / (1024 * 1024)) + " MB");
            double avgCpu = (cpuAfter + cpuBefore) / 2 * 100;
            System.out.println("Basic CPU usage: " + String.format("%.2f", avgCpu) + " %");

            MatrixMultiplication parallelMultiplication = new MatrixMultiplication(new ParallelMatrixMultiplier());
            memBefore = runtime.totalMemory() - runtime.freeMemory();
            cpuBefore = getCpuLoad();
            startTime = System.nanoTime();
            double[][] resultParallel = parallelMultiplication.multiply(A, B);
            endTime = System.nanoTime();
            cpuAfter = getCpuLoad();
            memAfter = runtime.totalMemory() - runtime.freeMemory();
            System.out.println("Parallel version time (" + size + "x" + size + "): " + (endTime - startTime) + " nanoseconds");
            System.out.println("Parallel memory used: " + ((memAfter - memBefore) / (1024 * 1024)) + " MB");
            double avgCpuParallel = (cpuAfter + cpuBefore) / 2 * 100;
            System.out.println("Parallel CPU usage: " + String.format("%.2f", avgCpuParallel) + " %");
            System.out.println("Parallel result is valid " + (compareMatrices(resultBasic, resultParallel) ? "✅" : "❌"));

            Config config = new Config();
            config.setInstanceName("hazelcast-instance");
            HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(config);
            IExecutorService executorService = hazelcastInstance.getExecutorService("matrix-executor");

            MatrixMultiplication distributedMultiplication = new MatrixMultiplication(
                    new DistributedMatrixMultiplier(executorService, hazelcastInstance)
            );
            memBefore = runtime.totalMemory() - runtime.freeMemory();
            cpuBefore = getCpuLoad();
            startTime = System.nanoTime();
            double[][] resultDistributed = distributedMultiplication.multiply(A, B);
            endTime = System.nanoTime();
            cpuAfter = getCpuLoad();
            memAfter = runtime.totalMemory() - runtime.freeMemory();
            System.out.println("Distributed version time (" + size + "x" + size + "): " + (endTime - startTime) + " nanoseconds");
            System.out.println("Distributed memory used: " + ((memAfter - memBefore) / (1024 * 1024)) + " MB");
            double avgCpuDistributed = (cpuAfter + cpuBefore) / 2 * 100;
            System.out.println("Basic CPU usage: " + String.format("%.2f", avgCpuDistributed) + " %");
            System.out.println("Distributed result is valid " + (compareMatrices(resultBasic, resultDistributed) ? "✅" : "❌"));

            hazelcastInstance.shutdown();
        }
    }

    public static double[][] generateMatrix(int size) {
        double[][] matrix = new double[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = Math.random() * 100;
            }
        }
        return matrix;
    }

    public static boolean compareMatrices(double[][] a, double[][] b) {
        double epsilon = 1e-6;
        if (a.length != b.length || a[0].length != b[0].length) return false;
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[0].length; j++) {
                if (Math.abs(a[i][j] - b[i][j]) > epsilon) {
                    return false;
                }
            }
        }
        return true;
    }

    private static double getCpuLoad() {
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        return osBean.getProcessCpuLoad();
    }
}
