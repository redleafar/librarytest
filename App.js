import React, { Component } from 'react';
import { NativeModules, NativeEventEmitter, Text, Button, View, Switch } from 'react-native';

const { SeapassReaderModule } = NativeModules;

const DEFAULT_FOLIO_NUMBER = "9841000061609601"
const DEFAULT_DEBARK_DATE = "20180517"
const DEFAULT_SECONDARY_FOLIO = "26542751"
const DEFAULT_LOYALTY_TIER_CODE = "L"
const DEFAULT_SHIP_CODE = "OA"
const DEFAULT_MUSTER_STATION_NUMBER = "C3"
const DEFAULT_EMBARK_DATE = "20140503"
const DEFAULT_FIRST_NAME = "Andres"
const DEFAULT_LAST_NAME = "Smith"
const DEFAULT_MIDDLE_NAME = "Anthony"

class App extends Component {  
  
constructor(props) {
  super(props);
  this.state = {
    toggle: false,
  }
}

componentDidMount() {
  const eventEmitter = new NativeEventEmitter(NativeModules.SeapassReaderModule);
  this.eventListener = eventEmitter.addListener('read', (event) => {
     console.log(event)     
  });

  this.eventListener = eventEmitter.addListener('tagdetected', (event) => {
     if (this.state.toggle == false) {
      SeapassReaderModule.read()     
     } else {
      SeapassReaderModule.write(
        DEFAULT_FOLIO_NUMBER,
        DEFAULT_DEBARK_DATE,
        DEFAULT_SECONDARY_FOLIO,
        DEFAULT_LOYALTY_TIER_CODE,
        DEFAULT_SHIP_CODE,
        DEFAULT_MUSTER_STATION_NUMBER,
        DEFAULT_EMBARK_DATE,
        DEFAULT_FIRST_NAME,
        DEFAULT_LAST_NAME,
        DEFAULT_MIDDLE_NAME        
      )
     }
 });
}

componentWillUnmount() {
  this.eventListener.remove();
}

 render() {
  return (
    <View>
      <Text>Seapass Card reader demo</Text>
      <Text>Move the swith Left for reading, move it right for writing, then put the tag near the device and check the console</Text>
      <Switch
        trackColor={{ false: "#767577", true: "#81b0ff" }}
        thumbColor="white"
        ios_backgroundColor="#3e3e3e"
        onValueChange={(value) => this.setState({toggle: value})}
        value={this.state.toggle}
      />
    </View>    
  )
 } 
}

export default App;
