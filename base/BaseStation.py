# BaseStation.py
# Alex Scarpantoni, 2015
# alex@ascarp.com
# WineWatch base station script.

import DataPacket, WineWatchComms, XMeshBase
import time, struct

config = {}

# try and load config file
config_list = open("config.txt").readlines()
for config_line in config_list:
	config[config_line.split(" ")[0].strip()] = \
		config_line.split(" ")[1].strip()

xmb = XMeshBase.XMeshBase(config["port"], require_ack = True, verbose = True)
wwc = WineWatchComms.WineWatchComms(config["api"], config["bid"],
	config["token"], verbose = True)

try:
	while True:
		# Process commands
		command = wwc.get_command()
		while command != False:
			ccode = 0

			if command["command"] == "sample":
				ccode = 1
			elif command["command"] == "pump":
				ccode = 2
			else:
				continue

			pack = DataPacket.DataPacket()
			pack.application_id = 51
			pack.am_type = DataPacket.AM_DATAACK2NODE
			pack.am_group = 125
			pack.source_address = 0
			pack.origin_address = 0
			pack.destination = command["mote"]
			pack.payload = struct.pack("<HBH",
				0xABCD, ccode, command["parameter"])

			xmb.send_packet(pack)
			command = wwc.get_command()
			
		# Process all incoming packets
		pack = xmb.get_packet()
		while pack != False:
			data = pack.payload

			if(len(data) < 18):
				continue

			sensors = {}
			sensors["temps"] = []
			send = True
			for x in range(0,6):
				sdata = struct.unpack("<H",data[2*x:(2*x)+2])[0]
				voltage = (2.5*sdata)/4096
				temp = voltage/.01
				if (temp > 40) or (temp < 5):
					send = False
				sensors["temps"].append(temp)

			# if send == False:
			# 	print "Erroneous reading detected."
			# 	continue

			sensors["battery"] = \
				1.223 * 1024 /struct.unpack("<H", data[-2:])[0]
			sensors["pump_enabled"] = \
				struct.unpack("<H", data[-4:-2])[0]
			sensors["sample_interval"] = \
				struct.unpack("<H", data[-6:-4])[0]

			csample = {"sensors": sensors,
				"mid": pack.origin_address, 
				"time": time.time()}

			wwc.send_data(csample)
			pack = xmb.get_packet()

except KeyboardInterrupt:
	xmb.kill()
	wwc.kill()