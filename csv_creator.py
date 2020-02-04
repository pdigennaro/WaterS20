#!/usr/bin/python3
# Copyright (C) 2020 Pietro Di Gennaro, Domenico Lofù, Vitanio Daniele, Pietro Tedeschi, Pietro Boccadoro

import csv
import os

def select_year():
	answer = True
	vyear = []
	print ("Select year")

	while answer:
		print ("""
			1. 2008
			2. 2009
			3. 2010
			4. 2011
			5. 2012
			""")
		answer=input("Number of year:") 
		if answer=='1': 
			vyear = ['2008']
		elif answer=='2':
			vyear = ['2009']
		elif answer=='3':
			vyear = ['2010']
		elif answer =="4":
			vyear = ['2011']
		elif answer =="5":
			vyear = ['2012']
		elif answer =="6":
			vyear = ['2008','2009','2010','2011','2012']
		return vyear

def select_hours():
	answer = True
	vhour = []
	print ("Select day hours")

	while answer:
		print ("""
			1. 09am, 15pm, 21pm
			2. All day hours
			3. custom
			""")
		answer=input("Option? (number)") 
		if answer=='1': 
			vhour = [9, 15, 21]
		elif answer=='2':
			for i in range(24):
				vhour.append(i)
		elif answer =="3":
			value = int(input("Insert the number of hours that you want to select (less or equal to 24): "))
			print("Hours to select: ")
			for i in range(0, value): 
				elem = int(input())
				if elem > 24 or elem < 0:
					print("Wrong value, will not be used!")
				else:
					vhour.append(elem) 
		return vhour
	

def select_city(vyear):	
	print ("Select a city")
	answer = True
	while answer:
		print ("""
			1. BARI
			2. BARLETTA
			3. POLIGNANO A MARE
			4. TRANI
			5. BITETTO
			6. ADELFIA
			7. BRINDISI
			8. CEGLIE MESSAPICA
			9. APRICENA
			10. NARDò
			11. OTRANTO
			12. LECCE
			13. SCORRANO
			14. VERNOLE
			15. TARANTO
			16. CASAMASSIMA
			17. RUTIGLIANO
			18. CONVERSANO
			19. PUTIGNANO
			20. FASANO
			21. FOGGIA
			22. CERIGNOLA
			23. MANFREDONIA
			24. SUPERSANO
			25. CORIGLIANO D'OTRANTO
			26. MANDURIA
			27. GALATINA
			28. CAPRARICA DI LECCE
			""")
		answer=input("# of city:") 
		if answer == "":
			print("\nNo city selected! Try again!")
			break
		return answer

def create_csv(pcity, ncity, path, vhour, year):
	with open(pcity) as csv_in, open('OUTPUT_'+ year[0] + '_' + ncity, "w+") as csv_out:
		csv_reader = csv.reader(csv_in, delimiter=';')
		csv_writer = csv.writer(csv_out)
		bool_check = False
		line_count = 0
		for row in csv_reader:
			if line_count == 0:
				csv_writer.writerow(row)
				line_count += 1
			else:
				for hour in vhour:
					if hour < 10:
						hour = "0"+str(hour)

					if row[3] == str(hour):
						bool_check = True
						csv_writer.writerow(row)
				line_count += 1
	if bool_check:
		print("Process completed!")
	else:
		print("Error!")
	return

def start_process(val, vyear, vhour):
	if val == '1':
		city = "BARI.csv"
	elif val == '2':
		city = "BARLETTA.csv"
	elif val == '3':
		city = "POLIGNANO A MARE.csv"
	elif val == '4':
		city = "TRANI.csv"
	elif val == '5':
		city = "BITETTO.csv"
	elif val == '6':
		city = "ADELFIA.csv"
	elif val == '7':
		city = "BRINDISI.csv"
	elif val == '8':
		city = "CEGLIE MESSAPICA.csv"
	elif val == '9':
		city = "APRICENA.csv"
	elif val == '10':
		city = "NARDò.csv"
	elif val == '10':
		city = "OTRANTO.csv"
	elif val == '12':
		city = "LECCE.csv"
	elif val == '13':
		city = "SCORRANO.csv"
	elif val == '14':
		city = "VERNOLE.csv"
	elif val == '15':
		city = "TARANTO.csv"
	elif val == '16':
		city = "CASAMASSIMA.csv"
	elif val == '17':
		city = "RUTIGLIANO.csv"
	elif val == '18':
		city = "CONVERSANO.csv"
	elif val == '19':
		city = "PUTIGNANO.csv"
	elif val == '20':
		city = "FASANO.csv"
	elif val == '21':
		city = "FOGGIA.csv"
	elif val == '22':
		city = "CERIGNOLA.csv"
	elif val == '23':
		city = "MANFREDONIA.csv"
	elif val == '24':
		city = "SUPERSANO.csv"
	elif val == '25':
		city = "CORIGLIANO D'OTRANTO.csv"
	elif val == '26':
		city = "MANDURIA.csv"
	elif val == '27':
		city = "GALATINA.csv"
	elif val == '28':
		city = "CAPRARICA DI LECCE.csv"


	pcity = ""

	for file in os.listdir("samples/" + vyear[0] + "/"):
		if file.endswith(city):
			path = os.path.join("/" + vyear[0], file)
			pcity = "samples/" + path

	if pcity != "":		
		create_csv(pcity,city,path,vhour, vyear)
	else:
		print("nosaples for "+ city + " in " + vyear[0])

print ("------------------  CSV Custom Creator  ------------------ \n")

year = select_year()
city = select_city(year)
vhour = select_hours()

start_process(city, year, vhour)