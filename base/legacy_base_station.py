#!/usr/bin/python

# winewatch
# alex scarpantoni
# winewatch base station

import sys, urllib2, random, time, json, urllib, struct, serial, threading, DataPacket

config = {}

# try and load config file
config_list = open("config.txt").readlines()
for config_line in config_list:
	config[config_line.split(" ")[0].strip()] = config_line.split(" ")[1].strip()

samples = []
serial_send_queue = []
serial_send_lock = threading.Lock()
waiting_for_acks = []
samples_lock = threading.Lock()
kill = False

def sample_sender():
	global samples, samples_lock, kill

	while True:
		while((len(samples) == 0) or (kill == True)):
			if(kill == True):
				return

		# Should ideally dump all unsent data in a file if it's killed

		samples_lock.acquire()
		current_sample = samples.pop(0)
		samples_lock.release()

		datum = {
			"package": json.dumps(current_sample["sensors"]),
			"bid": config["bid"],
			"time": current_sample["time"],
			"mote": current_sample["mid"],
			"token": config["token"]
		}

		print "Uploading sample data: " + str(datum)

		try:
			request = urllib2.urlopen(config["api"] + "reading/update", urllib.urlencode(datum))
			response = request.read()

			# If it didn't add, put it back in the list and sleep for sample time
			if( "Success" not in response ):
				samples_lock.acquire()
				samples.append(current_sample)
				samples_lock.release()
				print "Upload failed. Retrying later."
				time.sleep(int(config["resend_interval"]))
			else:
				print "Upload successful."

			
		except urllib2.HTTPError as e:
			print "Error: ", e		

def mote_reader():
	global samples, samples_lock, kill

	pack = DataPacket.DataPacket()

	try:
		ser = serial.Serial(config["port"], 57600, timeout=1)
	except serial.SerialException:
		print "Couldn't open port"
		kill = True
		return

	packet = ""

	while ser.isOpen():
		if(kill == True):
			ser.close()
			break

		# put unacknowledged packets in the send queue
		i = 0
		while i < len(waiting_for_acks):
			pack = waiting_for_acks[i]
			if(time.time() > pack[1]):
				waiting_for_acks.pop(i)
				serial_send_lock.acquire()
				serial_send_queue.append(pack[0])
				serial_send_lock.release()
				i = 0
				continue
			i+=1

		# if there's packets to send
		while(len(serial_send_queue) > 0):
			serial_send_lock.acquire()
			pack_to_send = serial_send_queue.pop(0)
			serial_send_lock.release()
			ser.write(pack_to_send.write())
			print "Sent packet to mote " + str(pack_to_send.destination)
			waiting_for_acks.append( [pack_to_send, time.time() + float(config["ack_time"])])

		c = ser.read(1)
		if(len(c) == 0): continue

		if(ord(c) != 0x7E):
			packet += c
		else:
			pack = DataPacket.DataPacket()
			pack.read(packet)
			data = pack.payload

			# check if we get an ack pack here
			# print("Packet Size:" + str(len(packet)))

			# remove ack from the waiting for ack queue
			# assume we have an ack packet here
			if(len(packet) == 20):
				i = 0
				while i < len(waiting_for_acks):
					apack = waiting_for_acks[i][0]
					#print "Comparing origin of pack: " + str(pack.origin_address)
					#print "With destination of sent: " + str(apack.destination)

					if(apack.destination == pack.origin_address):
						print "Receieved ACK from mote " + str(apack.destination)
						waiting_for_acks.pop(i)
						i = 0
						continue
					i += 1
				continue

			if(len(data) < 18):
				packet = ""
				continue

			sensors = {}
			sensors["temps"] = []
			send = True
			for x in range(0,6):
				sdata = struct.unpack("<H", data[2*x:(2*x)+2])[0]
				voltage = (2.5*sdata)/4096
				temp = voltage/.01
				if (temp > 40) or (temp < 5):
					send = False
				sensors["temps"].append(temp)

			if send == False:
				print "Erroneous reading detected."
				packet = ""
				continue

			sensors["battery"] = 1.223 * 1024 /struct.unpack("<H", data[-2:])[0]
			sensors["pump_enabled"] = struct.unpack("<H", data[-4:-2])[0]
			sensors["sample_interval"] = struct.unpack("<H", data[-6:-4])[0]

			csample = {"sensors": sensors, "mid": pack.origin_address, "time": time.time()}
							
			samples_lock.acquire()
			samples.append(csample)
			samples_lock.release()

			# reset packet
			packet = ""

	kill = True

def send_command(command, parameter, mote):
	try:
		print "Sending command " + str(command) + " : " + str(parameter) + " for mote " + str(mote)
		pack = DataPacket.DataPacket()
		pack.packet_type = 0x42
		pack.application_id = 51
		pack.am_type = 0x0E
		pack.am_group = 125
		pack.source_address = 0
		pack.origin_address = 0
		pack.destination = int(mote)
		if(parameter == ""): return
		pack.payload = struct.pack("<HBH", 0xABCD, command, int(parameter))

		serial_send_lock.acquire()
		serial_send_queue.append(pack)
		serial_send_lock.release()
	except:
		print "Error occured"

def control_monitor():
	while True:
		if(kill == True):
			return
		time.sleep(int(config["command_interval"]))
		try:
			datum = {
				"inactive_motes": json.dumps([]),
				"bid": config["bid"],
				"token": config["token"]
			}

			request = urllib2.urlopen(config["api"] + "heartbeat/query_command", urllib.urlencode(datum))
			response = request.read()

			commands = json.loads(response)["commands"]
			for command in commands:
				print "Processing command " + str(command)
				if len(command) < 2: continue

				if(command["command"] == "sample"):
					send_command(1, int(command["parameter"]), command["mote"])

		except urllib2.HTTPError as e:
			print "Error: ", e

sample_sender_thread = threading.Thread(target = sample_sender)
sample_sender_thread.start()

mote_reader_thread = threading.Thread(target = mote_reader)
mote_reader_thread.start()

control_monitor_thread = threading.Thread(target = control_monitor)
control_monitor_thread.start()

try:
	while(kill == False):
		something = str(raw_input()).split(" ")
		if(len(something) > 2):
			send_command(int(something[0]), int(something[1]), int(something[2]))
except:
	kill = True

print "Joining Mote reader"
mote_reader_thread.join()
print "Mote Reader finished, joining sample sender"
sample_sender_thread.join()
print "Sample sender finished"
