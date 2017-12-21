var printToConsole = false;
var dec2string = function (arr) {
    string = "";
    for (var x in arr) {
        if (printToConsole) computer.print(arr[x]);
        string = string + String.fromCharCode(arr[x]);
    }
    return string;
}

var error = function(text){
    computer.error(text);
}

var getComponentList = function(type){
    var allComp = computer.list();
    var results = [];
    for (comp in allComp) {
        if (printToConsole) {
            computer.print(comp);
            computer.print(allComp[comp]);
        }
        if(allComp[comp] == type) results.push(comp);
    }
    if (printToConsole) computer.print(JSON.stringify(results));
    return results;
}

var loadFrom = function(address, success){
    computer.invoke(address, 'open', ['/init.js'],function(handle){
        if (!handle) return;
        var buffer = "";
        var readData = function(results){ 
            if(results){
                var data = dec2string(results);
                buffer = buffer + data;
                computer.invoke(address,"read",[handle, Number.MAX_VALUE],readData,function(error){});
            }else{
                computer.invoke(address,'close', [handle], function(){
                    if (printToConsole) computer.print(buffer);
                    success();
                    eval(buffer); 
                }, () => {});
            }
        }
        computer.invoke(address,"read",[handle, Number.MAX_VALUE],readData,function(error){});
    },function(error){});
}

var setScreens = function(){
    if (printToConsole) computer.print("Setting Screens");
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
    if (printToConsole) computer.print("Running BIOS Init");
    setScreens();
    var drives = getComponentList('filesystem');
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
