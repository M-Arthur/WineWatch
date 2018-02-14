// WineWatch.nc
// Configuration for mote app
// Alex Scarpantoni 2015

includes WineWatch;
includes sensorboard;

configuration WineWatch {
	
}

implementation {
	components Main, GenericCommPromiscuous as Comm, MULTIHOPROUTER, ADCC, WineWatchM, TimerC, LedsC, IBADC, BatteryC, RelayC;

	Main.StdControl -> TimerC.StdControl;
	Main.StdControl -> WineWatchM.StdControl;
	Main.StdControl -> Comm.Control;
	Main.StdControl -> MULTIHOPROUTER.StdControl;
	Main.StdControl -> IBADC.StdControl;

	WineWatchM.Timer -> TimerC.Timer[unique("Timer")];
	WineWatchM.Leds -> LedsC.Leds;

	WineWatchM.ADC0 -> IBADC.ADConvert[0];
	WineWatchM.ADC1 -> IBADC.ADConvert[1];
	WineWatchM.ADC2 -> IBADC.ADConvert[2];
	WineWatchM.ADC3 -> IBADC.ADConvert[3];
	WineWatchM.ADC4 -> IBADC.ADConvert[4];
	WineWatchM.ADC5 -> IBADC.ADConvert[5];
	WineWatchM.ADC6 -> IBADC.ADConvert[6];
	WineWatchM.Excite -> IBADC.EXCITATION50;
	WineWatchM.ADCSC -> IBADC.StdControl;

	WineWatchM.PumpRelay1 -> RelayC.relay_normally_open;
	WineWatchM.PumpRelay2 -> RelayC.relay_normally_closed;
	WineWatchM.PumpRelayControl -> RelayC.RelayControl;

	WineWatchM.Battery -> BatteryC.Battery;
	WineWatchM.BatteryControl -> BatteryC.StdControl;

	WineWatchM.AckTimer -> TimerC.Timer[unique("Timer")];
	WineWatchM.ExciteTimer -> TimerC.Timer[unique("Timer")];
	WineWatchM.PumpTimer -> TimerC.Timer[unique("Timer")];

	WineWatchM.RouteControl -> MULTIHOPROUTER;
	WineWatchM.Send -> MULTIHOPROUTER.MhopSend[AM_XMULTIHOP_MSG];
	
	MULTIHOPROUTER.ReceiveMsg[AM_XMULTIHOP_MSG] -> Comm.ReceiveMsg[AM_XMULTIHOP_MSG];

	WineWatchM.ReceiveAck -> MULTIHOPROUTER.ReceiveAck[AM_XMULTIHOP_MSG];
	WineWatchM.Receive -> MULTIHOPROUTER.Receive[AM_XMULTIHOP_MSG];
}
