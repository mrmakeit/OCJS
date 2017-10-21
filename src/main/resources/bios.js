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
	try{
		var handle = computer.invoke(address, 'open', ['/init.js'])[0];
	}catch(e){
		return false;
	}
	if(!handle){
		return false;
	}
	computer.print(handle);
	var buffer = "";
	var data = "";
	do{
		data = computer.invoke(address, "read", [handle, Number.MAX_VALUE])[0];
		computer.print(data);
		if(data){
			buffer = buffer + data
		}
	}while(data);
	computer.invoke(address,'close', [handle]);
	computer.error(buffer);
	computer.load(buffer,'/init.js');
}

var setScreens = function(){
	computer.print("Setting Screens");
	var screen = getComponentList('screen')[0];
	var gpu = getComponentList('gpu')[0];
	if(screen){
		if(gpu){
			computer.invoke(gpu, 'bind', [screen]);
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
			loadFrom(address);
		}
	}
	computer.error("No Bootable Medium Found");
}

computer.next(init);
computer.direct();
