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
		computer.print(comp);
		computer.print(allComp[comp]);
		if(allComp[comp] == type){
			results.push(comp);
		}
	}
	computer.print(JSON.stringify(results));
	return results;
}

var loadFrom = function(address){
}

var setScreens = function(){
	computer.print("Setting Screens");
	var screen = getComponentList('screen')[0];
	var gpu = getComponentList('gpu')[0];
	if(screen){
		if(gpu){
			computer.invoke(gpu, 'bind', [screen], function(){});
		}
	}
}

var init = function(){
	computer.print("Running BIOS Init");
	setScreens();
	var drives = getComponentList('filesystem')
	if(drives.length>0){
		for(drive in drives){
			address = drives[drive];
			if(loadFrom(address)){
				return;
			}
		}
	}
	computer.error("No Bootable Medium Found");
}

computer.next(init);
computer.direct();
