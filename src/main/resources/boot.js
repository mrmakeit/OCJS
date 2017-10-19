var dec2string = function(arr){
	string = ""
	for(var x in arr){
		string = string + String.fromCharCode(arr[x])
	}
	return string
}

var error = function(text){
	computer.error(text);
}

var getComponentList = function(type){
	var allComp = computer.list();
	var results = []
	for(comp in allComp){
		if(allComp[comp] == type){
			results.push(comp);
		}
	}
	return results;
}

eval = null;

var loadEeprom = function(){
	var eepromList = getComponentList('eeprom');
	if(eepromList.length>0){
		cont = dec2string(computer.invoke(eepromList[0],"get",[])[0]);
		var loadEeprom = undefined
		computer.load(cont,"<bios>");
	}else{
		computer.error("No EEPROM Found");
	}
}

loadEeprom();
