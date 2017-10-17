var dec2string = function(arr){
	string = ""
	for(var x in arr){
		string = string + String.fromCharCode(arr[x])
	}
	return string
}

var error = function(text){
	component.error(text);
}

var getComponentList = function(type){
	var allComp = component.list();
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
	var eeprom = getComponentList('eeprom')[0];
	cont = dec2string(component.invoke(eeprom,"get",[])[0]);
	var loadEeprom = undefined
	component.load(cont,"test");
}

loadEeprom();
