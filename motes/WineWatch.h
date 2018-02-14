typedef struct XDataMsg {
	uint16_t sensor[7];
	uint16_t sample_interval;
	uint16_t use_pump;
	uint16_t battery;
} __attribute__ ((packed)) XDataMsg;

typedef struct WCommand {
	uint16_t magic;
	char type;
	uint16_t param;
} WCommand;

enum {
	COMMAND_SAMPLE_TIME = 1,
	COMMAND_USE_PUMP = 2
};

enum {
	AM_XDEBUG_MSG    = 49,
	AM_XSENSOR_MSG   = 50,
	AM_XMULTIHOP_MSG = 51,         // xsensor multihop 
};

#define DEFAULT_SAMPLE_INTERVAL	5
#define DEFAULT_USE_PUMP	1
#define PUMP_TIME		5000
#define COMMAND_MAGIC		0xABCD
#define EEPROM_MAGIC		0x2375
#define MINIMUM_SAMPLE_INTERVAL 1
#define MAXIMUM_SAMPLE_INTERVAL 3600
#define ACK_INTERVAL		500
#define EXCITE_TIME		100
#define RESAMPLE_TIME		100