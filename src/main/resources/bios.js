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

console = {
  line: 0,
  log: function(text){
    //text = JSON.stringify(text);
    gpu = getComponentList('gpu')[0];
    component.invoke(gpu, 'set', [0,console.line,text]);
  }
}

var loadFrom = function(address){
	console.log(address);
  console.log(component.invoke(address, 'list', ['']));
	var handle = component.invoke(address, 'open', ['/init.js']);
	if(!handle){
    console.log("No File");
		return false;
	}
  console.log(handle);
	var buffer = "";
	var data = "";
	do{
		data = component.invoke(address, "read", [handle, Number.MAX_VALUE]);
    console.log(data);
		if(data){
			buffer = buffer + data
		}
	}while(data);
	component.invoke(address,'close', [handle]);
  console.log("Got Buffer");
  console.log(buffer);
	component.load(buffer,'/init.js');
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
