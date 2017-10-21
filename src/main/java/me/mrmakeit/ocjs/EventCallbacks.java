package me.mrmakeit.ocjs;

import org.mozilla.javascript.Function;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import li.cil.oc.api.machine.Signal;
import li.cil.oc.api.machine.Machine;

class LoopCallback {
  Function callback;
  LoopCallback(Function callback){
    this.callback = callback;
  }
  public void runCallback(Context cx, Scriptable scope){
    callback.call(cx,scope,scope,null);
  }
  static class EventCallback extends LoopCallback {
    Machine machine;
    EventCallback(Function callback, Machine machine){
      super(callback);
      this.machine = machine;
    }
    @Override
    public void runCallback(Context cx, Scriptable scope){
      Signal signal = machine.popSignal();
      Object[] args = {signal.name(),signal.args()};
      callback.call(cx,scope,scope,args);
    }
    public void runCallback(Context cx, Scriptable scope,Signal signal){
      Object[] args = {signal.name(),signal.args()};
      callback.call(cx,scope,scope,args);
    }
  }

  static class InvokeCallback extends LoopCallback {
    private String address;
    private String method;
    private Object[] params;
    private Machine machine;
    InvokeCallback(Function callback, Machine machine, String address ,String method, Object[] params){
      super(callback);
      this.address=address;
      this.method=method;
      this.params=params;
      this.machine = machine;
    }
    public void runCallback(Context cx, Scriptable scope){
      Object[] results=null;
      try{
        results = machine.invoke(address,method,params);
      }catch(Exception e){
        Object[] res = {e.getMessage()};
        results = res.clone();
      }finally{
        callback.call(cx,scope,scope,results);
      }
    }
  }
}
