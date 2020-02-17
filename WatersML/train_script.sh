#!/bin/bash
# Copyright (C) 2020 Pietro Di Gennaro, Domenico Lofù, Vitanio Daniele, Pietro Tedeschi, Pietro Boccadoro

# number of numphy random seed
SEED=1

echo "#!/usr/bin/python3
from numpy import array

# variables
save_model = True
load_model = False

# number of epochs
EPOCHS = 5

# of neurons of the hidden layer
NEURONS = 5

# sets Numphy random seed
SEED = "$SEED"

#train/test set limits
TRAINSET_SIZE = 2460" > config.py


OLDNEURONS=5
OLDEPOCH=5
NEURONS=$OLDNEURONS
EPOCH=$OLDEPOCH

files=(log.txt loss.png cond.png cond.csv oxygen.png oxygen.csv ph.png ph.csv temp.png temp.csv train.png train.csv)

touch config.py

# max number of neurons
MAXNEURONS=100
#max number of epochs
MAXEPOCH=100
# + interval
INTERVAL=5

rm RESULTS
mkdir RESULTS

#itero sull'epoch
while [ $EPOCH -le $MAXEPOCH ]; do
    while [ $NEURONS -le $MAXNEURONS ]; do
        sleep 1
        
        rm config.py
        echo "#!/usr/bin/python3
from numpy import array

# variables
save_model = True
load_model = False

# number of epochs
EPOCHS = "$EPOCH"

# of neurons of the hidden layer
NEURONS = "$NEURONS"

# sets Numphy random seed
SEED = "$SEED"

#train/test set limits
TRAINSET_SIZE = 2460" > config.py

		sleep 1
        python3 train.py 2>&1

        dirname="RESULTS/NR${NEURONS}EPC${EPOCH}"
        mkdir -p -- "$dirname"

        for item in ${files[*]}
        do
            echo $item
            mv $item $dirname
        done

        let OLDNEURONS=NEURONS
        let NEURONS=NEURONS+INTERVAL

    done

    OLDNEURONS=5
    NEURONS=$OLDNEURONS

    let OLDEPOCH=EPOCH
    let EPOCH=EPOCH+INTERVAL
done
