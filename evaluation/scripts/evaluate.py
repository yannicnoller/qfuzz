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

    if len(sys.argv) != 5:
        raise Exception("usage: fuzzer-out-dir n timeout stepsize")

    fuzzerOutDir = sys.argv[1]
    NUMBER_OF_EXPERIMENTS = int(sys.argv[2])
    EXPERIMENT_TIMEOUT = int(sys.argv[3])
    STEP_SIZE = int(sys.argv[4])

    # Read data
    collected_partition_data = []
    collected_delta_for_max_partition_data = []
    collected_delta_data = []
    time_partition_greater_one = {}

    for i in range(START_INDEX, NUMBER_OF_EXPERIMENTS+1):
        experimentFolderPath = fuzzerOutDir + str(i)
        #print("run " + str(i))

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
                time_partition_greater_one[i] = int(row[0])

            for row in csvreader:
                currentTime = int(row[0])
                fileName = row[1]

                containsBetterPartition = "+partition" in fileName
                containsBetterDelta = "+delta" in fileName

                currentPartition = int(row[2])
                currentDelta = float(row[3])

                if i not in time_partition_greater_one and currentPartition > 1:
                    time_partition_greater_one[i] = currentTime

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

        if i not in time_partition_greater_one:
            time_partition_greater_one[i] = EXPERIMENT_TIMEOUT

    # Aggregate data for partitions
    mean_values_partition = {}
    error_values_partition = {}
    max_values_partition = {}
    partition_values = {}
    for i in range(STEP_SIZE, EXPERIMENT_TIMEOUT+1, STEP_SIZE):
        partition_values[i] = []
        for j in range(START_INDEX-1, NUMBER_OF_EXPERIMENTS):
            partition_values[i].append(collected_partition_data[j][i])
        mean_values_partition[i] = "{0:.2f}".format(sum(partition_values[i])/float(NUMBER_OF_EXPERIMENTS))
        error_values_partition[i] = "{0:.2f}".format(1.960 * numpy.std(partition_values[i])/float(math.sqrt(NUMBER_OF_EXPERIMENTS)))
        max_values_partition[i] = max(partition_values[i])

    mean_values_delta = {}
    error_values_delta = {}
    max_values_delta = {}
    delta_values = {}
    for i in range(STEP_SIZE, EXPERIMENT_TIMEOUT+1, STEP_SIZE):
        delta_values[i] = []
        for j in range(START_INDEX-1, NUMBER_OF_EXPERIMENTS):
            delta_values[i].append(collected_delta_data[j][i])
        mean_values_delta[i] = "{0:.2f}".format(sum(delta_values[i])/float(NUMBER_OF_EXPERIMENTS))
        error_values_delta[i] = "{0:.2f}".format(1.960 * numpy.std(delta_values[i])/float(math.sqrt(NUMBER_OF_EXPERIMENTS)))
        max_values_delta[i] = max(delta_values[i])


    # Calculate maximum delta for maximum number of partitions.
    absolute_max_partition = max_values_partition[EXPERIMENT_TIMEOUT]
    absolute_max_delta = 0
    for j in range(START_INDEX-1, NUMBER_OF_EXPERIMENTS):
        if collected_partition_data[j][EXPERIMENT_TIMEOUT] == absolute_max_partition:
            if collected_delta_data[j][EXPERIMENT_TIMEOUT] > absolute_max_delta:
                absolute_max_delta = collected_delta_data[j][EXPERIMENT_TIMEOUT]

    # Retrieve time to max partition.
    absolut_max_partition = max_values_partition[EXPERIMENT_TIMEOUT]
    times_local_maximum = []
    times_absolute_maximum = []
    min_time_absolute_max = EXPERIMENT_TIMEOUT
    for j in range(START_INDEX-1, NUMBER_OF_EXPERIMENTS):
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
    headers = ['seconds', 'p_avg_highscore', 'p_ci', 'p_max', 'd_avg_highscore', 'd_ci', 'd_max']
    outputFileName = fuzzerOutDir + "results-n=" + str(NUMBER_OF_EXPERIMENTS) + "-t=" + str(EXPERIMENT_TIMEOUT) + "-s=" + str(STEP_SIZE) + ".csv"
    print (outputFileName)
    with open(outputFileName, "w") as csv_file:
        writer = csv.DictWriter(csv_file, fieldnames=headers)
        writer.writeheader()
        for timeBucket in range(STEP_SIZE, EXPERIMENT_TIMEOUT+1, STEP_SIZE):
            values = {'seconds' : int(timeBucket)}
            values['p_avg_highscore'] = mean_values_partition[timeBucket]
            values['p_ci'] = error_values_partition[timeBucket]
            values['p_max'] = max_values_partition[timeBucket]
            values['d_avg_highscore'] = mean_values_delta[timeBucket]
            values['d_ci'] = error_values_delta[timeBucket]
            values['d_max'] = max_values_delta[timeBucket]
            writer.writerow(values)

        time_values = list(time_partition_greater_one.values())
        if len(time_values) == NUMBER_OF_EXPERIMENTS:
            avg_time = "{0:.2f}".format(sum(time_values)/float(NUMBER_OF_EXPERIMENTS))
            error = "{0:.2f}".format(1.960 * numpy.std(time_values)/float(math.sqrt(NUMBER_OF_EXPERIMENTS)))

            # Mean time to local maximum partition (might not be a valid metric.)
            mean_time_local_max = "{0:.2f}".format(sum(times_local_maximum)/float(NUMBER_OF_EXPERIMENTS))
            error_mean_time_local_max = "{0:.2f}".format(1.960 * numpy.std(times_local_maximum)/float(math.sqrt(NUMBER_OF_EXPERIMENTS)))

            # Mean time to absolute maximum partition (at least EXPERIMENT_TIMEOUT).
            mean_time_absolute_max = "{0:.2f}".format(sum(times_absolute_maximum)/float(NUMBER_OF_EXPERIMENTS))
            error_mean_time_absolute_max = "{0:.2f}".format(1.960 * numpy.std(times_absolute_maximum)/float(math.sqrt(NUMBER_OF_EXPERIMENTS)))

            csv_file.write("\nabsolute max #partition: " + str(absolute_max_partition) + "\nmax delta for absolute max #partition: " + str(absolute_max_delta) + "\n\ntime partition>1:\n" + str(avg_time) + " (+/- " + str(error) + ")\npartition>1Times=" + str(time_values) + "\n\n#partitions=" + str(partition_values[EXPERIMENT_TIMEOUT]) + "\n#partitions(30s)=" + str(partition_values[30]) + "\n\ndeltas=" + str(delta_values[EXPERIMENT_TIMEOUT]) + "\ndeltas(30s)=" + str(delta_values[30]) + "\n\ntime local max #partition: " + str(mean_time_local_max)+ " (+/- " + str(error_mean_time_local_max) + ")\ntime absolute max #partition: " + str(mean_time_absolute_max) +" (+/- " + str(error_mean_time_absolute_max) + ")\nmin time absolute max #partition: " + str(min_time_absolute_max) + "\n\n times_absolute_maximum=" + str(times_absolute_maximum) + "\n")
        else:
            csv_file.write("\nabsolute max #partition: " + str(absolute_max_partition) + "\nmax delta for absolute max #partition: " + str(absolute_max_delta) + "\n\ntime partition>1: -\npartition>1Times=" + str(time_values) + "\n\n#partitions=" + str(partition_values[EXPERIMENT_TIMEOUT]) + "\n#partitions(30s)=" + str(partition_values[30]) + "\n\ndeltas=" + str(delta_values[EXPERIMENT_TIMEOUT]) + "\ndeltas(30s)=" + str(delta_values[30]) + "\n\ntime local max #partition:  -\ntime absolute max #partition: -\nmin time absolute max #partition: " + str(min_time_absolute_max) + "\n times_absolute_maximum=" + str(times_absolute_maximum) + "\n")
