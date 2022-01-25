#!/bin/sh


# use FIFOs as semaphores and use them to ensure that new processes are spawned as soon as possible and that no more than N processes runs at the same time. But it requires more code.

task(){

   [[ ${#1} < 10 ]] && SUFFIX="0${1}" || SUFFIX="$1"
   echo  "${DESTINATION_TABLE_SPEC_PREFIX}_${SUFFIX}"
   bq cp --force $ORIGIN_TABLE_SPEC "${DESTINATION_TABLE_SPEC_PREFIX}_${SUFFIX}";
}

N=50
(
for table in {1..1000}; do
   ((i=i%N)); ((i++==0)) && wait
   task "${table}" &
done
)

