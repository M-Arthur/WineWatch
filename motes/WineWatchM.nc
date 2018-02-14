// WineWatchM.nc
// Main winewatch module
// Alex Scarpantoni 2015

includes WineWatch;
includes MultiHop;
includes sensorboard;

module WineWatchM {
	provides {
		interface StdControl;
	}

	uses {
		interface Timer;
		interface Timer as AckTimer;
		interface Timer as ExciteTimer;
		interface Timer as PumpTimer;
		interface Leds;
		interface Receive as ReceiveAck;
		interface Receive as Receive;

		interface Relay as PumpRelay1;
		interface Relay as PumpRelay2;
		interface StdControl as PumpRelayControl;

		interface Power as Excite;
		interface StdControl as ADCSC;
		interface ADConvert as ADC0;
		interface ADConvert as ADC1;
		interface ADConvert as ADC2;
		interface ADConvert as ADC3;
		interface ADConvert as ADC4;
		interface ADConvert as ADC5;
		interface ADConvert as ADC6;
		interface ADConvert as Battery;
		interface StdControl as BatteryControl;

		interface MhopSend as Send;
		interface RouteControl;
	}
}

implementation {
	bool sending_packet = FALSE;
	TOS_Msg msg_buffer;
	XDataMsg *pack;
	uint16_t sample_interval = DEFAULT_SAMPLE_INTERVAL;
	uint16_t use_pump = 0;
	uint16_t sample_set;
	uint32_t actual_sample_interval = DEFAULT_SAMPLE_INTERVAL * 1000;
	uint8_t waiting = 0;
	WCommand *wcommand;
	bool timer_running = TRUE;

	void task sendData() {
		call Leds.greenToggle();

		if(sending_packet) return;
		atomic sending_packet = TRUE;

		if(call Send.send(BASE_STATION_ADDRESS, MODE_UPSTREAM_ACK, 
			&msg_buffer, sizeof(XDataMsg)) != SUCCESS) {

			sending_packet = FALSE;
		}

		return;
	}

	command result_t StdControl.init() {
		uint16_t len;
		call Leds.init();
		call ADCSC.init();
		call BatteryControl.init();
		call PumpRelayControl.init();
		
		sample_set = eeprom_read_word((uint16_t*) 0x00);
		if(sample_set != EEPROM_MAGIC) {

			eeprom_write_word((uint16_t*) 0x02,
				DEFAULT_SAMPLE_INTERVAL);
			eeprom_write_word((uint16_t*) 0x04,
				DEFAULT_USE_PUMP);

			eeprom_write_word((uint16_t*) 0x00, EEPROM_MAGIC);
		} else {
			sample_interval = eeprom_read_word((uint16_t*) 0x02);
			actual_sample_interval = sample_interval;
			actual_sample_interval *= 1000;
			use_pump = eeprom_read_word((uint16_t*) 0x04);
		}

		atomic {
			pack = (XDataMsg*)call Send.getBuffer(
				&msg_buffer,&len);

			atomic pack->sensor[0] = 0x00;
			atomic pack->sensor[1] = 0x00;
			atomic pack->sensor[2] = 0x00;
			atomic pack->sensor[3] = 0x00;
			atomic pack->sensor[4] = 0x00;
			atomic pack->sensor[5] = 0x00;
			atomic pack->sensor[6] = 0x00;
			atomic pack->sample_interval = sample_interval;
			atomic pack->use_pump = use_pump;
			atomic pack->battery = 0x00;
		}

		return SUCCESS;
	}

	command result_t StdControl.start() {
		call Timer.start(TIMER_ONE_SHOT, actual_sample_interval);
		call ADCSC.start();
		call BatteryControl.start();
		call PumpRelayControl.start();

		return SUCCESS;
	}

	command result_t StdControl.stop() {
		call Timer.stop();
		call AckTimer.stop();
		call ExciteTimer.stop();
		call PumpTimer.stop();
		call ADCSC.stop();
		call BatteryControl.stop();
		call PumpRelayControl.stop();

		return SUCCESS;
	}

	event result_t Timer.fired() {
		atomic {
			if(waiting == 0) {
				call Excite.on();
				call Leds.yellowToggle();
				waiting = 8;
				if(use_pump != 0) {
					call Leds.redToggle();
					call PumpRelay1.close();
					call PumpRelay2.open();
					call PumpTimer.start(TIMER_ONE_SHOT, PUMP_TIME);
				} else {
					call ExciteTimer.start(TIMER_ONE_SHOT, EXCITE_TIME);
				}
			} else {
				call Timer.start(TIMER_ONE_SHOT, RESAMPLE_TIME);
			}
		}

		return SUCCESS;
	}

	event result_t ExciteTimer.fired() {
		atomic {
			call ADC0.getData();
			call ADC1.getData();
			call ADC2.getData();
			call ADC3.getData();
			call ADC4.getData();
			call ADC5.getData();
			call ADC6.getData();
			call Battery.getData();
		}
	}

	event result_t PumpTimer.fired() {
		atomic {
			call Leds.redToggle();
			call PumpRelay1.open();
			call PumpRelay2.close();
			call ExciteTimer.start(TIMER_ONE_SHOT, EXCITE_TIME);
		}
	}

	void data_in() {
		atomic {
			waiting--;
			if(waiting <= 0) {
				waiting = 0;
				call Excite.off();
				call Leds.yellowToggle();
				post sendData();
			}
		}
	}

	event result_t Send.sendDone(TOS_MsgPtr msg, result_t success) {
		call Leds.greenToggle();
		atomic sending_packet = FALSE;
		atomic timer_running = FALSE;
		call AckTimer.start(TIMER_ONE_SHOT, ACK_INTERVAL);
		return SUCCESS;
	}

	event TOS_MsgPtr ReceiveAck.receive(TOS_MsgPtr pMsg, void* payload,
		uint16_t payloadLen) {

		//call Leds.redToggle();		

		call AckTimer.stop();

		atomic {
			if(!timer_running) {
				call Timer.start(TIMER_ONE_SHOT, actual_sample_interval);
				timer_running = TRUE;
			}
		}

		return pMsg;
	}

	event TOS_MsgPtr Receive.receive(TOS_MsgPtr pMsg, void* payload,
		uint16_t payloadLen) {

		if(payloadLen > sizeof(WCommand)) {
			call Leds.redToggle();
			wcommand = payload;

			if(wcommand->magic == COMMAND_MAGIC) {
				if(wcommand->type == COMMAND_SAMPLE_TIME) {
					atomic {
						sample_interval = wcommand->param;
						if(sample_interval < MINIMUM_SAMPLE_INTERVAL) sample_interval = MINIMUM_SAMPLE_INTERVAL;
						if(sample_interval > MAXIMUM_SAMPLE_INTERVAL) sample_interval = MAXIMUM_SAMPLE_INTERVAL;
						actual_sample_interval = sample_interval;
						actual_sample_interval *= 1000;
						pack->sample_interval = sample_interval;
					}

					eeprom_write_word((uint16_t*) 0x02,
						sample_interval);
				}

				if(wcommand->type == COMMAND_USE_PUMP) {
					atomic {
						use_pump = wcommand->param;
						pack->use_pump = use_pump;
					}

					eeprom_write_word((uint16_t*) 0x04,
						use_pump);
				}
 			}
		}

		return pMsg;
	}

	event result_t AckTimer.fired() {
		post sendData();
		return SUCCESS;
	}

	event result_t ADC0.dataReady(uint16_t data) {atomic pack->sensor[0] = data; data_in(); return SUCCESS;}
	event result_t ADC1.dataReady(uint16_t data) {atomic pack->sensor[1] = data; data_in(); return SUCCESS;}
	event result_t ADC2.dataReady(uint16_t data) {atomic pack->sensor[2] = data; data_in(); return SUCCESS;}
	event result_t ADC3.dataReady(uint16_t data) {atomic pack->sensor[3] = data; data_in(); return SUCCESS;}
	event result_t ADC4.dataReady(uint16_t data) {atomic pack->sensor[4] = data; data_in(); return SUCCESS;}
	event result_t ADC5.dataReady(uint16_t data) {atomic pack->sensor[5] = data; data_in(); return SUCCESS;}
	event result_t ADC6.dataReady(uint16_t data) {atomic pack->sensor[6] = data; data_in(); return SUCCESS;}
	event result_t Battery.dataReady(uint16_t data) {atomic pack->battery = data; data_in(); return SUCCESS;}

}
