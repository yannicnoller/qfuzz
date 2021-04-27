## run_blazer_subjects.sh
# CAUTION
# Run this script within its folder. Otherwise the paths might be wrong!
#####################################
# chmod +x run_blazer_subjects.sh
# ./run_blazer_subjects.sh
#

trap "exit" INT

##########################################

echo "Run QFuzz on the Blazer subjects..."

number_of_runs=30 # 30 repetitions
time_bound=1800 # 1800 sec = 30 min
step_size_eval=30 # every 30 seconds (for reporting)

#driver="Driver_KDynamic"
driver="Driver_Greedy"

declare -a subjects=(
"blazer_array_unsafe"
"blazer_array_safe"
"blazer_loopandbranch_unsafe"
"blazer_loopandbranch_safe"
"blazer_sanity_unsafe"
"blazer_sanity_safe"
"blazer_straightline_unsafe"
"blazer_straightline_safe"
"blazer_unixlogin_unsafe"
"blazer_unixlogin_safe"
"blazer_modpow1_unsafe"
"blazer_modpow1_safe"
"blazer_modpow2_unsafe"
"blazer_modpow2_safe"
"blazer_passwordEq_unsafe"
"blazer_passwordEq_safe"
"blazer_k96_unsafe"
"blazer_k96_safe"
"blazer_gpt14_unsafe"
"blazer_gpt14_safe"
"blazer_login_unsafe"
"blazer_login_safe"
)

# maximum number of partitions
declare -a kValue=(
100
100
20
20
10
10
100
100
2
2
100
100
100
100
100
100
100
100
100
100
100
100
)

##########################################

# Check array sizes
if [[ ${#subjects[@]} != ${#kValue[@]} ]]
then
  echo "[Error in script] the array sizes of subjects and K values do not match!. Abort!"
  exit 1
fi

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
    nohup java -cp "./bin-instr/:./lib/*" edu.cmu.sv.kelinci.Kelinci -K ${kValue[i]} ${driver} @@ > fuzzer-out-$j/server-log.txt &
    server_pid=$!
    sleep 5 # Wait a little bit to ensure that server is started

    # Start modified AFL
    AFL_SKIP_CPUFREQ=1 AFL_I_DONT_CARE_ABOUT_MISSING_CRASHES=1 nohup ../../../tool/afl-2.51b-qfuzz/afl-fuzz -i in_dir -o fuzzer-out-$j -c quantify -K ${kValue[i]} -S afl -t 999999999 ../../../tool/fuzzerside/interface -K ${kValue[i]} @@ > fuzzer-out-$j/afl-log.txt &
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
