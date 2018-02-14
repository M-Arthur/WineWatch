# XMeshBase.py
# Alex Scarpantoni, 2015
# alex@ascarp.com
# Class to communicate with an XMeshBase enabled mote over a serial port
# and provide an interface for sending and receiving packets.
#
# Use the initializer, send_packet, get_packet, stop

import DataPacket
import serial, threading, time

class XMeshBase:
	def __init__(self, port, require_ack = False,
		ack_timeout = 200, verbose = False, serial_timeout = 1):

		self.in_queue = []
		self.out_queue = []
		self.waiting_for_ack_queue = []
		self.in_queue_lock = threading.Lock()
		self.out_queue_lock = threading.Lock()
		self.stop = False

		self.verbose = verbose

		self.port = port
		self.require_ack = require_ack
		self.ack_timeout = float(ack_timeout)/1000.0

		# (attempt to) open the serial port
		self.serial = serial.Serial(self.port, 57600,
			timeout=serial_timeout)

		# fire up a thread to do communication
		self.serial_thread = threading.Thread(
			target = self.serial_handler)

		self.serial_thread.start()

	def serial_handler(self):
		packet = ""
		while self.serial.isOpen():
			# handle the stop signal
			if(self.stop):
				self.serial.close()
				return

			# requeue any packets that have expired ACK times
			i = 0
			while i < len(self.waiting_for_ack_queue):
				pack = self.waiting_for_ack_queue[i]
				if(time.time() > pack[1]):
					self.waiting_for_ack_queue.pop(i)
					self.out_queue_lock.acquire()
					self.out_queue.append(pack[0])
					self.out_queue_lock.release()
					i = 0
					continue
				i += 1

			# send any packets in out queue whose motes aren't
			# waiting on acks
			self.out_queue_lock.acquire()
			i = 0
			while i < len(self.out_queue):
				pack = self.out_queue[i]

				# check there's no ack pending for this mote
				if self.ack_pending(pack.destination):
					i += 1
					continue

				pack = self.out_queue.pop(i)

				self.serial.write(pack.write())
				
				# print a line if we're verbose
				if(self.verbose):
					print "Sent packet to mote " + \
						str(pack.destination)

				# add to the ack queue
				if(self.require_ack):
					self.waiting_for_ack_queue.append([
						pack,
						time.time() + self.ack_timeout
					])

				i = 0

			self.out_queue_lock.release()
	
			# read packets coming in
			c = self.serial.read(1)
			if(len(c) == 0): continue

			if(ord(c) != 0x7E):
				packet += c
				continue
			
			if(len(packet) == 0):
				continue

			pack = DataPacket.DataPacket()
			pack.read(packet)
			packet = ""

			if(pack.origin_address == 0): continue

			# check if we got an ack
			if ( (pack.am_type == DataPacket.AM_UPSTREAM_ACK) and
				(self.require_ack) ):

				self.process_ack(pack.origin_address)

				if self.verbose:
					print "ACK from mote " + \
						str(pack.origin_address)

			else:
				self.in_queue_lock.acquire()
				self.in_queue.append(pack)
				self.in_queue_lock.release()

				if self.verbose:
					print "Packet from mote " + \
						str(pack.origin_address)

			

	# Helper function, checks if a mote is waiting on acks
	def ack_pending(self, mote):
		i = 0
		while i < len(self.waiting_for_ack_queue):
			pack = self.waiting_for_ack_queue[i][0]

			if(pack.destination == mote):
				return True
			
			i += 1

		return False

	# Helper function, removes acks for a mote
	def process_ack(self, mote):
		i = 0
		while i < len(self.waiting_for_ack_queue):
			pack = self.waiting_for_ack_queue[i][0]

			if(pack.destination == mote):
				self.waiting_for_ack_queue.pop(i)
				i = 0
				continue
			
			i += 1

	# Sends a packet, will resend if specified in class initializer
	def send_packet(self, packet):
		self.out_queue_lock.acquire()
		self.out_queue.append(packet)
		self.out_queue_lock.release()

	# Returns a packet if there's one in the queue
	# returns False if there are no packets waiting to be receieved
	def get_packet(self):
		self.in_queue_lock.acquire()
		pack = False
		
		if len(self.in_queue) > 0:
			pack = self.in_queue.pop(0)
			
		self.in_queue_lock.release()
		return pack

	# Terminates the serial thread
	def kill(self):
		self.stop = True