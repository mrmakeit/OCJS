package me.mrmakeit.ocjs;

import li.cil.oc.api.machine.LimitReachedException;
import li.cil.oc.api.machine.Machine;

import jdk.nashorn.api.scripting.JSObject;

class InvokeCallback {
  private String address;
  private String method;
  private Object[] params; 
  private Machine machine;
  protected JSObject cb;
  private JSObject error;

  InvokeCallback(Machine machine, String address, String method, Object[] params, JSObject cb, JSObject error) {
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
