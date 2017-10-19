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

var loadFrom = function(address){
	var handle = computer.invoke(address, 'open', ['/init.js']);
	if(!handle){
		return false;
	}
	var buffer = "";
	var data = "";
	do{
		data = computer.invoke(address, "read", [handle, Number.MAX_VALUE]);
		if(data){
			buffer = buffer + data
		}
	}while(data);
	computer.invoke(address,'close', [handle]);
	error(buffer);
	computer.load(buffer,'/init.js');
}

var setScreens = function(){
	var screen = getComponentList('screen')[0];
	var gpu = getComponentList('gpu')[0];
	if(screen){
		if(gpu){
			computer.invoke(gpu, 'bind', [screen]);
		}
	}
}

var init = function(){
	setScreens();
	var drives = getComponentList('filesystem')
	if(drives.length>0){
		for(drive in drives){
			address = drives[drive];
			loadFrom(address);
		}
	}
	error("No Bootable Medium Found");
}

init();
