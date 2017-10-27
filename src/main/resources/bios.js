var dec2string = function(arr){
	string = ""
	for(var x in arr){
    computer.print(arr[x]);
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

var loadFrom = function(address, success){
  computer.invoke(address, 'open', ['/init.js'],function(handle){
    if(!handle){
      return;
    }
    var buffer = "";
    var readData = function(results){ 
      if(results){
        var data = dec2string(results);
        buffer = buffer + data;
        computer.invoke(address,"read",[handle, Number.MAX_VALUE],readData,function(error){});
      }else{
        computer.invoke(address,'close', [handle], function(){
          computer.print(buffer);
          success();
          eval(buffer); 
        }, function(){});
      }
    }
    computer.invoke(address,"read",[handle, Number.MAX_VALUE],readData,function(error){});
  },function(error){});
}

var setScreens = function(){
	computer.print("Setting Screens");
	var screen = getComponentList('screen')[0];
	var gpu = getComponentList('gpu')[0];
	if(screen){
		if(gpu){
			computer.invokeSync(gpu, 'bind', [screen]);
		}
	}
}

var init = function(){
  if(!runOne){
    if(ready){
    }else{
      computer.error("No Bootable Medium Found");
    }
  }
  runOne = false;
	computer.print("Running BIOS Init");
	setScreens();
	var drives = getComponentList('filesystem')
	if(drives.length>0){
		for(drive in drives){
			address = drives[drive];
			loadFrom(address,function(){
        ready = true;
      })
		}
	}
}

var runOne = true;
var ready = false;
computer.next(init);
computer.direct();
