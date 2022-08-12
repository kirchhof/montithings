${tc.signature("componentName", "protobufModule", "inPorts", "outPorts")}

from IComputable import IComputable, GenericResult, GenericInput
from ${protobufModule} import *
# TODO: get unlock.FaceUnlock parameter

class ${componentName}Input(GenericInput):
    # protobuf already provides methods respectively
    # reference: https://developers.google.com/protocol-buffers/docs/pythontutorial - last visited 12.8.22
    # if unclear, use help(xInput.ports[port_name]) or dir(xInput.ports[port_name])
    # note that because the mapping for ports is dynamic (MQTT instructions), a mapping at runtime is much harder in
    # static languages. Therefore the mapping is done with a dict and the port-name
    def __init__(self):
        ports = {}
<#list inPorts as port>
        ports["${port.name}"] = ${port.type.getTypeInfo().name}()
</#list>

class ${componentName}Result(GenericResult):
    # protobuf already provides methods respectively
    # reference: https://developers.google.com/protocol-buffers/docs/pythontutorial - last visited 12.8.22
    # if unclear, use help(xResult.port_name) or dir(xResult.port_name)
    def __init__(self):
        self.uuid = uuid4()
        ports = {}
<#list outPorts as port>
        ports["${port.name}"] = ${port.type.getTypeInfo().name}()
</#list>

class ${componentName}ImplTOP(IComputable, MQTTConnector):
    # convenience dicts to lookup ports and their respective protobuf-types
    COMPONENT_PORTS_IN = {
<#list inPorts as port>
        "${port.name}": ${port.type.getTypeInfo().name},
</#list>
    }
    COMPONENT_PORTS_OUT = {
<#list outPorts as port>
        "${port.name}": ${port.type.getTypeInfo().name},
</#list>
    }

    _input = ${componentName}Input()
    _result = ${componentName}Result()
    serialize = lambda x: b64encode(x.serializeToString()).decode("UTF-8")
    deserialize = lambda x: b64decode(x)

    # MQTTConnector implementation

    ports_in = set()
    ports_out = set()
    # after startup the component will receive instructions on which topics a port should listen to
    # especially: one port may listen to several topics
    connectors = {}

    def __init__(self, client_id, **kwargs) -> None:
        self.client_id = client_id # call the constructor of this ImplTOP in your __init__ and set the client_id
        for port in COMPONENT_PORTS_IN.keys():
            ports_in.add(".".join([client_id, port]))
        super().__init__(client_id=client_id, **kwargs)

    def on_message(self, client, userdata, message) -> None:
        decoded_msg = message.payload.decode("utf-8")
        port = {message.topic.split("/")[-1]}
        if message.topic.startswith("/connectors/"): # TODO: only subscribe on correct connectors
            topic = f"/ports/{decoded_msg}".replace(".", "/")
            print(port, "now listening on", topic)
            self.subscribe(topic, qos=0)
            self.connectors[topic] = self._input.ports[port]
        else:
            payload_msg = deserialize(json.loads(decoded_msg)["value0"]["payload"]["data"]) # b64decode payload
            payload_uuid = json.loads(decoded_msg)["value0"]["uuid"]
            if self.connectors.get(message.topic, False):
                self.published_on_port = port # possibly racy, when compute is not finished before next message enters
                self.compute(self.connectors[message.topic].deserializeFromString(payload_msg), payload_uuid)
            else:
                print(f"Received unroutable message on topic {message.topic}")

    def on_connect(self, client, obj, flags, rc) -> None:
        connect = super().on_connect(client, obj, flags, rc)
        # TODO: handle getInitialValues

    # MQTT publish ports
<#list outPorts as port>
    def send_port_${port.name}(self) -> None:
        """publish the current value of _result.${port.name} to MQTT:/ports/...
        Use this in your hand-written-code to publish to the port ${port.name}"""
        self.publish(
            ".".join([self.client_id, "${port.name}"]),
            _result.ports["${port.name}"]
        )
</#list>