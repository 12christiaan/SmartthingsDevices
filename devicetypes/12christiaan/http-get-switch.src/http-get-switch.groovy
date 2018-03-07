/*
* 	Author: C Vermeulen
* 	Device Handler via HTTP GET.
* 	I made this device handler for direct integration of RFlink (without any other equipment dependancy)
* 	Smartthings hub <--> ESP8266 <--> RFLink
* 
* 
* 
* 	ESP8266 software:	i used ESPeasy from here https://www.letscontrolit.com/wiki/index.php/ESPEasy
* 	RFLink info:	http://www.rflink.nl
*	Inspiered by "URI Switch" from Authors: tguerena & surge919, which you can find here: https://raw.githubusercontent.com/tguerena/SmartThings/master/devicetypes/tguerena/uriswitch.src/uriswitch.groovy
* 
*/


preferences {
	section("External Access"){
		input "external_on_uri", "text", title: "External On URI", required: false
		input "external_off_uri", "text", title: "External Off URI", required: false
	}
    
	section("Internal Access"){
		input "internal_ip", "text", title: "Internal IP", required: false
		input "internal_port", "text", title: "Internal Port (if not 80)", required: false
		input "internal_on_path", "text", title: "Internal On Path (/blah?q=this)", required: false
		input "internal_off_path", "text", title: "Internal Off Path (/blah?q=this)", required: false
	}
}




metadata {
	definition (name: "HTTP get Switch", namespace: "12christiaan", author: "C Vermeulen") {
		capability "Actuator"
			capability "Switch"
			capability "Sensor"
	}

	// simulator metadata
	simulator {
	}

	// UI tile definitions
	tiles {
		standardTile("button", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "off", label: 'Off', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "on"
				state "on", label: 'On', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821", nextState: "off"
		}
		standardTile("offButton", "device.button", width: 1, height: 1, canChangeIcon: true) {
			state "default", label: 'Force Off', action: "switch.off", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
		}
		standardTile("onButton", "device.switch", width: 1, height: 1, canChangeIcon: true) {
			state "default", label: 'Force On', action: "switch.on", icon: "st.switches.switch.on", backgroundColor: "#79b821"
		}
		main "button"
			details (["button","onButton","offButton"])
	}
}

def parse(String description) {
	log.debug(description)
}


def on() {
	if (external_on_uri){
		log.debug "Executing ON (external)" 
		sendEvent(name: "switch", value: "on")
    	SendCmdViaCloud("${settings.external_on_uri}")
	}
	if (internal_off_path){
		log.debug "Executing ON (internal)" 
		sendEvent(name: "switch", value: "on")
        SendCmdViaHub("$internal_on_path")
	}
}

def off() {
	if (external_off_uri){
		log.debug "Executing OFF (external)" 
		sendEvent(name: "switch", value: "off")
    	SendCmdViaCloud("${settings.external_off_uri}")
	}
	if (internal_off_path){
		log.debug "Executing OFF (internal)" 
		sendEvent(name: "switch", value: "off")
        SendCmdViaHub("$internal_off_path")
	}
}

private SendCmdViaCloud(String path){
	//def cmd = "${settings.external_off_uri}";
	log.debug "Sending request cmd[${path}]"
	httpGet(path) {resp ->
		if (resp.data) {
			log.info "${resp.data}"
		} 
	}
}

private SendCmdViaHub(String path){
	def DevicePort
	if (internal_port){
		DevicePort = "${internal_port}"
	} else {
		DevicePort = 80
	}
    //
	def headers = [:] 
    headers.put("HOST", "$internal_ip:$DevicePort")
    headers.put("Accept", "text/html")		//
    log.debug "headers: $headers" 
    //
    //def path
    //log.debug "path: $path" 
    path = "$path".replaceAll(/ /,"%20")
    path = "$path".replaceAll(/,/,"%2C")
    log.debug "path: $path" 
    //
	sendHubCommand(new physicalgraph.device.HubAction(
		method: "GET",
		path: "${path}",
		headers: headers
		))
	// Done
}


