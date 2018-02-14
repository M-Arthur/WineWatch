# WineWatchComms.py
# Alex Scarpantoni, 2015
# alex@ascarp.com
# Class to communicate with the WineWatch server.
# Provides mechanism for heartbeat and uploading sensor data.
#
# Use get_command, send_data and kill externally
# Do not access the helper methods plz.

import DataPacket
import threading, urllib2, urllib, json, time

class WineWatchComms:
	def __init__(self, api, bid, token, command_interval=5,
		resend_interval=1, verbose=False):
		self.api = api
		self.bid = bid
		self.token = token
		self.command_interval = command_interval
		self.resend_interval = resend_interval
		self.verbose = verbose
		self.commands_last_fetched = 0

		self.command_queue = []
		self.data_queue = []

		self.command_queue_lock = threading.Lock()
		self.data_queue_lock = threading.Lock()

		self.stop = False

		# fire up a thread to do communication
		self.comm_thread = threading.Thread(
			target = self.comm_handler)

		self.comm_thread.start()

	def comm_handler(self):
		while self.stop != True:
			# Fetch commands
			elapsed = time.time() - self.commands_last_fetched
			if elapsed > self.resend_interval:
				self.fetch_commands()
				self.commands_last_fetched = time.time()

			# Upload data
			self.upload_data()

	# Helper function to fetch commands
	def fetch_commands(self):
		datum = {
				"inactive_motes": json.dumps([]),
				"bid": self.bid,
				"token": self.token
			}

		request = urllib2.urlopen(
			self.api + "heartbeat/query_command",
			urllib.urlencode(datum))

		response = request.read()

		commands = json.loads(response)["commands"]
		for command in commands:
			if self.verbose:
				print "Got command " + str(command)
			
			if len(command) < 2: continue

			self.command_queue_lock.acquire()
			self.command_queue.append(command)
			self.command_queue_lock.release()

	# Helper function to do the upload
	def upload_data(self):
		while len(self.data_queue) > 0:
			self.data_queue_lock.acquire()
			current_data = self.data_queue.pop(0)
			self.data_queue_lock.release()

			datum = {
				"package": json.dumps(current_data["sensors"]),
				"bid": self.bid,
				"time": current_data["time"],
				"mote": current_data["mid"],
				"token": self.token
			}

			if self.verbose:
				print "Uploading data " + str(datum)

			request = urllib2.urlopen(
				self.api + "reading/update",
				urllib.urlencode(datum))

			response = request.read()

			if "Success" not in response:
				data_queue_lock.acquire()
				data_queue.insert(0, current_data)
				data_queue_lock.release()
				
				if self.verbose:
					print "Upload failed. Retrying later."

				time.sleep(self.resend_interval)
				return
			else:
				if self.verbose:
					print "Uploaded packet successfully."
		pass

	def kill(self):
		self.stop = True

	def get_command(self):
		self.command_queue_lock.acquire()
		command = False
		
		if len(self.command_queue) > 0:
			command = command_queue.pop(0)

		self.command_queue_lock.release()
		return command

	def send_data(self, data):
		self.data_queue_lock.acquire()
		self.data_queue.append(data)
		self.data_queue_lock.release()