#!/usr/bin/python3
# Copyright (C) 2020 Pietro Di Gennaro, Domenico Lofù, Vitanio Daniele, Pietro Tedeschi, Pietro Boccadoro

from numpy import array
from pandas import read_csv
from matplotlib import pyplot
from keras.models import Sequential
from keras.layers import LSTM
from keras.layers import Dense
from keras.models import model_from_json
from keras.utils import plot_model
from sklearn.preprocessing import StandardScaler
import math
from matplotlib import pyplot
import matplotlib.pyplot as plt
import config
import numpy as np
import csv
import sklearn.metrics as skmetrics

# sets random seed
np.random.seed(config.SEED)

# model the timeseries dataset in the the (X, Y) form
def InOutDatasetSplitter(dataset, steps):
	x_output = list()
	y_output = list()
	
	for i in range(len(dataset)):
		count = i + steps
		
		if count > len(dataset)-1:
			break
		
		x, y = dataset[i:count, :], dataset[count, :]
		
		x_output.append(x)
		y_output.append(y)
	
	return array(x_output), array(y_output)

print("**********************************")
print("NEURONS: ", config.NEURONS)
print("EPOCHS: ", config.EPOCHS)
print("**********************************")

with open("log.txt", "a") as f:
	print("NEURONS=", config.NEURONS, file=f)
	print("EPOCHS=", config.EPOCHS, file=f)

dataset = read_csv('dataset.csv', header=0)

# std normalization (with mean and standard deviation)
scaler = StandardScaler()

print("\n\nscaler: ")

# computes the mean and the std for later use
print(scaler.fit(dataset.values))

print(scaler.mean_)
print(scaler.scale_)

# print scaled values
print(scaler.transform(dataset.values))

datasetvalues = scaler.transform(dataset.values)
values = datasetvalues[0:config.TRAINSET_SIZE-1]

# choose a number of time steps
n_steps = 3

# convert into input/output
X_input, y_input = InOutDatasetSplitter(values, n_steps)

# the dataset knows the number of featureS
n_features = X_input.shape[2]

if not config.load_model:
	# define model
	model = Sequential()
	# first LSTM layer
	model.add(LSTM(config.NEURONS, activation='relu', return_sequences=True, input_shape=(n_steps, n_features)))
	# second LSTM layer
	model.add(LSTM(config.NEURONS, activation='relu'))
	# output layer
	model.add(Dense(n_features))
	model.compile(optimizer='adam', loss='mse', metrics=['mse', 'mae', 'cosine'])

	# fit model
	history = model.fit(X_input, y_input, validation_split=0.33, epochs=config.EPOCHS, verbose=0)
	
	# list all data in history
	print(history.history.keys())
	
	plt.plot(history.history['loss'])
	plt.plot(history.history['val_loss'])
	
	plt.title('model loss')
	
	plt.ylabel('loss')
	plt.xlabel('epoch')
	
	plt.legend(['train', 'validation'], loc='upper left')
	
	plt.savefig('loss.png')
	
	plt.close()

	# save train performances in a csv file...
	with open("train.csv", 'w') as myfile:
		wr = csv.writer(myfile, quoting=csv.QUOTE_ALL)
		wr.writerow(["EPOCH", "LOSS", "VAL_LOSS"])

		for iii in range(0, config.EPOCHS):
			linei = [iii] + [history.history['loss'][iii]] + [history.history['val_loss'][iii]]
			wr.writerow(linei)

	# plot metrics
	pyplot.plot(history.history['mse'])
	pyplot.plot(history.history['mae'])
	pyplot.plot(history.history['cosine'])
	
	pyplot.savefig('train.png')
	
	pyplot.close()

	# save training metrics in a csv file
	with open("metrics.csv", 'w') as myfile:
		wr = csv.writer(myfile, quoting=csv.QUOTE_ALL)
		wr.writerow(["EPOCH", "MSE", "MAE", "COSINE"])

		for iii in range(0, config.EPOCHS):
			linei = [iii] + [history.history['mse'][iii]] + [history.history['mae'][iii]] + [history.history['cosine'][iii]]
			wr.writerow(linei)

	plot_model(model, to_file='model_plot.png', show_shapes=True, show_layer_names=True)
