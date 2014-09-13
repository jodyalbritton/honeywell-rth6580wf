/**
 *  Total Comfort API
 *
 */
preferences 
{
	input("username", "text", title: "Username", description: "Your Total Comfort User Name")
	input("password", "password", title: "Password", description: "Your Total Comfort password")
	input("honeywelldevice", "text", title: "Device ID", description: "Your Device ID")
	
}

metadata 
{
	definition (name: "Honeywell Thermostat", namespace: "st.jodyalbritton", author: "jodyalbritton") 
	{
		capability "Polling"
		capability "Thermostat"
		capability "Refresh"
		capability "Temperature Measurement"
		capability "Sensor"
	}

	simulator
	{
		// TODO: define status and reply messages here
	}

   tiles 
   {
		valueTile("temperature", "device.temperature", width: 1, height: 1, canChangeIcon: true) 
		{
			state("temperature", label: '${currentValue}°F', unit:"F", backgroundColors: 
			[
				[value: 31, color: "#153591"],
				[value: 44, color: "#1e9cbb"],
				[value: 59, color: "#90d2a7"],
				[value: 74, color: "#44b621"],
				[value: 84, color: "#f1d801"],
				[value: 95, color: "#d04e00"],
				[value: 96, color: "#bc2323"]
			]
			)
		}
		
		standardTile("thermostatMode", "device.thermostatMode", inactiveLabel: false, decoration: "flat")
		{
			state "heat", action:"thermostat.off", icon: "st.thermostat.heat"			
			state "cool", action:"thermostat.heat", icon: "st.thermostat.cool"
			state "off", action:"thermostat.cool", icon: "st.thermostat.heating-cooling-off"
		}
		
		standardTile("thermostatFanMode", "device.thermostatFanMode", inactiveLabel: false, decoration: "flat") 
		{
			state "auto", action:"thermostat.fanOn", icon: "st.thermostat.fan-auto"
			state "on", action:"thermostat.fanCirculate", icon: "st.thermostat.fan-on"
			state "circulate", action:"thermostat.fanAuto", icon: "st.thermostat.fan-off"
		}
		
		controlTile("coolSliderControl", "device.coolingSetpoint", "slider", height: 1, width: 2, inactiveLabel: false) 
		{
			state "setCoolingSetpoint", label:'Set to', action:"thermostat.setCoolingSetpoint", 
			backgroundColors:
			[
				[value: 31, color: "#153591"],
				[value: 44, color: "#1e9cbb"],
				[value: 59, color: "#90d2a7"],
				[value: 74, color: "#44b621"],
				[value: 84, color: "#f1d801"],
				[value: 95, color: "#d04e00"],
				[value: 96, color: "#bc2323"]
			]			   
		}
		
		valueTile("coolingSetpoint", "device.coolingSetpoint", inactiveLabel: false) 
		{
			state "default", label:'Set @ ${currentValue}°F', unit:"F",
			 backgroundColors:
			 [
				[value: 31, color: "#153591"],
				[value: 44, color: "#1e9cbb"],
				[value: 59, color: "#90d2a7"],
				[value: 74, color: "#44b621"],
				[value: 84, color: "#f1d801"],
				[value: 95, color: "#d04e00"],
				[value: 96, color: "#bc2323"]
			]   
		}
		
		valueTile("humidity", "device.humidity", inactiveLabel: false, decoration: "flat")
		{
			state "default", label:'${currentValue}%', unit:"Humidity"
		}
		
		standardTile("refresh", "device.thermostatMode", inactiveLabel: false, decoration: "flat") 
		{
			state "default", action:"polling.poll", icon:"st.secondary.refresh"
		}
		
		main "temperature"
		details(["temperature", "thermostatMode", "thermostatFanMode", "coolSliderControl", "coolingSetpoint", "humidity", "refresh"])
	}
}

// parse events into attributes
def parse(String description) 
{	
}

// handle commands
def setHeatingSetpoint(temp) 
{
	if (temp < 40)
	{
		temp = 40
	}
	
	if (temp > 99)
	{
		temp = 99
	}
	setTargetTemp(temp)
}

def setCoolingSetpoint(temp) 
{
	if (temp < 40)
	{
		temp = 40
	}
	
	if (temp > 99)
	{
		temp = 99
	}
	setTargetTemp(temp)
}

