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
}

metadata {
	definition (name: "Kotkot RC Switch", namespace: "smartthings", author: "Deep Datta") {
		capability "Switch"
        capability "Refresh"
		capability "Configuration"
		
		(1..3).each { n ->
			attribute "switch$n", "enum", ["on", "off"]
			command "on$n"
			command "off$n"
			command "toggle$n"
		}
	}

	// simulator metadata
	simulator {
		status "on":  "command: 2003, payload: FF"
		status "off":  "command: 2003, payload: 00"
		status "switch1 on": "command: 600D, payload: 01 00 25 03 FF"
		status "switch1 off": "command: 600D, payload: 01 00 25 03 00"
		status "switch4 on": "command: 600D, payload: 04 00 25 03 FF"
		status "switch4 off": "command: 600D, payload: 04 00 25 03 00"
		status "power": new physicalgraph.zwave.Zwave().meterV1.meterReport(
		        scaledMeterValue: 30, precision: 3, meterType: 4, scale: 2, size: 4).incomingMessage()
		status "energy": new physicalgraph.zwave.Zwave().meterV1.meterReport(
		        scaledMeterValue: 200, precision: 3, meterType: 0, scale: 0, size: 4).incomingMessage()
		status "power1": "command: 600D, payload: 0100" + new physicalgraph.zwave.Zwave().meterV1.meterReport(
		        scaledMeterValue: 30, precision: 3, meterType: 4, scale: 2, size: 4).format()
		status "energy2": "command: 600D, payload: 0200" + new physicalgraph.zwave.Zwave().meterV1.meterReport(
		        scaledMeterValue: 200, precision: 3, meterType: 0, scale: 0, size: 4).format()

		// reply messages
		reply "2001FF,delay 100,2502": "command: 2503, payload: FF"
		reply "200100,delay 100,2502": "command: 2503, payload: 00"
	}

	// tile definitions
	tiles(scale: 2){
    	standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		(1..3).each { n ->
			standardTile("switch$n", "switch$n", canChangeIcon: true, width: 2, height: 2) {
				state "toggle", label: "toggle", action: "toggle$n", icon: "st.switches.switch.on", backgroundColor: "#00a0dc"
			}
		}

		main(["switch1", "switch2", "switch3"])
		details(["switch1", "switch2", "switch3"])
	}
}


def parse(String description) {
	def msg  = parseLanMessage(description)
    def respjson = parseJson(msg.body)
    
    log.debug "Resp JSON '${respjson}'"
}

def toggleCmd(rc_code) {
	def result = new physicalgraph.device.HubAction(
        method: "GET",
        path: "/rcSwitch/${rc_code}",
        headers: [
            HOST:"${PiHubIP}:3000"
        ]
    )
    log.debug "Executing rc_code '${rc_code}' on ${PiHubIP}"
    return result
}

def toggle1() { toggleCmd(873921) }
def toggle2() { toggleCmd(873777) }
def toggle3() { toggleCmd(873741) }

def configure() {
	log.debug "configure() invoked"
}

def refresh() {
	log.debug "Executing 'refresh'"
    setDeviceNetworkId(PiHubIP, 3000)
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