else:
	json_file = open("model.json", "r")
	model = json_file.read()
	json_file.close()

	model = model_from_json(model)
	model.load_weights("model.h5")
	model.compile(optimizer='adam', loss='mse', metrics=['mse', 'mae', 'cosine'])

if(config.save_model):
	model_json = model.to_json()

	# serialize model to json
	with open("model.json", "w") as json_file:
		json_file.write(model_json)
	# serialize weights to HDF5
	model.save_weights("model.h5")

# from limit on...
test_values = datasetvalues[config.TRAINSET_SIZE:]

k = 0

out = []
prev = []

# used for the skmetrics
OUTPUT_TRUE = []
PREDICTED_VALUES = []

for val in range(len(test_values)):
	if (k + 3) > (len(test_values) - 1):
		break

	test_input = array([test_values[k], test_values[k+1], test_values[k+2]])
	test_input = test_input.reshape((1, n_steps, n_features))

	output = test_values[k+2]
	previsione = model.predict(test_input, verbose=0)

	# revert normalization
	output = scaler.inverse_transform(output)
	previsione = scaler.inverse_transform(previsione)

	out.append(output)
	prev.append(previsione)

	OUTPUT_TRUE.append(output)
	PREDICTED_VALUES.append(previsione.tolist()[0])

	k += 1

# model evaluate
X_test, y_test = InOutDatasetSplitter(test_values, n_steps)
score = model.evaluate(X_test, y_test, verbose=1)

with open("log.txt", "a") as f:
	print("\nTEST METRICS with normalized data:", file=f)
	print(model.metrics_names, file=f)
	print(score, file=f)

	print("Test loss: ", score[0], file=f)

for c in range(4):
	t1 = []
	t2 = []

	for b in range(0, len(out)):
		t1.append(out[b].item(c))
		t2.append(prev[b].item(c))

	days = range(len(out))

	etic = ["temp", "cond", "oxygen", "ph"]
	print(etic[c])
	pyplot.title(etic[c])
	pyplot.plot(days, t1, 'b')
	pyplot.plot(days, t2, 'r')
	pyplot.legend(['true value', 'predicted value'], loc='upper left')
	pyplot.savefig(etic[c] + '.png')
	pyplot.close()

	with open(etic[c] + ".csv", 'w') as myfile:
		wr = csv.writer(myfile, quoting=csv.QUOTE_ALL)
		wr.writerow(["#", "true_value", "predicted_value"])

		for iii in range(len(out)):
			linei = [iii+1] + [t1[iii]] + [t2[iii]]
			wr.writerow(linei)

with open("log.txt", "a") as f:
	print("\nTEST SET METRICS with not normalized data:", file=f)
	print("mean_squared_error", file=f)
	print(skmetrics.mean_squared_error(OUTPUT_TRUE, PREDICTED_VALUES, squared=False), file=f)
	print("Rmean_squared_error", file=f)
	print(skmetrics.mean_squared_error(OUTPUT_TRUE, PREDICTED_VALUES, squared=True), file=f)
	print("mean_absolute_error", file=f)
	print(skmetrics.mean_absolute_error(OUTPUT_TRUE, PREDICTED_VALUES), file=f)
	print("median_absolute_error", file=f)
	print(skmetrics.median_absolute_error(OUTPUT_TRUE, PREDICTED_VALUES), file=f)
	print("r2_score", file=f)
	print(skmetrics.r2_score(OUTPUT_TRUE, PREDICTED_VALUES), file=f)
	print("explained_variance_score", file=f)
	print(skmetrics.explained_variance_score(OUTPUT_TRUE, PREDICTED_VALUES), file=f)