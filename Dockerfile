FROM ubuntu:18.04
ARG DEBIAN_FRONTEND=noninteractive
ENV TZ=Europe/Rome
RUN apt-get update && apt-get install -y \
  git \
  python3 \
  python3-pip \
  python3-tk \
  graphviz \
  libjpeg8-dev \
  zlib1g-dev
RUN git clone https://github.com/pdigennaro/waters20 /home/waters
RUN cd /home/waters && pip3 install -r reqs.txt

RUN chmod +x /home/waters/WatersML/train.py

RUN echo "#!/usr/bin/python3 \n\
from numpy import array \n\
# variables \n\
save_model = True \n\
load_model = False \n\
# number of epochs \n\
EPOCHS = 40 \n\
# of neurons of the hidden layer \n\
NEURONS = 70 \n\
# sets Numphy random seed \n\
SEED = 1 \n\
#train/test set limits \n\
TRAINSET_SIZE = 2460" > /home/waters/WatersML/config.py

CMD ["/home/waters/WatersML/train.py"]
