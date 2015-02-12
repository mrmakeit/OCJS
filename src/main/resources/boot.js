function event(){
};

function list(name){
	if(name){
		components = {}
		allComp=computer.list();
		for(comp in allComp){
			if(allComp[comp].indexOf(name)>=0){
				components[comp] = allComp[comp];
				out.println(comp);
			}
		}
	}else{
		components = computer.list();
	}
	return components;
}

function getEepromAddr(){
	comps = list('eeprom')
	for(var k in comps){
		return k
	}
}

function printObject(obj){
	for(var k in obj){
		out.println(k);
		out.println(obj[k]);
	}
}
function dec2string(arr){
	string = ""
	out.println(arr.length)
	for(var x in arr){
		string = string + String.fromCharCode(arr[x])
	}
	return string
}
function loadEeprom(){
	cont = dec2string(computer.invoke(getEepromAddr(),"get",[])[0])
	out.println(cont);
	eval(cont);
}
out.println(computer.invoke(getEepromAddr(),"get",[])[0])
out.println(dec2string(computer.invoke(getEepromAddr(),"get",[])[0]))

loadEeprom()
