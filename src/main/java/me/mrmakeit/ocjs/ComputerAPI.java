package me.mrmakeit.ocjs;

import java.util.HashMap;
import java.util.Map;

import jdk.nashorn.api.scripting.*;

import li.cil.oc.api.machine.*;

public class ComputerAPI {

  private Machine machine;
  private NashornAPI vm;

  public ComputerAPI(Machine machine, NashornAPI vm) {
    this.machine = machine;
    this.vm = vm;
  }

  public Map<String,String> list() {
    Map<String, String> components = machine.components();
    System.out.println("Getting component list");
    return components;
  }

  public void invoke(String address, String method, Object[] params,InvokeSuccessCallback callback, InvokeErrorCallback error){
    System.out.println("Running "+method+" on "+address);
    try{ 
      Object[] result = machine.invoke(address,method,params);
      if(result==null){
        result = new Object[]{};
      }
      callback.call(result);
    } catch (LimitReachedException e){
      vm.addInvoke(new InvokeCallback(machine,address,method,params,callback,error));
    } catch(Exception e){
      e.printStackTrace();
      error.call(e.getMessage());
    }
  }

  public Object pullSignal(){
    Signal signal = machine.popSignal();
    if(signal==null){
      return null;
    }
    Map<String,Object[]> nobj = new HashMap<String,Object[]>();
    nobj.put(signal.name(),signal.args());
    return nobj;
  }

  public void shutdown(boolean reboot){
    vm.shutdown(reboot);
  }

  public void print(String message){
    System.out.println(message);
  }

  public void onSignal(String name, Object[] args){
  }

  public Object[] error(String message){
    boolean state = machine.crash(message);
    return new Object[]{state};
  }
}
