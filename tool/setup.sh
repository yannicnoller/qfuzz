## setup.sh
#####################################
# chmod +x setup.sh
# ./setup.sh
#

trap "exit" INT

# Build custom AFL.
echo "Build custom AFL.."
cd ./afl-2.51b-qfuzz/
make clean
make -s
cd ..
echo ""

# Build interface program.
echo "Build interface program.."
cd ./fuzzerside
make clean
make -s
cd ..
echo ""

# Build Kelinci server and instrumentor.
echo "Build Kelinci and instrumentor"
cd ./instrumentor
gradle clean
gradle build
cd ..
echo ""

echo ">> Finished building QFuzz components."
