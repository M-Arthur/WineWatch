# DataPacket.py
# Alex Scarpantoni, 2015
# alex@ascarp.com
# Class to represent, serialize and unserialize data packets as forwarded
# by XMesh base e.g. over a serial port
#
# To initialize:
#    packet = DataPacket()
#  
# To convert a byte string into a packet
#    packet.read( input_bytes )
# 
# To convert packet to byte string
#    byte_string = packet.write()
#
# For packet variables, see class definition below.

import struct

# AM Types
AM_UPSTREAM_ACK = 247		# Ack to base station from node.
AM_DATAACK2NODE = 14		# Message from base with ACK required.
AM_DATAACK2BASE = 13		# Message from node with ACK required.
AM_DATA2BASE = 11		# Message to base (no ACK) required.
AM_DATA2NODE = 12		# Message to node (no ACK) required

class DataPacket:
	# Packet Variables
	packet_type = 0x42	# This is between base station and computer.
	destination = 0		# Node ID to send message to.
	am_type = 0		# See AM Types above.
	am_group = 0		# AM Group of the packet (default 125?).
	packet_length = 0	# Total length of packet.
	source_address = 0	# Node ID of last node to forward packet.
	origin_address = 0	# Node ID of sender.
	sequence_number = 0	# Sequence number of message.
	application_id = 0	# Application ID (your application specific).
	payload = []		# Byte array representing payload data.

	# Reads a string of bytes into the DataPacket
	def read(self, pstr):
		pstr = bytearray(pstr)
		
		# iterate through and remove escaped characters
		c = 0
		while c < len(pstr):
			if(pstr[c] == 0x7D):
				post_char = pstr[c+1]
				pstr = pstr[0:c] + pstr[c+1:]
				pstr[c] = post_char ^ 0x20
			c += 1
	
		# check packet has at least 13 bytes
		if(len(pstr) < 13): return

		# unpack the headers
		unpacked = struct.unpack("<BHBBBHHHB", pstr[0:13])

		self.packet_type = unpacked[0]
		self.destination = unpacked[1]
		self.am_type = unpacked[2]
		self.am_group = unpacked[3]
		self.packet_length = unpacked[4]
		self.source_address = unpacked[5]
		self.origin_address = unpacked[6]
		self.sequence_number = unpacked[7]
		self.application_id = unpacked[8]

		# extract payload as a string of bytes
		self.payload = pstr[13:-2]

	# Converts the DataPacket into a string of bytes
	def write(self):
		# trim payload
		if(len(self.payload) > 128):
			self.payload = self.payload[0:128]

		# calculate length
		self.packet_length = 7 + len(self.payload)
		
		# construct headers
		headers = struct.pack("<BHBBBHHHB",
			self.packet_type,
			self.destination,
			self.am_type,
			self.am_group,
			self.packet_length,
			self.source_address,
			self.origin_address,
			self.sequence_number,
			self.application_id)

		# concatentate headers and payload
		packet = bytearray(headers) + bytearray(self.payload)

		# add the crc
		packet = packet + bytearray(
			struct.pack("<H", self.crc(packet)))

		# add escapes
		c = 0
		while c < len(packet):
			if( (packet[c] == 0x7D) or (packet[c] == 0x7E) ):
				packet[c] = packet[c] ^ 0x20
				packet.insert(c, 0x7D)
				c += 1
			c += 1

		return bytearray([0x7e]) + packet + bytearray([0x7e])

	# CRC checksum implementation
	# based on C code provided by Crossbow
	def crcbyte(self, crc, b):
		crc = crc ^ (b << 8)
		for i in range(0,8):
			if( (crc & 0x8000) == 0x8000 ):
				crc = crc << 1 ^ 0x1021
			else:
				crc = crc << 1
		return crc & 0xffff

	def crc(self, packdata):
		crc = 0
		for c in packdata:
			crc = self.crcbyte(crc, c)
		return crc
