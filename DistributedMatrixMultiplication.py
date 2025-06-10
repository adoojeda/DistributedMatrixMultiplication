import hazelcast
import numpy as np
import time
import psutil
import pandas as pd
import os
from multiprocessing import Pool, cpu_count

# Hazelcast client
client = hazelcast.HazelcastClient()
matrix_map = client.get_map("matrix_map").blocking()

# Monitor system resources
def monitor_resources():
    process = psutil.Process(os.getpid())
    cpu_usage = psutil.cpu_percent(interval=None)
    memory_usage_mb = process.memory_info().rss / (1024 * 1024)
    return cpu_usage, memory_usage_mb

# Basic matrix multiplication
def basic_multiply(A, B):
    return np.dot(A, B)

# Worker for parallel multiplication
def parallel_worker(args):
    A_row, B = args
    return np.dot(A_row, B)

# Parallel matrix multiplication
def parallel_multiply(A, B):
    with Pool(processes=cpu_count()) as pool:
        result = pool.map(parallel_worker, [(row, B) for row in A])
    return np.array(result)

# Divide matrix into blocks
def divide_matrix(matrix, num_blocks):
    rows, cols = matrix.shape
    block_size_row = rows // num_blocks
    block_size_col = cols // num_blocks
    blocks = []
    for i in range(num_blocks):
        for j in range(num_blocks):
            start_row = i * block_size_row
            end_row = (i + 1) * block_size_row if i < num_blocks - 1 else rows
            start_col = j * block_size_col
            end_col = (j + 1) * block_size_col if j < num_blocks - 1 else cols
            blocks.append(matrix[start_row:end_row, start_col:end_col])
    return blocks

# Distributed matrix multiplication using Hazelcast
def distributed_multiply(A, B, num_blocks=4):
    blocks_A = divide_matrix(A, num_blocks)
    blocks_B = divide_matrix(B, num_blocks)

    for i in range(len(blocks_A)):
        result_block = np.dot(blocks_A[i], blocks_B[i])
        matrix_map.put(i, result_block.tolist())

    result = np.zeros((A.shape[0], B.shape[1]))
    for i in range(len(blocks_A)):
        block_result = np.array(matrix_map.get(i))
        row_start = (i // num_blocks) * (A.shape[0] // num_blocks)
        row_end = (i // num_blocks + 1) * (A.shape[0] // num_blocks) if (i // num_blocks) < num_blocks - 1 else A.shape[0]
        col_start = (i % num_blocks) * (B.shape[1] // num_blocks)
        col_end = (i % num_blocks + 1) * (B.shape[1] // num_blocks) if (i % num_blocks) < num_blocks - 1 else B.shape[1]
        result[row_start:row_end, col_start:col_end] = block_result
    return result

# Validate matrices are equal
def compare_matrices(mat1, mat2):
    return np.allclose(mat1, mat2, atol=1e-6)

# Run one experiment
def run_experiment(matrix_size):
    print(f"\n🔎 Matrix size: {matrix_size}x{matrix_size}")
    A = np.random.rand(matrix_size, matrix_size)
    B = np.random.rand(matrix_size, matrix_size)

    results = []

    # Basic
    start = time.perf_counter_ns()
    C_basic = basic_multiply(A, B)
    end = time.perf_counter_ns()
    cpu, mem = monitor_resources()
    results.append(("Basic", end - start, cpu, mem))

    # Parallel
    start = time.perf_counter_ns()
    C_parallel = parallel_multiply(A, B)
    end = time.perf_counter_ns()
    cpu, mem = monitor_resources()
    valid_parallel = compare_matrices(C_basic, C_parallel)
    results.append(("Parallel", end - start, cpu, mem, valid_parallel))

    # Distributed
    start = time.perf_counter_ns()
    C_distributed = distributed_multiply(A, B)
    end = time.perf_counter_ns()
    cpu, mem = monitor_resources()
    valid_distributed = compare_matrices(C_basic, C_distributed)
    results.append(("Distributed", end - start, cpu, mem, valid_distributed))

    # Print results
    for res in results:
        name = res[0]
        time_ns = res[1]
        cpu_usage = round(res[2], 2)
        mem_usage = round(res[3])
        valid = f"✅" if len(res) == 5 and res[4] else ""
        print(f"{name} -> Time: {time_ns} ns | CPU: {cpu_usage}% | Memory: {mem_usage} MB {valid}")

    return {
        "Matrix Size": f"{matrix_size}x{matrix_size}",
        "Basic Time (ns)": results[0][1],
        "Basic CPU (%)": round(results[0][2], 2),
        "Basic Mem (MB)": round(results[0][3]),
        "Parallel Time (ns)": results[1][1],
        "Parallel CPU (%)": round(results[1][2], 2),
        "Parallel Mem (MB)": round(results[1][3]),
        "Parallel Valid": results[1][4],
        "Distributed Time (ns)": results[2][1],
        "Distributed CPU (%)": round(results[2][2], 2),
        "Distributed Mem (MB)": round(results[2][3]),
        "Distributed Valid": results[2][4],
    }

if __name__ == "__main__":
    matrix_sizes = [128, 256, 512, 1024, 2048, 4096]
    all_results = [run_experiment(size) for size in matrix_sizes]

    df = pd.DataFrame(all_results)
    df.to_csv("comparison_results.csv", index=False)

    client.shutdown()
