# DistributedMatrixMultiplication

This project implements and compares three approaches to matrix multiplication:

- **Basic (sequential)**
- **Parallel (multi-threaded)**
- **Distributed (Hazelcast-based)**

The goal is to analyze and benchmark performance in terms of **execution time**, **CPU usage**, and **memory consumption**.

## Languages Used

- Java — for all three approaches using multithreading and Hazelcast for distributed execution.
- Python — alternative implementation using multiprocessing and Hazelcast client.

## Results

Performance metrics were collected for matrix sizes from `128x128` to `4096x4096`.  
You can find the full analysis and generated plots in [DistributedMatrixMultiplication.pdf](./DistributedMatrixMultiplication.pdf).
