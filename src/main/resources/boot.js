var dec2string = function(arr){
	string = ""
	for(var x in arr){
		string = string + String.fromCharCode(arr[x])
	}
	out.println(string);
	return string
}

var error = function(text){
	component.error(text);
}

var loadEeprom = function(){
	var eeprom = null;
	allComp=component.list();
	for(comp in allComp){
		if(allComp[comp]=="eeprom"){
			eeprom = comp;
			break;
		}
	}
	out.println(eeprom);
	out.println(component.invoke(eeprom,"get",[]));
	cont = dec2string(component.invoke(eeprom,"get",[])[0]);
	var loadEeprom = undefined
	out.println(cont);
	eval(cont);
}


loadEeprom()
