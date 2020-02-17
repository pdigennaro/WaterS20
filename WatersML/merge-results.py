#!/usr/bin/python3

import csv

with open("test.csv", 'w') as myfile:
    wr = csv.writer(myfile, quoting=csv.QUOTE_ALL)
    wr.writerow(["NEURONS", "EPOCHS", "MSE", "MAE", "MAP", "COSINE"])

    for i in range(5,105,5):
        for j in range(5,505,5):
            print("LEGGO FILE: NR"+str(i)+"EPC"+str(j)+"/log.txt")
            file = open("NR"+str(i)+"EPC"+str(j)+"/log.txt", "r")

            lines = str(file.readlines())

            str1 = "Test results:"

            l1 = lines[lines.find("'cosine_proximity']")+25:]
            
            l2 = l1[:l1.find("Test ")]

            l3 = l2.replace("\\n', '", "")

            lista = list(map(float, l3[1:len(l3)-1].split(",")))

            finalista = [i] + [j] + lista[1:]
            print(finalista)

            wr.writerow(finalista)