#!/usr/bin/python

# winewatch
# alex scarpantoni
# outputs sensor values read in from serial port

import serial, struct, time, sys, DataPacket

pack = DataPacket.DataPacket()

if(len(sys.argv) < 2):
	print "Usage: sensor_out.py <serial_port>"
	sys.exit(0)

ser = serial.Serial(sys.argv[1], 57600)

packet = ""

while ser.isOpen():
	c = ser.read(1)

	if(ord(c) != 0x7E):
		packet += c
	else:
		if(len(packet) < 20):
			packet = ""
			continue

		pack.read(packet)
		data = pack.payload
		

		for x in range(0,6):
			sdata = struct.unpack("<H", data[2*x:(2*x)+2])[0]
			voltage = (2.5*sdata)/4096
			temp = voltage/.01

			print str(time.time()) + " Sensor " + str(x) + " : " + str(temp)

		print str(time.time()) + " Sample Interval : " + str( struct.unpack("<H", data[14:16])[0] )
		print str(time.time()) + " Battery : " + str( 1.223 * 1024 / (struct.unpack("<H", data[16:18])[0]))

		print ""
		#	sensors["t" + str(x)] = temp
		
		#sensors["mote"] = board_id

		#print data

		# reset packet
		packet = ""