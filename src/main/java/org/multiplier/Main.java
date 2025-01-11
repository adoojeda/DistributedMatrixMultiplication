package org.multiplier;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.config.SerializationConfig;

public class Main {
    public static void main(String[] args) {
        // Definir tamaños de matrices para la evaluación de escalabilidad
        int[] sizes = {500, 1000, 1500, 3000, 5000};

        for (int size : sizes) {
            double[][] A = generateMatrix(size);
            double[][] B = generateMatrix(size);

            // Básica
            MatrixMultiplication basicMultiplication = new MatrixMultiplication(new BasicMatrixMultiplier());
            long startTime = System.nanoTime();
            double[][] resultBasic = basicMultiplication.multiply(A, B);
            long endTime = System.nanoTime();
            System.out.println("Basic version time (" + size + "x" + size + "): " + (endTime - startTime) + " nanoseconds");

            // Paralela
            MatrixMultiplication parallelMultiplication = new MatrixMultiplication(new ParallelMatrixMultiplier());
            startTime = System.nanoTime();
            double[][] resultParallel = parallelMultiplication.multiply(A, B);
            endTime = System.nanoTime();
            System.out.println("Parallel version time (" + size + "x" + size + "): " + (endTime - startTime) + " nanoseconds");

            // Distribuida
            // Configuración de Hazelcast sin serialización compacta
            Config config = new Config();

            // Evitar serialización compacta: No necesitas configurar nada adicional para evitarla

            HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(config);  // Instancia Hazelcast
            IExecutorService executorService = hazelcastInstance.getExecutorService("matrix-executor");

            MatrixMultiplication distributedMultiplication = new MatrixMultiplication(new DistributedMatrixMultiplier(executorService, hazelcastInstance));
            startTime = System.nanoTime();
            double[][] resultDistributed = distributedMultiplication.multiply(A, B);
            endTime = System.nanoTime();
            System.out.println("Distributed version time (" + size + "x" + size + "): " + (endTime - startTime) + " nanoseconds");

            // Cerrar Hazelcast
            hazelcastInstance.shutdown();
        }
    }

    // Método para generar matrices de tamaño n x n con valores aleatorios
    public static double[][] generateMatrix(int size) {
        double[][] matrix = new double[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = Math.random() * 100;  // Llena con valores aleatorios
            }
        }
        return matrix;
    }
}