def setTargetTemp(temp) 
{
	data.SystemSwitch = 'null' 
	data.HeatSetpoint = temp
	data.CoolSetpoint = temp
	data.HeatNextPeriod = 'null'
	data.CoolNextPeriod = 'null'
	data.StatusHeat='1'
	data.StatusCool='1'
	data.FanMode = 'null'
	setStatus()
}

def heat() 
{
	setThermostatMode(1)
}

def cool() 
{
	setThermostatMode(2)
}

def off() 
{
	setThermostatMode(3)
}

def emergencyHeat() 
{
}

def setThermostatMode(mode) 
{
	data.SystemSwitch = mode 
	data.HeatSetpoint = 'null'
	data.CoolSetpoint = 'null'
	data.HeatNextPeriod = 'null'
	data.CoolNextPeriod = 'null'
	data.StatusHeat = 1
	data.StatusCool = 1
	data.FanMode = 'null'

	setStatus()
	
	def switchPos

	if(mode==1)
		switchPos = 'heat'
	if(mode==2)
		switchPos = 'cool'
	if(mode==3)
		switchPos = 'off'
    if(mode==5)
		switchPos = 'auto'

	sendEvent(name: 'thermostatMode', value: switchPos)
}

def fanOn() 
{
	setThermostatFanMode(1)
}

def fanAuto() 
{
	setThermostatFanMode(0)
}

def fanCirculate() 
{
	setThermostatFanMode(2)
}

def setThermostatFanMode(mode) 
{	
	data.SystemSwitch = 'null' 
	data.HeatSetpoint = 'null'
	data.CoolSetpoint = 'null'
	data.HeatNextPeriod = 'null'
	data.CoolNextPeriod = 'null'
	data.StatusHeat='null'
	data.StatusCool='null'
	data.FanMode = mode

	setStatus()

	def fanMode

	if(mode==0)
	 	fanMode = 'auto'
	if(mode==2)
		fanMode = 'circulate'
	if(mode==1)
		fanMode = 'on'

	sendEvent(name: 'thermostatFanMode', value: fanMode)	
}


def poll() 
{
	refresh()
}

def setStatus()
{
	login()
	log.debug "Executing 'setStatus'"
	def today= new Date()
	log.debug "https://rs.alarmnet.com/TotalConnectComfort/Device/SubmitControlScreenChanges"
	
	def params = 
	[
		uri: "https://rs.alarmnet.com/TotalConnectComfort/Device/SubmitControlScreenChanges",
		headers: 
		[
			'Accept': 'application/json, text/javascript, */*; q=0.01',
			'DNT': '1',
			'Accept-Encoding': 'gzip,deflate,sdch',
			'Cache-Control': 'max-age=0',
            'Content-Type':'application/json; charset=UTF-8',
			'Accept-Language': 'en-US,en,q=0.8',
			'Connection': 'keep-alive',
			'Host': 'rs.alarmnet.com',
			'Referer': "https://rs.alarmnet.com/TotalConnectComfort/",
			'X-Requested-With': 'XMLHttpRequest',
			'User-Agent': 'Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.95 Safari/537.36',
            'Referer':'/TotalConnectComfort/Device/CheckDataSession/${settings.honeywelldevice}',
			'Cookie': data.cookiess		
		],
		body: 
		[
			DeviceID: "${settings.honeywelldevice}", 
			SystemSwitch : data.SystemSwitch,
			HeatSetpoint : data.HeatSetpoint, 
			CoolSetpoint: data.CoolSetpoint, 
			HeatNextPeriod: data.HeatNextPeriod,
			CoolNextPeriod:data.CoolNextPeriod,
			StatusHeat:data.StatusHeat,
			StatusCool:data.StatusCool,
			FanMode:data.FanMode]
		]

		httpPost(params) { response ->
		log.debug "Request was successful, $response.status"
	}
}

