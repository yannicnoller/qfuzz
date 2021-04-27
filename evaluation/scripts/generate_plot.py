"""
    Script to generate the plots for various seed inputs.

    Input: source folder path, e.g.
    python3 generate_plot_seed.py <path-to-experiments-results>/Eclipse_jetty_1_eps1_Greedy 5 30 1800 3 <path-to-experiments-results>

"""
import sys
import csv
import statistics
import math
import numpy
import re

# do not change this parameters
START_INDEX = 1

if __name__ == '__main__':

    if len(sys.argv) != 7:
        raise Exception("usage: python3 generate_plot_seed.py <working-path-incl.-subject> <m> <n> <timeout> <stepsize> <outputDir>")

    subjectDir = sys.argv[1]
    NUMBER_OF_INPUTS = int(sys.argv[2])
    NUMBER_OF_EXPERIMENTS = int(sys.argv[3])
    EXPERIMENT_TIMEOUT = int(sys.argv[4])
    STEP_SIZE = int(sys.argv[5])
    outputDir = sys.argv[6]


    c_avg = {}
    c_ci = {}
    d_avg = {}
    d_ci = {}


    for i in range(1, NUMBER_OF_INPUTS+1):
        data_file = subjectDir + "_" + str(i) +  "/fuzzer-out-results-n=" + str(NUMBER_OF_EXPERIMENTS) + "-t=" + str(EXPERIMENT_TIMEOUT) + "-s=" + str(STEP_SIZE) + ".csv"

        c_avg_tmp = {}
        c_ci_tmp = {}
        d_avg_tmp = {}
        d_ci_tmp = {}

        with open(data_file,'r') as csvfile:
            csvreader = csv.reader(csvfile, delimiter=',')
            next(csvreader) # skip first row
            for row in csvreader:
                if len(row) != 7: break
                time = int(row[0])
                c_avg_tmp[time] = row[1]
                c_ci_tmp[time] = row[2]
                d_avg_tmp[time] = row[4]
                d_ci_tmp[time] = row[5]

        c_avg[i] = c_avg_tmp
        c_ci[i] = c_ci_tmp
        d_avg[i] = d_avg_tmp
        d_ci[i] = d_ci_tmp


    subject_name = subjectDir[subjectDir.rfind("/")+1:]

    headers_partitions = ['seconds']
    for i in range(1, NUMBER_OF_INPUTS+1):
        headers_partitions.append('c_avg_' + str(i))
        headers_partitions.append('c_ci_' + str(i))
    outputFileName_partitions = outputDir + "/_plot_" + subject_name +  "_t=" + str(EXPERIMENT_TIMEOUT) + "_partitions.csv"
    print(outputFileName_partitions)
    with open(outputFileName_partitions, "w") as csv_file:
        writer = csv.DictWriter(csv_file, fieldnames=headers_partitions)
        writer.writeheader()
        for timeBucket in range(STEP_SIZE, EXPERIMENT_TIMEOUT+1, STEP_SIZE):
            values = {'seconds' : int(timeBucket)}
            for i in range(1, NUMBER_OF_INPUTS+1):
                values['c_avg_' + str(i)] = c_avg[i][timeBucket]
                values['c_ci_' + str(i)] = c_ci[i][timeBucket]
            writer.writerow(values)

    headers_delta = ['seconds']
    for i in range(1, NUMBER_OF_INPUTS+1):
        headers_delta.append('d_avg_' + str(i))
        headers_delta.append('d_ci_' + str(i))
    outputFileName_delta = outputDir + "/_plot_" + subject_name +  "_t=" + str(EXPERIMENT_TIMEOUT) + "_delta.csv"
    print(outputFileName_delta)
    with open(outputFileName_delta, "w") as csv_file:
        writer = csv.DictWriter(csv_file, fieldnames=headers_delta)
        writer.writeheader()
        for timeBucket in range(STEP_SIZE, EXPERIMENT_TIMEOUT+1, STEP_SIZE):
            values = {'seconds' : int(timeBucket)}
            for i in range(1, NUMBER_OF_INPUTS+1):
                values['d_avg_' + str(i)] = d_avg[i][timeBucket]
                values['d_ci_' + str(i)] = d_ci[i][timeBucket]
            writer.writerow(values)


    subject_latex_name = subject_name.replace("_", "\\_")

    colors = ['blue', 'green', 'brown', 'red', 'gray']

    output_plot_latex_partition = outputDir + "/_plot_" + subject_name +  "_t=" + str(EXPERIMENT_TIMEOUT) + "_partitions.tex"
    print(output_plot_latex_partition)
    with open(output_plot_latex_partition, 'w') as tex_file:
        tex_file.write('\\begin{figure}[h]\n')
        tex_file.write('\\begin{tikzpicture}[scale=0.8]\n')
        tex_file.write('\\begin{axis}[\n')
        tex_file.write('   xlabel=time (seconds),\n')
        tex_file.write('   ylabel= $\overline{\#partition}$,\n')
        tex_file.write('   xmajorgrids=true,\n')
        tex_file.write('   ymajorgrids=true,\n')
        tex_file.write('   grid style=dashed,\n')
        tex_file.write('   xmin=0, xmax=' + str(EXPERIMENT_TIMEOUT) + ',\n')
        tex_file.write('   ymin=0,\n')
        tex_file.write('   x label style={at={(axis description cs:0.5,0.0)}},\n')
        tex_file.write('   y label style={at={(axis description cs:0.0,0.5)}},\n')
        tex_file.write('   width = \columnwidth,\n')
        tex_file.write('   legend style={font=\\footnotesize,at={(0.05,0.8)}, anchor=west}\n')
        tex_file.write(']\n')
        tex_file.write('\n')
        for i in range(1, NUMBER_OF_INPUTS+1):
            tex_file.write('\\addplot[color=' + colors[i-1] + ',mark=none, thick] table [y=c_avg_' + str(i) + ', x=seconds, col sep=comma]{_plot_' + subject_name + '_t=' + str(EXPERIMENT_TIMEOUT) + '_partitions.csv};\n')
            tex_file.write('\\addlegendentry{seed-' + str(i) + '}\n')
            tex_file.write('\\errorband[' + colors[i-1] + ', opacity=0.3]{_plot_' + subject_name + '_t=' + str(EXPERIMENT_TIMEOUT) + '_partitions.csv}{seconds}{c_avg_' + str(i) + '}{c_ci_' + str(i) + '}\n')
            tex_file.write('\\addlegendentry{95\% CI}\n')
            tex_file.write('\n')
        tex_file.write('\\end{axis}\n')
        tex_file.write('\\end{tikzpicture}\n')
        tex_file.write('\\caption{\\emph{' + subject_latex_name + '}: number of partitions for ' + str(NUMBER_OF_INPUTS) + ' seed inputs (lines and bands show averages and 95\\% confidence intervals across 30 repetitions).}\n')
        tex_file.write('\\label{plot:' + subject_name + '_partition}\n')
        tex_file.write('\\end{figure}\n')

    output_plot_latex_delta = outputDir + "/_plot_" + subject_name +  "_t=" + str(EXPERIMENT_TIMEOUT) + "_delta.tex"
    print(output_plot_latex_delta)
    with open(output_plot_latex_delta, 'w') as tex_file:
        tex_file.write('\\begin{figure}[h]\n')
        tex_file.write('\\begin{tikzpicture}[scale=0.8]\n')
        tex_file.write('\\begin{axis}[\n')
        tex_file.write('   xlabel=time (seconds),\n')
        tex_file.write('   ylabel= $\overline{\#instructions}$,\n')
        tex_file.write('   xmajorgrids=true,\n')
        tex_file.write('   ymajorgrids=true,\n')
        tex_file.write('   grid style=dashed,\n')
        tex_file.write('   xmin=0, xmax=' + str(EXPERIMENT_TIMEOUT) + ',\n')
        tex_file.write('   ymin=0,\n')
        tex_file.write('   x label style={at={(axis description cs:0.5,0.0)}},\n')
        tex_file.write('   y label style={at={(axis description cs:0.0,0.5)}},\n')
        tex_file.write('   width = \columnwidth,\n')
        tex_file.write('   legend style={font=\\footnotesize,at={(0.05,0.8)}, anchor=west}\n')
        tex_file.write(']\n')
        tex_file.write('\n')
        for i in range(1, NUMBER_OF_INPUTS+1):
            tex_file.write('\\addplot[color=' + colors[i-1] + ',mark=none, thick] table [y=d_avg_' + str(i) + ', x=seconds, col sep=comma]{_plot_' + subject_name + '_t=' + str(EXPERIMENT_TIMEOUT) + '_delta.csv};\n')
            tex_file.write('\\addlegendentry{seed-' + str(i) + '}\n')
            tex_file.write('\\errorband[' + colors[i-1] + ', opacity=0.3]{_plot_' + subject_name + '_t=' + str(EXPERIMENT_TIMEOUT) + '_delta.csv}{seconds}{d_avg_' + str(i) + '}{d_ci_' + str(i) + '}\n')
            tex_file.write('\\addlegendentry{95\% CI}\n')
            tex_file.write('\n')
        tex_file.write('\\end{axis}\n')
        tex_file.write('\\end{tikzpicture}\n')
        tex_file.write('\\caption{\\emph{' + subject_latex_name + '}: max delta development for ' + str(NUMBER_OF_INPUTS) + ' seed inputs (lines and bands show averages and 95\\% confidence intervals across 30 repetitions).}\n')
        tex_file.write('\\label{plot:' + subject_name + '_delta}\n')
        tex_file.write('\\end{figure}\n')
