/**
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
	input("PiHubIP", "string", title:"IP Address", description: "ip of the PiHub", defaultValue: "10.11.12.20" ,required: true, displayDuringSetup: true)
    input("PiHubPort", "string", title:"Port", description: "port number of the PiHub server", defaultValue: "3000" ,required: true, displayDuringSetup: true)
}

metadata {
	definition (name: "Kotkot RC Switch", namespace: "deepdatta", author: "Deep Datta") {
		capability "Switch"
		
		(1..3).each { n ->
			attribute "switch$n", "enum", ["on", "off"]
			command "toggle$n"
		}
	}

	// simulator metadata
	simulator {
		
	}

	// tile definitions
	tiles (scale:2) {
    	(1..3).each { n ->
			standardTile("switch$n", "switch$n", canChangeIcon: true, width: 2, height: 2) {
				state "toggle", label: "T$n", action: "toggle$n", icon: "st.switches.switch.on", backgroundColor: "#00a0dc"
			}
		}

		main(["switch1", "switch2", "switch3"])
		details(["switch1", "switch2", "switch3"])
	}
}


def parse(String description) {
	//log.debug "parse(${description})"
	def msg  = parseLanMessage(description)
    def respjson = parseJson(msg.body)
    if (respjson['code'] != "OK") {
    	log.debug "RespCode ${respjson['code']}"
	}
}

def toggleCmd(rc_code) {
	def result = new physicalgraph.device.HubAction(
        method: "GET",
        path: "/rcSwitch/${rc_code}",
        headers: [
            HOST:"${PiHubIP}:${PiHubPort}"
        ]
    )
    log.debug "Executing rc_code '${rc_code}' on ${PiHubIP}:${PiHubPort}"
    return result
}

def toggle1() { toggleCmd(873921) }
def toggle2() { toggleCmd(873777) }
def toggle3() { toggleCmd(873741) }

def updated() {
	log.debug "updated() invoked"
    setDeviceNetworkId(PiHubIP, PiHubPort)
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