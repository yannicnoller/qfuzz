## run_maxleak_subjects.sh
# CAUTION
# Run this script within its folder. Otherwise the paths might be wrong!
#####################################
# chmod +x run_maxleak_subjects.sh
# ./run_maxleak_subjects.sh
#

trap "exit" INT

##########################################

echo "Run QFuzz on the MaxLeak subjects..."

number_of_runs=30 # 30 repetitions
time_bound=3600 # 3600 sec = 60 min
step_size_eval=30 # every 30 seconds (for reporting)

#driver="Driver_KDynamic"
driver="Driver_Greedy"

declare -a subjects=(
"rsa_modpow_1717_bitlength_3_count"
"rsa_modpow_1717_bitlength_4_count"
"rsa_modpow_1717_bitlength_5_count"
"rsa_modpow_1717_bitlength_6_count"
"rsa_modpow_1717_bitlength_7_count"
"rsa_modpow_1717_bitlength_8_count"
"rsa_modpow_1717_bitlength_9_count"
"rsa_modpow_1717_bitlength_10_count"
"rsa_modpow_834443_bitlength_3_count"
"rsa_modpow_834443_bitlength_4_count"
"rsa_modpow_834443_bitlength_5_count"
"rsa_modpow_834443_bitlength_6_count"
"rsa_modpow_834443_bitlength_7_count"
"rsa_modpow_834443_bitlength_8_count"
"rsa_modpow_834443_bitlength_9_count"
"rsa_modpow_834443_bitlength_10_count"
"rsa_modpow_1964903306_bitlength_3_count"
"rsa_modpow_1964903306_bitlength_4_count"
"rsa_modpow_1964903306_bitlength_5_count"
"rsa_modpow_1964903306_bitlength_6_count"
"rsa_modpow_1964903306_bitlength_7_count"
"rsa_modpow_1964903306_bitlength_8_count"
"rsa_modpow_1964903306_bitlength_9_count"
"rsa_modpow_1964903306_bitlength_10_count"
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
