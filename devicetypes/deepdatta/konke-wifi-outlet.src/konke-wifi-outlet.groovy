/**
 *  Konke WiFi Outlet
 *
 *  Copyright 2017 Deep Datta
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
 preferences {
    input("ip", "string", title:"IP Address", description: "ip of the outlet", defaultValue: "10.11.12.11" ,required: true, displayDuringSetup: true)
 }
 
metadata {
	definition (name: "Konke WiFi Outlet", namespace: "deepdatta", author: "Deep Datta") {
		capability "Switch"
        capability "Refresh"
        capability "Polling"
	}


	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
		// TODO: define your main and details tiles here
      	standardTile("switch", "device.switch") {
    		// use the state name as the label ("off" and "on")
    		state "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff"
    		state "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#00a0dc"
		}
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main "switch"
		details(["switch","refresh"])
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	// TODO: handle 'checkInterval' attribute
	// TODO: handle 'DeviceWatch-DeviceStatus' attribute
	// TODO: handle 'switch' attribute

	def    msg  = parseLanMessage(description)
    String resp = msg.body.toString().trim()
    log.debug "resp: ${resp}"
    
    def event = createEvent(name: "switch", value: "off")
    if (resp == 'OK') {
    	log.debug "Set command was Successful!"
        event.value = device.currentValue("switch")
    } else if (resp == 'on') {
    	log.debug "Outlet is On"
        event.value = "on"
	} else if (resp == 'off') {
    	log.debug "Outlet is Off"
    } else {
    	log.debug "Set command failed!"
    }
    return event
}

// handle commands
def initialize() {
	log.debug "Executing 'installed'"
    refresh()
}

def refresh() {
	log.debug "Executing 'refresh'"
    setDeviceNetworkId(ip, 80)
	// TODO: handle 'refresh' command
    send_cgi_comms("state")
}

def off() {
	log.debug "Executing 'off'"
	// TODO: handle 'off' command
    sendEvent(name: "switch", value: "off")
    send_cgi_comms("off")
}

def on() {
	log.debug "Executing 'on'"
	// TODO: handle 'on' command
    sendEvent(name: "switch", value: "on")
    send_cgi_comms("on")
}

def polling() {
	refresh()
}

// Helper utils
private def send_cgi_comms(command) {
	def result = new physicalgraph.device.HubAction(
        method: "GET",
        path: "/cgi-bin/lightswitch.cgi?${command}",
        headers: [
            HOST:"${ip}:80"
        ]
    )
    log.debug "Executing hubAction '${command}' on ${ip}"
    return result
}

private setDeviceNetworkId(ip, port){
	def iphex = convertIPtoHex(ip)
    def porthex = convertPortToHex(port)
    def hexVal = "$iphex:$porthex"
  	device.deviceNetworkId = "${hexVal}"
  	//log.debug "Device Network Id set to ${hexVal}" //:${porthex}"
    return hexVal
}

private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04x', port.toInteger() )
    return hexport
}

private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    return hex
}