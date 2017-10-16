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
		capability "Health Check"
		capability "Outlet"
	}


	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
		// TODO: define your main and details tiles here
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	// TODO: handle 'checkInterval' attribute
	// TODO: handle 'DeviceWatch-DeviceStatus' attribute
	// TODO: handle 'switch' attribute

	def msg = parseLanMessage(description)
    log.debug "msg: ${msg}"
    
    if (msg.body == 'OK') {
    	log.debug "Set command was Successful!"
    } else if (msg.body == 'on') {
    	log.debug "Outlet is On"
   		sendEvent(name: "switch", value: "on")
	} else if (msg.body == 'off') {
    	log.debug "Outlet is Off"
   		sendEvent(name: "switch", value: "off")
    } else {
    	log.debug "Set command failed!"
    }
}

// Helper utils
def send_cgi_comms(command) {
	def send_cgi_commsAction = new physicalgraph.device.HubAction(
        method: "GET",
        path: "/cgi-bin/lightswitch.cgi?${command}",
        headers: [
            HOST:"${ip}:80"
        ]
    )
    log.debug("Executing hubAction on ${addr}") // + getOutletAddress())
    send_cgi_commsAction 
}

// handle commands
def ping() {
	log.debug "Executing 'ping'"
	// TODO: handle 'ping' command
    send_cgi_comms("state")
}

def off() {
	log.debug "Executing 'off'"
	// TODO: handle 'off' command
    send_cgi_comms("off")
    ping()  
}

def on() {
	log.debug "Executing 'on'"
	// TODO: handle 'on' command
    send_cgi_comms("on")
    ping()
}
