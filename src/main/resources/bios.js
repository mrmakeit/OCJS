var getComponentList = function(type){
	var allComp = component.list();
	var results = []
	for(comp in allComp){
		if(allComp[comp] == type){
			out.println(comp);
			results.push(comp);
		}
	}
	return results;
}

var loadFrom = function(address){
	out.println(address);
	var handle = component.invoke(address, 'open', ['/init.js']);
	if(!handle){
		return false;
	}
	var buffer = "";
	var data = "";
	do{
		data = component.invoke(address, "read", [handle, Number.MAX_VALUE]);
		if(data){
			buffer = buffer + data
		}
	}while(data);
	component.invoke(address,'close', [handle]);
	eval(buffer);
}

var setScreens = function(){
	var screen = getComponentList('screen')[0];
	var gpu = getComponentList('gpu')[0];
	if(screen){
		if(gpu){
			component.invoke(gpu, 'bind', [screen]);
		}
	}
}

var init = function(){
	setScreens();
	var drives = getComponentList('filesystem')
	if(drives.length>0){
		drives.forEach(function(address){
			loadFrom(address);
		});
	}
	error('No Bootable Medium Found!');
}

init();
