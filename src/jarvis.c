#include "c_types.h"
#include "ip_addr.h"
#include "espconn.h"
#include "ets_sys.h"
#include "gpio.h"
#include "osapi.h"
#include "os_type.h"
#include "user_interface.h"

#if !defined(SSID) || !defined(WIFI_PWD)
#error "Please specify 'SSID' and 'WIFI_PWD' defines during compilation"
#endif

/*
 * Enable command needs to be resent every 10 mins, like a Watchdog pat
 * else the output will get disabled.
 *
 * If this functionality is not needed, then a 'Always Enable' command
 * needs to be sent
 */
#define	ENABLE		'E'
#define	ALWAYS_ENABLE	'A'
#define	DISABLE		'D'
#define	OUTPUT_BIT	BIT2

#define	ENABLE_TIMEOUT_SECS	600

char outbuffer[2048];

struct espconn esp_conn;
esp_tcp esptcp;

os_timer_t	timer;
int		secs_elapsed = 0;

/* Timer handling functions */
void enable_timer(void);
void disable_timer(void);

void set_output(char state);

static void
timer_callback(void *param)
{
	secs_elapsed++;

	if (secs_elapsed == ENABLE_TIMEOUT_SECS) {
		/* There is a race condition here. Assume you won't hit it */
		disable_timer();
		set_output(DISABLE);
	}
}

void ICACHE_FLASH_ATTR
enable_timer(void)
{
	secs_elapsed = 0;
	os_timer_disarm(&timer);
	os_timer_setfn(&timer, timer_callback, NULL);
	os_timer_arm(&timer, 1000, 1);
}

void ICACHE_FLASH_ATTR
disable_timer(void)
{
	secs_elapsed = 0;
	os_timer_disarm(&timer);
}

void ICACHE_FLASH_ATTR
set_output(char state)
{
	switch(state) {
	case ENABLE:
		os_printf("Turning on LED\n");
		gpio_output_set(OUTPUT_BIT, 0, OUTPUT_BIT, 0);
		break;
	case DISABLE:
		os_printf("Turning off LED\n");
		gpio_output_set(0, OUTPUT_BIT, OUTPUT_BIT, 0);
		break;
	default:
		os_printf("Error: Unknown bit state %c\n", state);
	}
}

void ICACHE_FLASH_ATTR
wifi_init()
{
	struct station_config stationConf;

	wifi_set_opmode(STATION_MODE);

	os_memcpy(&stationConf.ssid, SSID, 32);
	os_memcpy(&stationConf.password, WIFI_PWD, 32);
	wifi_station_set_config(&stationConf);

	wifi_station_connect();
}

void ICACHE_FLASH_ATTR
server_sent(void *arg)
{
}

void ICACHE_FLASH_ATTR
server_recv(void *arg, char *usrdata, unsigned short length)
{
	struct espconn 	*esp_conn = arg;
	char *ACK = "ACK";
	char *NACK = "NACK";
	char *reply = ACK;

	os_printf("Received %d bytes: %s\n", strlen(usrdata), usrdata);
	if (length != 1) {
		reply = NACK;
	} else {
		switch(usrdata[0]) {
		case ENABLE:
			enable_timer();
			set_output(ENABLE);
			break;

		case ALWAYS_ENABLE:
			disable_timer();
			set_output(ENABLE);
			break;

		case DISABLE:
			disable_timer();
			set_output(DISABLE);
			break;
		default:
			os_printf("Error: Unknown message %c\n", usrdata[0]);
			reply = NACK;
		}
	}

	os_memcpy(outbuffer, reply, strlen(reply) + 1);
	espconn_send(esp_conn, outbuffer, strlen(reply) + 1);
}

void ICACHE_FLASH_ATTR
server_recon(void *arg, sint8 err)
{
	struct espconn *pesp_conn = arg;

	os_printf("webserver's %d.%d.%d.%d:%d err %d reconnect\n", pesp_conn->proto.tcp->remote_ip[0],
	    pesp_conn->proto.tcp->remote_ip[1],pesp_conn->proto.tcp->remote_ip[2],
	    pesp_conn->proto.tcp->remote_ip[3],pesp_conn->proto.tcp->remote_port, err);
}


void ICACHE_FLASH_ATTR
server_discon(void *arg)
{
	struct espconn *pesp_conn = arg;

	os_printf("webserver's %d.%d.%d.%d:%d disconnect\n", pesp_conn->proto.tcp->remote_ip[0],
	    pesp_conn->proto.tcp->remote_ip[1],pesp_conn->proto.tcp->remote_ip[2],
	    pesp_conn->proto.tcp->remote_ip[3],pesp_conn->proto.tcp->remote_port);
}


void ICACHE_FLASH_ATTR
server_connect(void *arg)
{
	struct espconn *pesp_conn = arg;

	os_printf("Connected to %d.%d.%d.%d:%d ...\n", pesp_conn->proto.tcp->remote_ip[0],
	    pesp_conn->proto.tcp->remote_ip[1],pesp_conn->proto.tcp->remote_ip[2],
	    pesp_conn->proto.tcp->remote_ip[3],pesp_conn->proto.tcp->remote_port);

	espconn_regist_recvcb(pesp_conn, server_recv);
	espconn_regist_sentcb(pesp_conn, server_sent);
	espconn_regist_reconcb(pesp_conn, server_recon);
	espconn_regist_disconcb(pesp_conn, server_discon);
}

void ICACHE_FLASH_ATTR
initialise_gpio()
{
	gpio_init();

	//Set GPIO2 to output mode
	PIN_FUNC_SELECT(PERIPHS_IO_MUX_GPIO2_U, FUNC_GPIO2);

	//Set GPIO2 low
	gpio_output_set(0, OUTPUT_BIT, OUTPUT_BIT, 0);
}

void ICACHE_FLASH_ATTR
user_init()
{
	uart_init(115200, 115200);
	//wifi_init();
	initialise_gpio();

	esp_conn.type = ESPCONN_TCP;
	esp_conn.state = ESPCONN_NONE;
	esp_conn.proto.tcp = &esptcp;
	esp_conn.proto.tcp->local_port = 5000;
	espconn_regist_connectcb(&esp_conn, server_connect);

	espconn_accept(&esp_conn);
}
