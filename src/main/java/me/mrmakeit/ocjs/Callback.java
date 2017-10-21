package me.mrmakeit.ocjs;

import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

import li.cil.oc.api.machine.LimitReachedException;
import li.cil.oc.api.machine.Machine;

import org.mozilla.javascript.Context;

class Callback {
  protected Function cb;
  Callback(Function cb){
    this.cb = cb;
  }
  public void call(Context cx, Scriptable scope, Object[] params){
    cb.call(cx,scope,scope,params);
  }

  static class InvokeCallback extends Callback {
    private String address;
    private String method;
    private Object[] params; 
    private Machine machine;
    InvokeCallback(Machine machine, String address, String method, Object[] params, Function cb) {
      super(cb);
      this.address = address;
      this.method = method;
      this.params = params;
      this.machine = machine;
    }
    @Override
    public void call(Context cx, Scriptable scope, Object[] params){
      Object[] results = null;
      try{
        results = machine.invoke(address,method,this.params);
        this.cb.call(cx,scope,scope,results);
      } catch(Exception e){
        Object[] except = {e.getMessage()};
        results = except.clone();
        this.cb.call(cx,scope,scope,results);
      }
    }
  }
}
