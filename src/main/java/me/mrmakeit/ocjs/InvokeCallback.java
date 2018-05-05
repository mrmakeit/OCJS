package me.mrmakeit.ocjs;

import li.cil.oc.api.machine.LimitReachedException;
import li.cil.oc.api.machine.Machine;

class InvokeCallback {
  private String address;
  private String method;
  private Object[] params; 
  private Machine machine;
  private InvokeSuccessCallback cb;
  private InvokeErrorCallback error;

  InvokeCallback(Machine machine, String address, String method, Object[] params, InvokeSuccessCallback cb, InvokeErrorCallback error) {
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
      cb.call(results);
    }catch(LimitReachedException e){
      vm.addInvoke(this);
    }catch(Exception e){
      error.call(e.getMessage());
    }
  }
}
