# WaterS 2.0
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-green.svg)](https://www.gnu.org/licenses/gpl-3.0) ![version](https://img.shields.io/badge/version-2.0-brightgreen) 

This repository contains the source code for the WaterS 2.0 project.
WaterS 2.0 is a Water Quality Prediction System based on LSTM NNs for a Sigfox-compliant IoT Device.
The goal is to make predictions of water quality through time series analysis. 
The dataset used for training the model contains a qualitative and quantitative monitoring system of the underground water bodies. It is populated with several water parameters, such as, average temperature, electric conductivity, medium dissolved oxygen, and PH) coming from different station probes.
The code is mainly written with Python 3 and integrates different types of libraries for the back-end computation of the model. There are also several bash scripts, so for a faithful reproduction, a Linux environment is recommended.

The libraries used by WaterS 2.0 are the following:

| Plugin | README |
| ------ | ------ |
| Keras | [https://keras.io/] |
| Tensorflow | [https://www.tensorflow.org/] |
| sklearn.preprocessing | [https://scikit-learn.org/stable/] |
| NumPy | [https://numpy.org/] |
| Pandas read_csv module | [https://pandas.pydata.org/] |
| Matplotlib | [https://matplotlib.org/] |

# Instructions
We to use a Linux based distro such as Ubuntu.
Use the following commands to install the scripts' dependencies:

```sh
$ sudo apt-get install python3-pip python3-tk
$ pip3 install tensorflow-gpu keras numpy pandas matplotlib sklearn h5py
```

Use the config.py.demo to build a config settings file, setting:
- number of neurons;
- number of epochs;
- Numphy random seed value.

Then start the script:
```sh
$ ./train.py
```
Train data will be created in the same folder of the Python script.

You can also use the automatic train with dynamic setting of neurons and epochs (see the train_script.sh file). In this case, a custom "RESULTS" folder will be created, with the train data according to the dynamic configuration chosen.

License
----

GPLv3