# WaterS 2.0
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-green.svg)](https://www.gnu.org/licenses/gpl-3.0) ![version](https://img.shields.io/badge/version-2.0-brightgreen) 

This repository contains the source code for the WaterS 2.0 project.
WaterS 2.0 is a Water Quality Prediction System based on LSTM NNs for a Sigfox-compliant IoT Device.
The goal is to make predictions of water quality through time series analysis. 
The dataset used for training the model contains a qualitative and quantitative monitoring system of the underground water bodies. It is populated with several water parameters, such as, average temperature, electric conductivity, medium dissolved oxygen, and PH) coming from different station probes.
The code is mainly written with Python 3 and integrates different types of libraries for the back-end computation of the model. There are also several bash scripts, so for a faithful reproduction, a Linux environment is recommended.

<p align="center">
  <img src="/images/NeuralN.png" width="100%" height="100%">
</p>

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
We suggest to use a Linux based distro such as Ubuntu.
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

## Arduino Environment

This work uses the Arduino MKRFOX1200, a development board which contains the [ATMEL SAMD21](http://www.atmel.com/Images/Atmel-42181-SAM-D21_Datasheet.pdf) micro controller. As all Arduino devices, the MKRFOX1200 can be programmed using the Arduino Software (IDE).

Detailed instructions for installation in popular operating systems can be found at:

-  [Linux](https://www.arduino.cc/en/Guide/Linux) (see also the [Arduino playground](https://playground.arduino.cc/Learning/Linux))
-  [macOS](https://www.arduino.cc/en/Guide/MacOSX)
-  [Windows](https://www.arduino.cc/en/Guide/Windows)

The Arduino MKRFOX1200 requires also the installation of additional libraries (for adding the [SigFox](https://www.sigfox.com/en) connectivity to the Arduino platform) to be integrated into the Arduino IDE. All the necessary information are available below:
- [Guide MKRFOX 1200](https://www.arduino.cc/en/Guide/MKRFox1200)

After that, just import the arduino/Waters\_source\_v2.ino file into the Arduino IDE and deploy it to your device!

For more information on how to get started with these kind of product please visit the [Getting Started page](https://www.arduino.cc/en/Guide/HomePage).

<p align="center">
  <img src="/images/Water1.jpg">
</p>
<p align="center"> WaterS System and detailed component part list </p>

License
----

WaterS is released under the GPLv3 <a href="LICENSE">license</a>.

