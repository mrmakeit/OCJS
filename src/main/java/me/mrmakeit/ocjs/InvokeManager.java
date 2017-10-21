package me.mrmakeit.ocjs;

import org.mozilla.javascript.Function;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import li.cil.oc.api.machine.LimitReachedException;
import li.cil.oc.api.machine.Machine;

import me.mrmakeit.ocjs.Callback.InvokeCallback;

class InvokeManager {
  Machine machine;
  Scriptable scope;
  InvokeCallback cb=null;

  InvokeManager(Machine machine, Scriptable scope) {
    this.machine = machine;
    this.scope = scope;
  }

  public void runInvokeCallback(Context cx){
    this.cb.call(cx,scope,null);
    this.cb=null;
  }

  public void readyInvokeCallback(String address, String method, Object[] params, Function cb){
    if(this.cb!=null){
      machine.crash("Too many requests for invoke at once.");
      return;
    }
    this.cb = new InvokeCallback(machine,address,method,params,cb);
  }
}
