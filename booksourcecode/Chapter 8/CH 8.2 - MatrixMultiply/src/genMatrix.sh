#!/bin/bash
if [ $# -ne 3 ]
then
  echo "there must be 3 arguments to generate the two matries file!"
  exit 1
fi
cat /dev/null > M_$1_$2
cat /dev/null > N_$2_$3
for i in `seq 1 $1`
do
	for j in `seq 1 $2`
	do
		s=$((RANDOM%100))
		echo –e "$i,$j\t$s" >>M_$1_$2
	done
done
echo "we have built the matrix file M"

for i in `seq 1 $2`
	do
	for j in ` seq 1 $3`
	do
		s=$((RANDOM%100))
		echo -e "$i,$j\t$s" >>N_$2_$3 
	done
done
echo "we have built the matrix file N"