def getStatus() 
{
	log.debug "Executing 'getStatus'"
	def today= new Date()
	log.debug "https://rs.alarmnet.com/TotalConnectComfort/Device/CheckDataSession/${settings.honeywelldevice}?_="+today.time

	def params =
	[
		uri: "https://rs.alarmnet.com/TotalConnectComfort/Device/CheckDataSession/${settings.honeywelldevice}",
		headers: 
		[
			'Accept': '*/*',
			'DNT': '1',
			'Accept-Encoding': 'plain',
			'Cache-Control': 'max-age=0',
			'Accept-Language': 'en-US,en,q=0.8',
			'Connection': 'keep-alive',
			'Host': 'rs.alarmnet.com',
			'Referer': 'https://rs.alarmnet.com/TotalConnectComfort/',
			'X-Requested-With': 'XMLHttpRequest',
			'User-Agent': 'Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.95 Safari/537.36',
			'Cookie': data.cookiess	
		],
	]

	httpGet(params) { response ->

		
		log.debug "Request was successful, $response.status"

		
		def curTemp = response.data.latestData.uiData.DispTemperature
		def fanMode = response.data.latestData.fanData.fanMode
		def switchPos = response.data.latestData.uiData.SystemSwitchPosition
		def coolSetPoint = response.data.latestData.uiData.CoolSetpoint
		def heatSetPoint = response.data.latestData.uiData.HeatSetpoint

		def IndoorHumiditySensorAvailable = response.data.latestData.uiData.IndoorHumiditySensorAvailable
		def OutdoorHumidityAvailable = response.data.latestData.uiData.OutdoorHumidityAvailable
		def OutdoorHumidity = response.data.latestData.uiData.OutdoorHumidity
		def IndoorHumidity = response.data.latestData.uiData.IndoorHumidity
		//def outHumidity = response.data.latestData.weather.Humidity
		//def Humidity = outHumidity

		if (OutdoorHumiditySensorAvailable == 'true')
			Humidity = OutdoorHumidity

		if (IndoorHumiditySensorAvailable == 'true')
			Humidity = IndoorHumidity
		
		log.debug curTemp
		log.debug fanMode
		log.debug switchPos
		log.debug outHumidity
 		//fan mode 0=auto, 2=circ, 1=on
		
		if(fanMode==0)
			fanMode = 'auto'
		if(fanMode==2)
			fanMode = 'circulate'
		if(fanMode==1)
			fanMode = 'on'

		if(switchPos==1)
			switchPos = 'heat'
		if(switchPos==2)
			switchPos = 'off'
		if(switchPos==3)
			switchPos = 'cool'
        if(switchPos==5)
			switchPos = 'cool'

		sendEvent(name: 'thermostatFanMode', value: fanMode)
		sendEvent(name: 'thermostatMode', value: switchPos)
		sendEvent(name: 'coolingSetpoint', value: coolSetPoint as Integer)
		sendEvent(name: 'heatingSetpoint', value: heatSetPoint as Integer)
		sendEvent(name: 'temperature', value: curTemp as Integer, state: switchPos)
		sendEvent(name: 'humidity', value: Humidity as Integer)
	}
}

def api(method, args = [], success = {}) 
{
}

// Need to be logged in before this is called. So don't call this. Call api.
def doRequest(uri, args, type, success)
{
}

def refresh()
{
	log.debug "Executing 'refresh'"
	login()
	getStatus()
}

def login()
{  
	log.debug "Executing 'login'"
		
	def params = 
	[
		uri: 'https://rs.alarmnet.com/TotalConnectComfort/',
		headers: 
		[
			'Content-Type': 'application/x-www-form-urlencoded',
			'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
			'Accept-Encoding': 'sdch',
			'Host': 'rs.alarmnet.com',
			'DNT': '1',
			'Origin': 'https://rs.alarmnet.com/TotalComfort/',
			'User-Agent': 'Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.95 Safari/537.36'
		],
		body: 
		[
			timeOffset: '240', 
			UserName: "${settings.username}", 
			Password: "${settings.password}", 
			RememberMe: 'false'
		]
	]

	data.cookiess = ''

	httpPost(params) 
	{ 
		response ->
		log.debug "Request was successful, $response.status"
		response.getHeaders('set-cookie').each 
		{
			String cookie = it.value.split(';|,')[0]
			log.debug "Adding cookie to collection: $cookie"
            if(cookie != ".ASPXAUTH_TH_A=") {
			data.cookiess = data.cookiess+cookie+';'
            }
		}
		log.debug "cookies: $data.cookiess"
	}
}

def isLoggedIn()
{
	if(!data.auth)
	{
		log.debug "No data.auth"
		return false
	}
	
	def now = new Date().getTime();
	return data.auth.expires_in > now
}
