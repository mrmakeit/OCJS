/* This is the non-JSDOC Version */
var _computer = Object(computer);

function getCompList(type) {
    var allComp = computer.list();
    var results = [];
    for (var comp in allComp) {
        if (allComp[comp] == type) {
            results.push(comp);
        }
    }
    return results;
}

function setScreens(cb, addr) {
    if (typeof addr != 'string') addr = null;
    var gpu = getCompList('gpu')[0];
    function set() {
        if (!addr) var addr = getCompList('screen')[0];
        computer.invoke(gpu, 'bind', [addr], function (_) {
            cb();
        }, function (err) {
            cb(err);
        });
    }
    if (addr) set();
    else {
        computer.invoke(gpu, 'getScreen', [], function (address) {
            if (!address || address.trim() == '') set();
            else cb();
        }, function (err) {
            cb(err);
        });
    }
}

function scaninit(cb) {
    var drives = getCompList('filesystem');
    if (drives.length <= 0) computer.error('no filesystems installed!');
    var index = -1;
    saw(function (cb) {
        index++;
        if (index <= drives.length - 1) {
            computer.invoke(drives[index], 'exists', ["/init.js"], function (b) { //clear lowest line
                if (b) {
                    cb(false, drives[index]);
                } else cb(true);
            }, function (_) {
                computer.print('somehow something errored in write');
            });
        } else cb(false, null);
    }, function (back) {
        if (!back) {
            computer.error('No init.js found!');
        }

        cb(back);
    });
}

function saw(func, end) {
    func(function (again, back) {
        if (again) {
            saw(func, end);
        } else end(back);
    });
}

function decodeRead(arr) {
    var string = "";
    for (var x in arr) {
        string += String.fromCharCode(arr[x]);
    }
    return string;
}

setScreens(function (err) {
    if (err) computer.error(err);
    var gpu = getCompList('gpu');
    computer.invoke(gpu, 'getResolution', [], function (x, y) {
        computer.invoke(gpu, 'setBackground', [0], function () {
            computer.invoke(gpu, 'fill', [1, 1, x, y, " "], function () {
                scaninit(function (addr) {
                    computer.invoke(addr, 'open', ['/init.js'], function (handle) {
                        var buffer = '';
                        function readData(results) {
                            if (results) {
                                buffer += decodeRead(results);
                                computer.invoke(addr, "read", [handle, Number.MAX_VALUE], readData, function (err) { });
                            } else {
                                computer.invoke(addr, 'close', [handle], function () {
                                    eval(buffer);
                                }, function () { });
                            }
                        }
                        computer.invoke(addr, "read", [handle, Number.MAX_VALUE], readData, function (err) { });
                    }, function () {
                        computer.print('Cloud not open "init.js" on ' + addr);
                    });
                });
            }, function () {
                computer.print('Something went wrong at clearing the screen!');
            });
        }, function () {
            computer.print('Something went wrong at setting the background-color');
        });
    }, function () {
        computer.print('Something went wrong at getting the Resolution!');
    });
});
