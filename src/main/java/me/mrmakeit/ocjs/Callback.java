package me.mrmakeit.ocjs;

import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

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
    InvokeCallback(Machine machine, String address, String method, Object[] params, Function cb) {
      super(cb);
      this.address = address;
      this.method = method;
      this.params = params;
    }
    @Override
    public void call(Context cx, Scriptable scope, Object[] params){
      Function invoke = (Function)scope.get("machine.invoke",scope);
      invoke.call(cx,scope,scope,new Object[] {address,method,this.params,cb});
    }
  }
}
