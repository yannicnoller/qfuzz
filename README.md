[![DOI](https://zenodo.org/badge/204430941.svg)](https://zenodo.org/badge/latestdoi/204430941)

# QFuzz: Quantitative Fuzzing for Side Channels
This repository provides the tool and the evaluation subjects for the paper "QFuzz: Quantitative Fuzzing for Side Channels" accepted for the technical track at [ISSTA'2021](https://conf.researchr.org/home/issta-2021).
A pre-print of the paper is available [here](https://yannicnoller.github.io/publications/issta2021_noller_qfuzz.pdf).

Authors:
[Yannic Noller](https://yannicnoller.github.io) and
[Saeid Tizpaz-Niari](https://sites.google.com/a/colorado.edu/saeid-tizpaz-niari/).

The repository includes:
* a [Dockerfile](Dockerfile) to build the Docker script,
* a [setup](tool/setup.sh) script for manual building of the tool,
* the source code of *QFuzz*: [tool](./tool),
* the evaluation subjects: [evaluation/subjects](./evaluation/subjects),
* the summarized evaluation results: [evaluation/results](./evaluation/results), and
* the scripts to rerun all experiments: [evaluation/scripts](./evaluation/scripts).




## Docker Image
A pre-built version of *QFuzz* is also available as [Docker image](https://hub.docker.com/r/yannicnoller/qfuzz):
```
docker pull yannicnoller/qfuzz
docker run -it --rm yannicnoller/qfuzz
```

We recommend to use Docker's [volume](https://docs.docker.com/engine/reference/run/#volume-shared-filesystems) feature to connect the docker container to the own file system so that QFuzz's results can be easily accessed.
Furthermore, we recommend to run scripts in an own [screen session](https://linuxize.com/post/how-to-use-linux-screen/#starting-named-session) so that the results can be observed during execution.


## Tool
*QFuzz* is built on top of the fuzzer [DifFuzz](https://github.com/isstac/diffuzz), a differential fuzzer to detect side-channel vulnerabilities.
*QFuzz* extends the capabilities by enabling a quantitative analysis based on min entropy to evaluate the strength of side channels.
Both *QFuzz* and *DifFuzz* use the fuzzing framework of [Kelinci](https://github.com/isstac/kelinci) by Rody Kersten, wich interfaces *AFL* with Java bytecode.

The folder *tool* contains the source code for the fuzzing approach structured in three subfolders:
1. [afl-2.51b-qfuzz](tool/afl-2.51b-qfuzz) the extended version of *AFL*,
2. [fuzzerside](tool/fuzzerside) the interface program to connect to Java bytecode, and
3. [instrumentor](tool/instrumentor) our custom Kelinci version, which includes the code for instrumentation and partitioning

### Requirements
* Git, Ant, Build-Essentials, Gradle
* Java JDK = 1.8
* Python3, Numpy Package
* recommended: Ubuntu 18.04.1 LTS

### How to install QFuzz
(Note: If you use the pre-built [Docker image](#docker-image), the tool is already built and ready to use so that you can skip this section.)

If you want to build the tool manually, we provide a script [`setup.sh`](tool/setup.sh) to simply build everything.
But please read first the explanations below.

The folder [tool](tool) contains 3 subfolders:

* [afl-2.51b-qfuzz](tool/afl-2.51b-qfuzz): Kelinci, and hence also *QFuzz*, is using [AFL](http://lcamtuf.coredump.cx/afl/) as the underlying fuzzing engine. Kelinci leverages a server-client architecture to make *AFL* applicable to Java applications, please refer to the Kelinci [poster-paper](https://dl.acm.org/citation.cfm?id=3138820) for more details. In order to make it easy for the users, we provide our complete modified *AFL* variant in this folder. Note that we only modified the file *afl-fuzz.c*. For our experiments we have used [afl-2.51b](http://lcamtuf.coredump.cx/afl/releases/?O=D). Please build *AFL* by following their instructions. Although the `make` command should be enough.

* [fuzzerside](tool/fuzzerside): This folder includes the *interface* program to connect the *Kelinci server* to the *AFL* fuzzer. Simply use `make` to compile the `interface.c` file. If there is an error, you will have to modify the `Makefile` according to your system setup.

* [instrumentor](tool/instrumentor): This folder includes the *Kelinci server* and the *instrumentor* written in Java. The instrumentor is used to instrument the Java bytecode, which is necessary to add the coverage reporting and other metric collecting for the fuzzing. Additionally, the instrumentor project also includes the source code for our partition algorithms. The Kelinci server handles requests from *AFL* to execute a mutated input on the application. Both are included in the same Gradle project. Therefore, you can simply use `gradle build` to build them.

As already mentioned, we have provided a script to build everything.
Please execute [`setup.sh`](tool/setup.sh) to trigger that.
Note that depending on your execution environment, you may want to modify this script.
We tested our scripts on a Ubuntu Ubuntu 18.04.1 LTS machine.

```
cd tool
./setup.sh
```




## Evaluation

The folder [*evaluation*](evaluation) contains 3 subfolders:
* [results](evaluation/results): the summarized evaluation results and the generated plots,
* [scripts](evaluation/scripts): all scripts necessary to prepare and run the experiments, and
* [subjects](evaluation/subjects): all evaluation subjects presented in our paper.

After having *QFuzz* installed, you can navigate to the folder [evaluation/scripts](evaluation/scripts) and run the script [`prepare_all.sh`](evaluation/scripts/prepare_all.sh) to build and instrument all subjects (it will run ~3 minutes).
We recommend to first ensure that all previous results are cleaned, for which you can use the script [`clean_experiments.sh`](evaluation/scripts/clean_experiments.sh).
Note that you may want to store results from previous runs at a different location.

```
cd evaluation/scripts
./clean_experiments.sh
./prepare_all.sh
```


### Getting Started with an example
After succesfully installed *QFuzz* and prepared the evaluation subjects, you can try a simple example to check the basic functionality of *QFuzz*.
Therefore, we prepared a simple run script for the *Blazer login unsafe* subject.
It represents an *unsafe* password comparison algorithm, which eventually leaks the complete password via a timing side-channel.
You can find the run script here: [`evaluation/scripts/run_example.sh`](evaluation/scripts/run_example.sh).
We have constructed all run scripts in the way that the compartment in the beginning defines the run configurations:
```
...

number_of_runs=1 # 1 repetitions
time_bound=300 # 300 sec = 5 min
step_size_eval=1 # every 1 seconds (for reporting)

#driver="Driver_KDynamic"
driver="Driver_Greedy"

declare -a subjects=(
"blazer_login_unsafe"
)

K=100 # maximum number of partitions

...
```

The variable `number_of_runs` defines how often each experiment is repeated.
While in the complete evaluation we repeated every experiment `n=30` times, for this example, we will just execute it once.
`time_bound` defines the timeout for the experiment, which is here just `t=300` seconds (5 mins); in our experiments it is 30 minutes by default.
`step_size_eval` is only for reporting and defines the time step size `s` for parsing *AFL*'s output.
The variable `driver` can be either set to `Driver_Greedy`or `Driver_KDynamic` depending on which partition algorithm to use (default is the *Greedy* algorithm).
The array `subjects` includes the names of all subjects, in this case only `blazer_login_unsafe`, and finally, the variable `K` denotes the maximum number of partitions.

To run the example, you can leave everything unchanged and just execute the script:
```
cd evaluation/scripts
./run_example.sh
```

During the execution, which will take 5 mins in this example, the following steps will happen in background:
1. the folder `fuzzer-out-1` will be created in `evaluation/subjects/blazer_login_unsafe/`, to store the *AFL* output and the log files (you can check this immediately)
2. the Kelinci server will be started (see the log file `fuzzer-out-1/server-log.txt`), it should start with:
```
Fuzzer runs handler thread started.
Server listening on port 7007
Connection established.
```
(if there was a problem with starting the server, it will be shown here, e.g., when the port 7007 is not available)

3. the custom *AFL* will be started, which is visible by the newly created `blazer_login_unsafe/fuzzer-out-1/afl` folder and `blazer_login_unsafe/fuzzer-out-1/afl-log.txt` log file; `afl` holds *AFL's* internal files that will be later parsed to create the reports; `afl-log.txt` represents *AFL*'s output, which is usually shown in dynamic terminal window (the dashboard of *AFL*), but in a static text form is not really usable. The file can be used to check whether *AFL* is running.

During the analysis time there is not much to observe: one can check the `server-log.txt` to see the output of the analysis driver and in total there should be two processes running in background: one Java process for the Kelinci server, and the afl-fuzz process.
Additionally, one can check the fuzzing queue of *AFL* for newly added input files: `blazer_login_unsafe/fuzzer-out-1/afl/queue`.
The file `blazer_login_unsafe/fuzzer-out-1/afl/path_costs.csv` shows important statistics after the afl-fuzz process is terminated.

After 5 minutes the script will automatically stop all processes, parse *AFL*'s output, and produce the summary: `blazer_login_unsafe/fuzzer-out-results-n=1-t=300-s=1.csv`.
The output on the terminal should be:
```
Run QFuzz on the 'Blazer login unsafe' subjects...
[1/1] Run fuzzing for blazer_login_unsafe, round 1 ..
blazer_login_unsafe/fuzzer-out-results-n=1-t=300-s=1.csv
```

The content of `fuzzer-out-results-n=1-t=300-s=1.csv` should be similar as follows:
```
seconds,p_avg_highscore,p_ci,p_max,d_avg_highscore,d_ci,d_max
1,1.00,0.00,1,0.00,0.00,0.0
2,1.00,0.00,1,0.00,0.00,0.0
3,1.00,0.00,1,0.00,0.00,0.0
...
298,17.00,0.00,17,2.00,0.00,2.0
299,17.00,0.00,17,2.00,0.00,2.0
300,17.00,0.00,17,2.00,0.00,2.0

absolute max #partition: 17
max delta for absolute max #partition: 2.0

time partition>1:
6.00 (+/- 0.00)
partition>1Times=[6]

#partitions=[17]
#partitions(30s)=[7]

deltas=[2.0]
deltas(30s)=[4.0]

time local max #partition: 82.00 (+/- 0.00)
time absolute max #partition: 82.00 (+/- 0.00)
min time absolute max #partition: 82

times_absolute_maximum=[82]
```

After 300 seconds (5 mins), we can see that 17 partitions (see `#partitions=[17]`) have been identified, that matches the length of the secret in this case + one partition of the empty secret.
But note that depedending on the available computational power, 5 minutes might not be enough to identify all 17 partitions in your experiments.
If you observe less than 17 partitions, you can either increase the timeout (`time_bound`) or perform multiple experiments (`number_of_runs`).
In general, be aware that the fuzzing approach uses random mutations and therefore it is necessary to repeat the experiments to get reliable results.
For the paper, we executed each subject for 30 minutes, that is `time_bound=1800` (only exception for the MaxLeak experiments).
We repeated each experiment 30 times (`number_of_runs=30`) and reported the averaged results.



### Complete Evaluation Reproduction
In order to reproduce our evaluation completely, we provide the instructions and scripts for each Table in our paper.

#### Execution Comments
You can stop the scripts by using CTRL-C. Be aware that it is necessary to start the scripts in their folder, otherwise the paths used in the scripts won't match.
After manually stopping a script, you need to make sure that the processes triggered are also stopped.
Feel free to reuse our scripts to build your own execution environment, e.g., to stop the analysis after a certain timebound or run multiple instances of the same analysis repeatedly.



#### Table 1: Jetty + Leak Set + Apache WSS4J (5 seeds)
The *QFuzz* results for the Jetty, Leak Set, and Apache WSS4J subjects (shown in Table 1) can be obtained with the script [`evaluation/scripts/run_jetty_leakset_wss4j.sh`](evaluation/scripts/run_jetty_leakset_wss4j.sh).
The script will execute all subjects, each with 30 minutes timeout, and will repeat each experiment 30 times.
Since there are 60 subjects (already inclusive the 5 seeds, we have separate folders for each seed value), the script will approximately run for 30 * 30 * 60 = 54000 minutes = 900 hours.
The scripts needs to be executed for both partition algorithms: *Greedy* and *KDynamic*.
Therefore, the overall execution time will be 900 hours * 2 = 1800 hours = 75 days.

If you want to shorten the overall analysis time to just get a glimpse on the results, we recommend to:
* pick specific subjects by removing the others from the scripts or add a `#` in the beginning of the line to comment them
* reduce the number of experiment repetitions (we recommend to do at least 5 repetitions each to get reliable results)
* reduce the execution time (we recommend to do at least 10 minutes)

```
cd evaluation/scripts
./run_jetty_leakset_wss4j.sh
```

For each subject, there will be a `<subject>/fuzzer-out-results-n=<chosenNo.Repetitions>-t=<chosenTimeoutInSec>-s=30.csv` file generated that holds the summarized experiment results (already combining the repetitions, **but** not the different seeds).
To obtain the combined results for the different seeds, you need to use the script [`evaluation/scripts/evaluate_seed.py`](evaluation/scripts/evaluate_seed.py).

For example, if you want to combine the results from `Eclipse_jetty_1` with `epsilon=1` and its 5 experiments with different seeds, you first need to execute experiments for: `Eclipse_jetty_1_eps1_1`, `Eclipse_jetty_1_eps1_2`, `Eclipse_jetty_1_eps1_3`, `Eclipse_jetty_1_eps1_4`, and `Eclipse_jetty_1_eps1_5`.

And then, use the python script to combine the experiment for the different seeds:
```
cd evaluation/scripts
python3 evaluate_seed.py ../subjects Eclipse_jetty_1_eps1 5 30 1800 3
```

The parameters `5 30 1800 3` denote:
* `5` seeds (suffixes for the folders _1, _2, ...),
* `30` experiment repetitions,
* `1800` seconds experiment timeout, and
* `3` as step size for reporting.

The resulting `Eclipse_jetty_1_eps1-results-m=5-n=30-t=1800-s=3.csv` file can be used to compare the numbers in Table 1.

To compare the produced results with Table 1, you need to check the following values:
* `p_max` (means the maximum number of partitions over all runs/repetitions): in the `.csv` file this is the first value below the table seqeuence of average values, indicated with `absolute max #partition:`
* `delta_max` (means the delta value corresponding to `p_max`): in the `.csv` file this is the value below `d_max`, indicated with `max delta for absolute max #partition:`
* `Time(s) QFuzz, p>1` (means the average time when *QFuzz* finds more than one partition): in the `.csv` file this is indicated with `time partition>1:` and is located below the `delta_max` value
* `Time(s) QFuzz, p_max` (means the average time to reach p_max): in the `.csv` file this is indicated with `time absolute max #cluster:`
* `Time(s) QFuzz, t_{pmax}^{min}` (means the minimum time needed to reach p_max): in the `.csv` file this is indicated with `min time absolute max #cluster:`



#### Figure 2: Computation Time - Greedy vs KDynamic
The left part of Figure 2 shows a boxplot comparing the computation times of *Greedy* and *KDynamic* partitioning.
We conducted this experiment using all generated inputs files from the *Apache WSS4J* subjects.
Therefore, in order to re-produce the boxplot, you first need to re-run these experiments (see previous [Section](#table-1-jetty-leak-set-apache-wssf4-5-seeds)).

Assuming you have somewhere the experiment results in the sub-folders:
```
apache_wss4j_1
apache_wss4j_2
apache_wss4j_3
apache_wss4j_4
apache_wss4j_5
```

We prepared a simple Java program to run only the clustering: [`evaluation/subjects/apache_wss4j/src/ClusterComparison.java`](evaluation/subjects/apache_wss4j/src/ClusterComparison.java).
The corresponding bytecode should have been already prepared with the initial preparation of all subjects.
Please nagivate to the folder [`evaluation/subjects/apache_wss4j`](evaluation/subjects/apache_wss4j) and run this Java class.
```
cd evaluation/subjects/apache_wss4j
java -cp "bin-instr:lib/*" ClusterComparison <absolute-path-to-subjects-folders> > output.txt
```
The parameter `<absolute-path-to-subjects-folders>` means the absolute path to the parent folder of `apache_wss4j_1`, `apache_wss4j_2`, ... .
For example, in the docker, the parameter should be `/root/qfuzz/evaluation/subjects`.

The Java program will produce the terminal output relayed to the file `output.txt`.
There you can find the timing information on the bottom of the file. In our case:
```
...

#files=4779
rep=1

timeGreedy=2.1807005E7
timeKDynamic=1.0768523E8

avgTimeGreedy=4563.089558485039
avgTimeKDynamic=22533.004812722327

executionTimeGreedyList=[...]
executionTimeKDynamicList=[...]
```

To get the statistical information about the boxplot diagram, we prepared the python script: [`evaluation/scripts/generate_boxplot_info.py`](evaluation/scripts/generate_boxplot_info.py).
```
cd evaluation/scripts
python3 generate_boxplot_info.py
```
In case you want to use the timing information from your experiments, copy the *executionTimeGreedyList* to the `greedy` variable (line 45 of `generate_boxplot_info.py`) and *executionTimeKDynamicList* to the `dynamic` variable (line 47 of `generate_boxplot_info.py`).
 
The script `generate_boxplot_info.py` will print the boxplot information on the terminal.

```
...
Greedy:
lower_whisker=397.0
lower_quartile=728.0
median=1053.0
upper_quartile=1727.0
upper_whisker=3225.0

KDynamic:
lower_whisker=1263.0
lower_quartile=2110.0
median=2704.0
upper_quartile=4080.0
upper_whisker=7021.0
```

To generate the actual plot, we provide a LaTeX template [`evaluation/scripts/plots_boxplot.tex`](evaluation/scripts/plots_boxplot.tex).
Please transfer the obtained information from the python script to the LaTeX script.
The pre-filled information represent the values from our experiments.
Then, you can simply build the `.pdf` file with:
```
cd evaluation/scripts
pdflatex plots_boxplot.tex
```

The resulting plot can be examined in the generated `plots_boxplot.pdf` file.
It should match the plot on the left of Figure 2.
Our generated pdf can be found here: [plots_boxplot.pdf](evaluation/results/plots_boxplot.pdf)




#### Figure 2: Plots - Eclipse Jetty 1 Greedy
The plot in the middle of Figure 2 shows the plots for the temporal development of the *Greedy* partitioning with 5 different seed inputs for *Eclipse Jetty 1 (epsilon=1)*.

Assuming that you already executed the experiments for *Eclipse Jetty 1 (epsilon=1)* for *Greedy*, and you have stored the folders:
```
Eclipse_jetty_1_eps1_Greedy_1
Eclipse_jetty_1_eps1_Greedy_2
Eclipse_jetty_1_eps1_Greedy_3
Eclipse_jetty_1_eps1_Greedy_4
Eclipse_jetty_1_eps1_Greedy_5
```
Note: we recommend storing these folders in a separte location from where the experiments have been executed.
By duplicating the folders and changing their names, you can make sure that the original experiments are still available, in case you mistakenly override some of the files in the following process.

To generate the plot, you first need to generate the combined summaries for each partitioning algorithm:
```
cd evaluation/scripts/
python3 evaluate_plain.py <path-to-experiments-results>/Eclipse_jetty_1_eps1_Greedy_1/fuzzer-out- 30 1800 3
python3 evaluate_plain.py <path-to-experiments-results>/Eclipse_jetty_1_eps1_Greedy_2/fuzzer-out- 30 1800 3
python3 evaluate_plain.py <path-to-experiments-results>/Eclipse_jetty_1_eps1_Greedy_3/fuzzer-out- 30 1800 3
python3 evaluate_plain.py <path-to-experiments-results>/Eclipse_jetty_1_eps1_Greedy_4/fuzzer-out- 30 1800 3
python3 evaluate_plain.py <path-to-experiments-results>/Eclipse_jetty_1_eps1_Greedy_5/fuzzer-out- 30 1800 3
```
The `*_plain.py` means that it only will generate the table with the temporal development without reporting the additional information on the bottom of the `.csv` file.

The above commands should generate 5 files:
```
[..]/Eclipse_jetty_1_eps1_Greedy_1/fuzzer-out-results-n=30-t=1800-s=3.csv
[..]/Eclipse_jetty_1_eps1_Greedy_2/fuzzer-out-results-n=30-t=1800-s=3.csv
[..]/Eclipse_jetty_1_eps1_Greedy_3/fuzzer-out-results-n=30-t=1800-s=3.csv
[..]/Eclipse_jetty_1_eps1_Greedy_4/fuzzer-out-results-n=30-t=1800-s=3.csv
[..]/Eclipse_jetty_1_eps1_Greedy_5/fuzzer-out-results-n=30-t=1800-s=3.csv
```
Note that previous `fuzzer-out-` results files might be overriden.

Afterward, you can generate the LaTeX code for the plots, for which we provide the script [`evaluation/scripts/generate_plot.py`](evaluation/scripts/generate_plot.py).

```
cd evaluation/scripts/
python3 generate_plot.py <path-to-experiments-results>/Eclipse_jetty_1_eps1_Greedy 5 30 1800 3 <output-dir-latex-files>
```

The above commands should generate four files at the specified location:
```
_plot_Eclipse_jetty_1_eps1_Greedy_t=1800_partitions.csv
_plot_Eclipse_jetty_1_eps1_Greedy_t=1800_delta.csv
_plot_Eclipse_jetty_1_eps1_Greedy_t=1800_partitions.tex
_plot_Eclipse_jetty_1_eps1_Greedy_t=1800_delta.tex
```

The `.csv` files contain the data, the `.tex` files contain the corresponding LaTeX code for the Figures.
Since the plot in the paper only shows the data for the number of partitions, you can ignore the `*_delta.*` files.

To generate the actual plot, we provide a LaTeX template [`evaluation/scripts/plots_5seeds_greedy.tex`](evaluation/scripts/plots_5seeds_greedy.tex).
The easiest would be just to copy this file to the location of the generated `.tex` and `.csv` file.
Otherwise you can also modify the path in the `plots_5seeds_greedy.tex`.

Assuming that you have copied the `plots_5seeds_greedy.tex` the location of the generated `.tex` and `.csv` file, you generate the `.pdf` file with the plot with the following command:
```
pdflatex plots_5seeds_greedy.tex
```

The resulting plot can be examined in the generated `plots_5seeds_greedy.pdf` file.
It should match the plot in the middle of Figure 2.
Note that in the paper, we show a smaller variant of it, so the axis scaling can be different.
Our generated PDF can be found here: [evaluation/results/plots_5seeds_greedy.pdf](evaluation/results/plots_5seeds_greedy.pdf)





#### Figure 2: Plots - Eclipse Jetty 1: KDynamic vs. Greedy
The plot on the right of Figure 2 shows the plots for the temporal development of the *Greedy* and *KDynamic* partitioning with 5 different seed inputs combined for *Eclipse Jetty 1 (epsilon=1)*.

Assuming that you already executed the experiments for *Eclipse Jetty 1 (epsilon=1)* for *Greedy* and *KDynamic*, and you have stored the folders:
```
Eclipse_jetty_1_eps1_Greedy_1   Eclipse_jetty_1_eps1_KDynamic_1
Eclipse_jetty_1_eps1_Greedy_2   Eclipse_jetty_1_eps1_KDynamic_2
Eclipse_jetty_1_eps1_Greedy_3   Eclipse_jetty_1_eps1_KDynamic_3
Eclipse_jetty_1_eps1_Greedy_4   Eclipse_jetty_1_eps1_KDynamic_4
Eclipse_jetty_1_eps1_Greedy_5   Eclipse_jetty_1_eps1_KDynamic_5
```

To generate the plot, you first need to generate the combined summaries for each partitioning algorithm:
```
cd evaluation/scripts/
python3 evaluate_seed_plain.py <path-to-experiments-results> Eclipse_jetty_1_eps1_Greedy 5 30 1800 3
python3 evaluate_seed_plain.py <path-to-experiments-results> Eclipse_jetty_1_eps1_KDynamic 5 30 1800 3
```
The `*_plain.py` means that it only will generate the table with the temporal development without reporting the additional information on the bottom of the `.csv` file.

The above commands should generate two files:
```
Eclipse_jetty_1_eps1_Greedy-results-m=5-n=30-t=1800-s=3.csv
Eclipse_jetty_1_eps1_KDynamic-results-m=5-n=30-t=1800-s=3.csv
```

Afterward, you can generate the LaTeX code for the plots, for which we provide the script [`evaluation/scripts/generate_plot_seed.py`](evaluation/scripts/generate_plot_seed.py).

```
cd evaluation/scripts/
python3 generate_plot_seed.py <path-to-experiments-results> Eclipse_jetty_1_eps1 5 30 1800 3 <output-dir-latex-files>
```

The above commands should generate four files at the specified location:
```
_plot_Eclipse_jetty_1_eps1_t=1800_delta.csv
_plot_Eclipse_jetty_1_eps1_t=1800_delta.tex
_plot_Eclipse_jetty_1_eps1_t=1800_partitions.csv
_plot_Eclipse_jetty_1_eps1_t=1800_partitions.tex
```

The `.csv` files contain the data, the `.tex` files contain the corresponding LaTeX code for the Figures.
Since the plot in the paper only shows the data for the number of partitions, you can ignore the `*_delta.*` files.

To generate the actual plot, we provide a LaTeX template `evaluation/scripts/plots_greedy_kdynamic.tex`.
The easiest would be just to copy this file to the location of the generated `.tex` and `.csv` file.
Otherwise you can also modify the path in the `plots_greedy_kdynamic.tex`.

Assuming that you have copied the `plots_greedy_kdynamic.tex` the location of the generated `.tex` and `.csv` file, you generate the `.pdf` file with the plot with the following command:
```
pdflatex plots_greedy_kdynamic.tex
```

The resulting plot can be examined in the generated `plots_greedy_kdynamic.pdf` file.
It should match the plot on the right of Figure 2.
Note that in the paper, we show a smaller variant of it, so the axis scaling can be different.
Our generated PDF can be found here: [evaluation/results/plots_greedy_kdynamic.pdf](evaluation/results/plots_greedy_kdynamic.pdf)




#### Table 2: Blazer experiments
The *QFuzz* results for the *Blazer* subjects (shown in Table 2) can be obtained with the script [`evaluation/scripts/run_blazer_subjects.sh`](evaluation/scripts/run_blazer_subjects.sh).
For some of the *Blazer* subjects we had to deviate from the default `K` value (see explanation in our paper), therefore, there is an array in the script defining K values specifically.
For the other experiments this is not present as they simply use `K=100`.
The script will execute all *Blazer* subjects, each with 30 minutes timeout, and will repeat each experiment 30 times.
Since there are 22 subjects, the script will approximately run for 30 * 30 * 22 = 19800 minutes = 330 hours ~ 13.8 days.
If you want to shorten the overall analysis time to just get a glimpse on the results, we recommend to:
* pick specific subjects by removing the others from the scripts or add a `#` in the beginning of the line to comment them
* reduce the number of experiment repetitions (we recommend to do at least 5 repetitions each to get reliable results)
* reduce the execution time (we recommend to do at least 10 minutes)

```
cd evaluation/scripts
./run_blazer_subjects.sh
```

For each subject, there will be a `<subject>/fuzzer-out-results-n=<chosenNo.Repetitions>-t=<chosenTimeoutInSec>-s=30.csv` file generated that holds the summarized experiment results (already combining the repetitions).
To compare the produced results with Table 2, you need to check the following values:
* `p_max` (means the maximum number of partitions over all runs/repetitions): in the `.csv` file this is the first value below the table seqeuence of average values, indicated with `absolute max #partition:`
* `delta_max` (means the delta value corresponding to `p_max`): in the `.csv` file this is the value below `p_max`, indicated with `max delta for absolute max #partition:`
* `Time(s) QFuzz, p>1` (means the average time when *QFuzz* finds more than one partition): in the `.csv` file this is indicated with `time partition>1:` and is located below the `delta_max` value

To check the numbers for *DifFuzz*, *Blazer*, and *Themis* please refer to their publication and/or artifacts. Our artifact does only reproduce the results for *QFuzz*.




#### Table 3: Themis experiments
The *QFuzz* results for the *Themis* subjects (shown in Table 3) can be obtained with the script [`evaluation/scripts/run_themis_subjects.sh`](evaluation/scripts/run_themis_subjects.sh).
The script will execute all *Themis* subjects, each with 30 minutes timeout, and will repeat each experiment 30 times.
Since there are 23 subjects, the script will approximately run for 30 * 30 * 23 = 20700 minutes = 345 hours ~ 14.4 days.
If you want to shorten the overall analysis time to just get a glimpse on the results, we recommend to the same strategy is for the [Blazer experiments](#table-2-blazer-experiments).

Note that the subjects *themis_pac4j_** and *themis_tomcat_** require a running database connection.
In our experiments we used *h2* databases.
Please follow their [instructions](https://www.h2database.com/html/main.html) for setup and maybe consider our helping [notes](evaluation/subjects/database_resources/database_notes.txt).
Our fuzzing drivers for the *themis_pac4j_** subjects assume a connection with `url="jdbc:h2:~/pac4j-fuzz"`, `user="sa"` and `password=""`.
Furthermore they assume an existing table `users (id INT, username varchar(255), password varchar(255))`.
Our fuzzing drivers for the *themis_tomcat_** subjects assume a connection with `url="jdbc:h2:~/tomcat"`, `user="sa"` `password=""`, and an existing table `users (user_name varchar(255), user_pass varchar(255))`.
Please make sure that your environment matches these requirements or adjust the drivers or subjects.

```
cd evaluation/scripts
./run_themis_subjects.sh
```

For each subject, there will be a `<subject>/fuzzer-out-results-n=<chosenNo.Repetitions>-t=<chosenTimeoutInSec>-s=30.csv` file generated that holds the summarized experiment results (already combining the repetitions).
To compare the produced results with Table 3, you can follow the same steps as for [Table 2](#table-2-blazer-experiments).



#### Table 4: Additional DifFuzz experiments
The *QFuzz* results for the *DifFuzz* subjects (shown in Table 4) can be obtained with the script [`evaluation/scripts/run_diffuzz_subjects.sh`](evaluation/scripts/run_diffuzz_subjects.sh).
The script will execute all *DifFuzz* subjects, each with 30 minutes timeout, and will repeat each experiment 30 times.
Since there are 23 subjects, the script will approximately run for 30 * 30 * 13 = 11700 minutes = 195 hours ~ 8.1 days.
If you want to shorten the overall analysis time to just get a glimpse on the results, we recommend to the same strategy is for the [Blazer experiments](#table-2-blazer-experiments).

```
cd evaluation/scripts
./run_diffuzz_subjects.sh
```

For each subject, there will be a `<subject>/fuzzer-out-results-n=<chosenNo.Repetitions>-t=<chosenTimeoutInSec>-s=30.csv` file generated that holds the summarized experiment results (already combining the repetitions).
To compare the produced results with Table 4, you can follow the same steps as for [Table 2](#table-2-blazer-experiments).




#### Table 5: MaxLeak experiments
The *QFuzz* results for the *MaxLeak* subjects (shown in Table 5) can be obtained with the script [`evaluation/scripts/run_maxleak_subjects.sh`](evaluation/scripts/run_maxleak_subjects.sh).
The script will execute all presented *MaxLeak* subjects, each with 60 minutes timeout, and will repeat each experiment 30 times.
Since there are 24 subjects, the script will approximately run for 60 * 30 * 24 = 43200 minutes = 720 hours = 30 days.
If you want to shorten the overall analysis time to just get a glimpse on the results, we recommend to:
* pick specific subjects by removing the others from the scripts or add a `#` in the beginning of the line to comment them
* reduce the number of experiment repetitions (we recommend to do at least 5 repetitions each to get reliable results)
* reduce the execution time (we recommend to do at least 30 minutes)

```
cd evaluation/scripts
./run_maxleak_subjects.sh
```

For each subject, there will be a `<subject>/fuzzer-out-results-n=<chosenNo.Repetitions>-t=<chosenTimeoutInSec>-s=30.csv` file generated that holds the summarized experiment results (already combining the repetitions).
To compare the produced results with Table 5, you can need to check the following values:
* `p_max` (means the maximum number of partitions over all runs/repetitions): in the `.csv` file this is the first value below the table seqeuence of average values, indicated with `absolute max #partition:`
* `delta_max` (means the delta value corresponding to `p_max`): in the `.csv` file this is the value below `p_max`, indicated with `max delta for absolute max #partition:`
* `Time(s) QFuzz, p>1` (means the average time when *QFuzz* finds more than one partition): in the `.csv` file this is indicated with `time partition>1:` and is located below the `delta_max` value
* `t_min` (the minimum time needed to achieve `p_max`): in the `.csv` file this is indicated with `min time absolute max #partition:`

To check the numbers for *MaxLeak* please refer to their publication and/or artifacts.
Our artifact does only reproduce the results for *QFuzz*.




## General Instructions: How to apply QFuzz on new subjects
If you want to apply *QFuzz* on new subjects, we recommend to have a look at the existing subjects and the provided scripts for the subject preparation and experiment execution.
For each subject you will find the following:
* a `src` folder that holds the application code and the drivers (aka fuzzing harness) to guide the analysis
* a `in_dir` folder that holds the seed inputs for the fuzzing campaign

In general, you will have to follow six steps in order to apply *QFuzz* for the quantification of side channels:

1. **Write the fuzzing driver**: Please check our evaluation subjects for some examples.

2. **Provide an initial fuzzing input**: The initial fuzzing input should be a file that does not crash the application. You can also provide multiple files.

3. **Instrument the bytecode**: Assuming that your bytecode is in the `bin` folder, the command for instrumentation could look like: `java -cp [..]/tool/instrumentor/build/libs/kelinci.jar edu.cmu.sv.kelinci.instrumentor.Instrumentor -mode LABELS -i ./bin/ -o ./bin-instr -skipmain`

4. **Starting the Kelinci server**: Assuming that the fuzzing driver class is called `Driver`, the command for starting the Kelinci server could look like: `java -cp bin-instr edu.cmu.sv.kelinci.Kelinci -K 100 Driver @@`

5. **Start fuzzing by starting the modified AFL**: Assuming that you have installed *AFL* correctly, the command for starting *AFL* could be like this: `afl-fuzz -i in_dir -o fuzzer-out -c quantify -K 100 -S afl -t 999999999 [..]/tool/fuzzerside/interface -K 100 @@`. Depending on your execution environment, you might want to add flags like: `AFL_I_DONT_CARE_ABOUT_MISSING_CRASHES=1` or `AFL_SKIP_CPUFREQ=1`. The timeout parameter `-t` is set to a high value, just because that we want to kill *AFL* process ourself.

6. **Stop fuzzing and check results**: After running *AFL* for a couple of minutes (the exact time depends on your execution budget, we used 30 minutes in our evaluation), you can kill the *AFL* process, as well as stopping the Kelinci server process. Please have a look at the file `[..]/fuzzer-out/afl/path_cost.csv`. It includes a list of mutated input that were considered *interesting*, i.e. increased overall coverage or improved the the number of partitions and delta values. You want to check the last file that is labeled with `+partition`. The following commmand might be helpful: `cat [..]/fuzzer-out/afl/path_cost.csv | grep +partition`. The last file labeled as `+partition` provides the maximum cost difference observed during the fuzzing run. The column `#Partitions` shows the observed number of partitions.

**Note**:
between step 4 and 5 you might want to test that the Kelinci server is running correctly, by executing the initial input with the interface program.
Assuming that the initial input file is located in the folder `in_dir` and is called `example`, the command could look like this: `[..]/tool/fuzzerside/interface -K 100 in_dir/example`.

**Recommendation**:
In order to run and at the same time check the current results, we think it is comfortable to open a terminal with three windows: (1) for the Kelinci server, (2) for the modified *AFL*, and (3) to regularly check the latest `+partition` `path_cost.csv` file.
During the process you may want to check window (1) for any unexpected exceptions. Make sure that the server runs and produces messages.
You mostly want to check window (2), which shows the *AFL* status screen (check their [description](http://lcamtuf.coredump.cx/afl/status_screen.txt) for more details).




## Developers
* **Yannic Noller** (yannic.noller at acm.org)
* **Saeid Tizpaz-Niari** (saeid at utep.edu)




## License
This project is licensed under the Apache-2.0 License - see the [LICENSE](LICENSE) file for details
