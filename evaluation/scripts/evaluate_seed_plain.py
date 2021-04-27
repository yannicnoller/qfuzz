"""
    Script to aggregate the results from an experiment.

    Input: source folder path, e.g.
    python3 evaluate.py blazer_login_unsafe/fuzzer-out-

"""
import sys
import csv
import statistics
import math
import numpy

# do not change this parameters
START_INDEX = 1

if __name__ == '__main__':

    if len(sys.argv) != 7:
        raise Exception("usage: WORKING_PATH SUBJECT_NAME NUMBER_OF_SEEDS NUMBER_OF_EXPERIMENTS EXPERIMENT_TIMEOUT STEP_SIZE")

    WORKING_PATH = sys.argv[1]
    SUBJECT_NAME = sys.argv[2]
    NUMBER_OF_SEEDS = int(sys.argv[3])
    NUMBER_OF_EXPERIMENTS = int(sys.argv[4])
    EXPERIMENT_TIMEOUT = int(sys.argv[5])
    STEP_SIZE = int(sys.argv[6])

    # Read data
    collected_partition_data = []
    collected_delta_for_max_partition_data = []
    collected_delta_data = []
    time_partition_greater_one = {}

    for seedId in range(START_INDEX, NUMBER_OF_SEEDS+1):
        for i in range(START_INDEX, NUMBER_OF_EXPERIMENTS+1):
            experimentFolderPath = WORKING_PATH + "/" + SUBJECT_NAME + "_" + str(seedId) + "/" + "fuzzer-out-" + str(i)
            #print(experimentFolderPath)

            dataId = (seedId - 1) * NUMBER_OF_EXPERIMENTS + i
            #print(dataId)

            partition_data = {}
            delta_data = {}
            dataFile = experimentFolderPath + "/afl/path_costs.csv"
            with open(dataFile,'r') as csvfile:
                csvreader = csv.reader(csvfile, delimiter=';')
                timeBucket = STEP_SIZE
                next(csvreader) # skip first row

                # seed input
                row = next(csvreader)
                previousHighscorePartition = int(row[2])
                previousHighscoreDelta = float(row[3])
                currentHighscorePartition = int(row[2])
                currentHighscoreDelta = float(row[3])

                if currentHighscorePartition > 1:
                    time_partition_greater_one[dataId] = int(row[0])

                for row in csvreader:
                    currentTime = int(row[0])
                    fileName = row[1]

                    containsBetterPartition = "+partition" in fileName
                    containsBetterDelta = "+delta" in fileName

                    currentPartition = int(row[2])
                    currentDelta = float(row[3])

                    if i not in time_partition_greater_one and currentPartition > 1:
                        time_partition_greater_one[dataId] = currentTime

                    if containsBetterPartition:
                        currentHighscorePartition = currentPartition

                    if containsBetterDelta:
                        currentHighscoreDelta = currentDelta

                    while (currentTime > timeBucket):
                        partition_data[timeBucket] = previousHighscorePartition
                        delta_data[timeBucket] = previousHighscoreDelta
                        timeBucket += STEP_SIZE

                    previousHighscorePartition = currentHighscorePartition
                    previousHighscoreDelta = currentHighscoreDelta

                    if timeBucket > EXPERIMENT_TIMEOUT:
                        break

                # fill data with last known value if not enough information
                while timeBucket <= EXPERIMENT_TIMEOUT:
                    partition_data[timeBucket] = previousHighscorePartition
                    delta_data[timeBucket] = previousHighscoreDelta
                    timeBucket += STEP_SIZE

            collected_partition_data.append(partition_data)
            collected_delta_data.append(delta_data)

            if dataId not in time_partition_greater_one:
                time_partition_greater_one[dataId] = EXPERIMENT_TIMEOUT

    # Aggregate data for partitions
    mean_values_partition = {}
    error_values_partition = {}
    max_values_partition = {}
    partition_values = {}
    for i in range(STEP_SIZE, EXPERIMENT_TIMEOUT+1, STEP_SIZE):
        partition_values[i] = []
        for j in range(START_INDEX-1, NUMBER_OF_EXPERIMENTS*NUMBER_OF_SEEDS):
            partition_values[i].append(collected_partition_data[j][i])
        mean_values_partition[i] = "{0:.2f}".format(sum(partition_values[i])/float(NUMBER_OF_EXPERIMENTS*NUMBER_OF_SEEDS))
        error_values_partition[i] = "{0:.2f}".format(1.960 * numpy.std(partition_values[i])/float(math.sqrt(NUMBER_OF_EXPERIMENTS*NUMBER_OF_SEEDS)))
        max_values_partition[i] = max(partition_values[i])

    mean_values_delta = {}
    error_values_delta = {}
    max_values_delta = {}
    delta_values = {}
    for i in range(STEP_SIZE, EXPERIMENT_TIMEOUT+1, STEP_SIZE):
        delta_values[i] = []
        for j in range(START_INDEX-1, NUMBER_OF_EXPERIMENTS*NUMBER_OF_SEEDS):
            delta_values[i].append(collected_delta_data[j][i])
        mean_values_delta[i] = "{0:.2f}".format(sum(delta_values[i])/float(NUMBER_OF_EXPERIMENTS*NUMBER_OF_SEEDS))
        error_values_delta[i] = "{0:.2f}".format(1.960 * numpy.std(delta_values[i])/float(math.sqrt(NUMBER_OF_EXPERIMENTS*NUMBER_OF_SEEDS)))
        max_values_delta[i] = max(delta_values[i])


    # Calculate maximum delta for maximum number of partitions.
    absolute_max_partition = max_values_partition[EXPERIMENT_TIMEOUT]
    absolute_max_delta = 0
    for j in range(START_INDEX-1, NUMBER_OF_EXPERIMENTS*NUMBER_OF_SEEDS):
        if collected_partition_data[j][EXPERIMENT_TIMEOUT] == absolute_max_partition:
            if collected_delta_data[j][EXPERIMENT_TIMEOUT] > absolute_max_delta:
                absolute_max_delta = collected_delta_data[j][EXPERIMENT_TIMEOUT]

    # Retrieve time to max partition.
    absolut_max_partition = max_values_partition[EXPERIMENT_TIMEOUT]
    times_local_maximum = []
    times_absolute_maximum = []
    min_time_absolute_max = EXPERIMENT_TIMEOUT
    for j in range(START_INDEX-1, NUMBER_OF_EXPERIMENTS*NUMBER_OF_SEEDS):
        current_max_partition = partition_values[EXPERIMENT_TIMEOUT][j]
        current_time_max_partition = EXPERIMENT_TIMEOUT
        for i in range(EXPERIMENT_TIMEOUT, STEP_SIZE-1, (-1)*STEP_SIZE):
            if partition_values[i][j] != current_max_partition:
                break
            current_time_max_partition = i
        times_local_maximum.append(current_time_max_partition)

        if current_max_partition == absolut_max_partition and min_time_absolute_max > current_time_max_partition:
            min_time_absolute_max = current_time_max_partition

        if current_max_partition == absolut_max_partition:
            times_absolute_maximum.append(current_time_max_partition)
        else:
            times_absolute_maximum.append(EXPERIMENT_TIMEOUT)

    # Write collected data
    headers = ['seconds', 'c_avg_highscore', 'c_ci', 'c_max', 'd_avg_highscore', 'd_ci', 'd_max']
    outputFileName = WORKING_PATH + "/" + SUBJECT_NAME + "-results-m=" + str(NUMBER_OF_SEEDS) + "-n=" + str(NUMBER_OF_EXPERIMENTS) + "-t=" + str(EXPERIMENT_TIMEOUT) + "-s=" + str(STEP_SIZE) + ".csv"
    print (outputFileName)
    with open(outputFileName, "w") as csv_file:
        writer = csv.DictWriter(csv_file, fieldnames=headers)
        writer.writeheader()
        for timeBucket in range(STEP_SIZE, EXPERIMENT_TIMEOUT+1, STEP_SIZE):
            values = {'seconds' : int(timeBucket)}
            values['c_avg_highscore'] = mean_values_partition[timeBucket]
            values['c_ci'] = error_values_partition[timeBucket]
            values['c_max'] = max_values_partition[timeBucket]
            values['d_avg_highscore'] = mean_values_delta[timeBucket]
            values['d_ci'] = error_values_delta[timeBucket]
            values['d_max'] = max_values_delta[timeBucket]
            writer.writerow(values)
