## run_jetty_leakset_wss4j.sh
# CAUTION
# Run this script within its folder. Otherwise the paths might be wrong!
#####################################
# chmod +x run_jetty_leakset_wss4j.sh
# ./run_jetty_leakset_wss4j.sh
#

trap "exit" INT

##########################################

echo "Run QFuzz on the Themis subjects..."

number_of_runs=30 # 30 repetitions
time_bound=1800 # 1800 sec = 30 min
step_size_eval=30 # every 30 seconds (for reporting)

#driver="Driver_KDynamic"
driver="Driver_Greedy"

declare -a subjects=(
"Eclipse_jetty_1_eps1_1"
"Eclipse_jetty_1_eps1_2"
"Eclipse_jetty_1_eps1_3"
"Eclipse_jetty_1_eps1_4"
"Eclipse_jetty_1_eps1_5"
"Eclipse_jetty_1_eps4_1"
"Eclipse_jetty_1_eps4_2"
"Eclipse_jetty_1_eps4_3"
"Eclipse_jetty_1_eps4_4"
"Eclipse_jetty_1_eps4_5"
"Eclipse_jetty_4_eps1_1"
"Eclipse_jetty_4_eps1_2"
"Eclipse_jetty_4_eps1_3"
"Eclipse_jetty_4_eps1_4"
"Eclipse_jetty_4_eps1_5"
"Eclipse_jetty_4_eps4_1"
"Eclipse_jetty_4_eps4_2"
"Eclipse_jetty_4_eps4_3"
"Eclipse_jetty_4_eps4_4"
"Eclipse_jetty_4_eps4_5"
"Eclipse_jetty_5_eps1_1"
"Eclipse_jetty_5_eps1_2"
"Eclipse_jetty_5_eps1_3"
"Eclipse_jetty_5_eps1_4"
"Eclipse_jetty_5_eps1_5"
"Eclipse_jetty_5_eps4_1"
"Eclipse_jetty_5_eps4_2"
"Eclipse_jetty_5_eps4_3"
"Eclipse_jetty_5_eps4_4"
"Eclipse_jetty_5_eps4_5"
"leaksn1b-1_1"
"leaksn1b-1_2"
"leaksn1b-1_3"
"leaksn1b-1_4"
"leaksn1b-1_5"
"leaksn1b-2_1"
"leaksn1b-2_2"
"leaksn1b-2_3"
"leaksn1b-2_4"
"leaksn1b-2_5"
"leaksn1b-3_1"
"leaksn1b-3_2"
"leaksn1b-3_3"
"leaksn1b-3_4"
"leaksn1b-3_5"
"leaksn1b-4_1"
"leaksn1b-4_2"
"leaksn1b-4_3"
"leaksn1b-4_4"
"leaksn1b-4_5"
"leaksn1b-5_1"
"leaksn1b-5_2"
"leaksn1b-5_3"
"leaksn1b-5_4"
"leaksn1b-5_5"
"apache_wss4j_1"
"apache_wss4j_2"
"apache_wss4j_3"
"apache_wss4j_4"
"apache_wss4j_5"
)

K=100 # maximum number of partitions

##########################################

run_counter=0
total_number_subjects=${#subjects[@]}
total_number_experiments=$(( $total_number_subjects * $number_of_runs ))

cd ../subjects

# Run QFuzz
for (( i=0; i<=$(( $total_number_subjects - 1 )); i++ ))
do
  cd ./${subjects[i]}/
  for j in `seq 1 $number_of_runs`
  do
    run_counter=$(( $run_counter + 1 ))
    echo "[$run_counter/$total_number_experiments] Run fuzzing for ${subjects[i]}, round $j .."

    mkdir fuzzer-out-$j/

    # Start Kelinci server
    nohup java -cp "./bin-instr/:./lib/*" edu.cmu.sv.kelinci.Kelinci -K ${K} ${driver} @@ > fuzzer-out-$j/server-log.txt &
    server_pid=$!
    sleep 5 # Wait a little bit to ensure that server is started

    # Start modified AFL
    AFL_SKIP_CPUFREQ=1 AFL_I_DONT_CARE_ABOUT_MISSING_CRASHES=1 nohup ../../../tool/afl-2.51b-qfuzz/afl-fuzz -i in_dir -o fuzzer-out-$j -c quantify -K ${K} -S afl -t 999999999 ../../../tool/fuzzerside/interface -K ${K} @@ > fuzzer-out-$j/afl-log.txt &
    afl_pid=$!

    # Wait for timebound
    sleep $time_bound

    # Stop AFL and Kelinci server
    kill $afl_pid
    kill $server_pid

    # Wait a little bit to make sure that processes are killed
    sleep 10
  done
  cd ../

  # Evaluate run
  python3 ../scripts/evaluate.py ${subjects[i]}/fuzzer-out- $number_of_runs $time_bound $step_size_eval

done
