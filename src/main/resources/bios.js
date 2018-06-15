function decodeRead(arr){
	var string = "";
	for(var x in arr){
		string = string + String.fromCharCode(arr[x]);
	}
	return string;
}

function getComponentList(type){
	var allComp = computer.list();
	var results = [];
	for(var comp in allComp){
		if(allComp[comp] == type){
			results.push(comp);
		}
	}
	return results;
}

function loadFrom(address, success){
  computer.invoke(address, 'open', ['/init.js'],function(handle){
    if(!handle){
      return;
    }
    var buffer = "";
    var readData = function(results){ 
      if(results){
        var data = decodeRead(results);
        buffer = buffer + data;
        computer.invoke(address,"read",[handle, Number.MAX_VALUE],readData,function(error){});
      }else{
        computer.invoke(address,'close', [handle], function(){
          success();
          eval(buffer); 
        }, function(){});
      }
    }
    computer.invoke(address,"read",[handle, Number.MAX_VALUE],readData,function(error){});
  },function(error){
  });
}

function setScreens(){
	var screen = getComponentList('screen')[0];
	var gpu = getComponentList('gpu')[0];
	if(screen){
		if(gpu){
      computer.invoke(gpu, 'bind', [screen],function(_){},function(_){});
		}
	}
}

function init(){
  if(!runOne){
    if(ready){
    }else{
      computer.error("No Bootable Medium Found");
    }
  }
  runOne = false;
	setScreens();
	var drives = getComponentList('filesystem')
	if(drives.length>0){
		for(var drive in drives){
			var address = drives[drive];
			loadFrom(address,function(){
      })
		}
	}
}

var runOne = true;
var ready = false;
init();
