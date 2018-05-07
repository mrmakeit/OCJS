package me.mrmakeit.ocjs;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

import li.cil.oc.api.machine.LimitReachedException;
import li.cil.oc.api.machine.Machine;

class InvokeCallback {
  private String address;
  private String method;
  private Object[] params; 
  private Machine machine;
  private ScriptObjectMirror cb;
  private ScriptObjectMirror error;

  InvokeCallback(Machine machine, String address, String method, Object[] params, ScriptObjectMirror cb, ScriptObjectMirror error) {
    this.machine = machine;
    this.method = method;
    this.address = address;
    this.params = params;
    this.cb = cb;
    this.error = error;
  }

  public void call(NashornAPI vm){
    try{
      Object[] results = machine.invoke(address,method,params);
      cb.call(null,results);
    }catch(LimitReachedException e){
      vm.addInvoke(this);
    }catch(Exception e){
      error.call(null,e.getMessage());
    }
  }
}
