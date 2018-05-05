package me.mrmakeit.ocjs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import delight.nashornsandbox.*;

import jdk.nashorn.api.scripting.JSObject;

import li.cil.oc.api.machine.*; 
public class NashornAPI {

  Machine machine;
  NashornSandbox sandbox;
  State state;
  boolean reboot = false;
  List<InvokeCallback> invokeList = new ArrayList<InvokeCallback>();

  public boolean initialized = false;
  
  public NashornAPI(Machine m) {
    machine = m;
    sandbox = NashornSandboxes.create();
    //sandbox.setMaxCPUTime(200);
    sandbox.setExecutor(Executors.newSingleThreadExecutor());
    sandbox.inject("computer", new ComputerAPI(machine,this));
  }
  
  public void runSync() {
  }

  public void addInvoke(InvokeCallback cb){
    if(state == State.SLEEP){
      invokeList.add(cb);
      state = State.INVOKE;
    }
  }

  public void shutdown(boolean rb){
    state = State.SHUTDOWN;
    reboot = rb;
  }

  public ExecutionResult runThreaded(boolean syncReturn) {
    state = State.SLEEP;
    if(!initialized){
      System.out.println("Running Init");
      String eepromAddress = "";
      Map<String, String> components = machine.components();
      System.out.println("Getting EEPROM Address");
      for( Map.Entry<String,String> entry: components.entrySet()){
        System.out.println(entry.getValue());
        System.out.println(entry.getKey());
        if("eeprom".equals(entry.getValue())){
          eepromAddress = entry.getKey();
        }
      }
      if(eepromAddress.isEmpty()){
        System.out.println("No EEPROM");
        return new ExecutionResult.Error("No EEPROM");
      }
      System.out.println("Found EEPROM "+eepromAddress);
      String bios = "";
      System.out.println("Getting BIOS");
      try{
        byte[] biosIn = (byte[])machine.invoke(eepromAddress,"get",new Object[0])[0];
        bios = new String(biosIn);
        sandbox.eval(bios);
      } catch(LimitReachedException e){
        return new ExecutionResult.Error("Shouldn't run out of invoke requests on the first one.  Report to mod author");
      } catch(Exception e){
        e.printStackTrace();
        return new ExecutionResult.Error(e.getMessage());
      }
      System.out.println("Got BIOS");
      initialized=true;
    }else{
      if(invokeList.size()>0){
        int size = Math.min(invokeList.size(),10);
        for (int i = 0; i < size; i++){
          invokeList.get(i).call(this);
        }
        invokeList.subList(0,size).clear();
      }
      Signal next = machine.popSignal();
      if(next != null){
        JSObject onSignal = (JSObject)sandbox.get("computer.onSignal");
        onSignal.call(null,next.name(),next.args());
      }
    }
    switch(state){
      case SHUTDOWN: return new ExecutionResult.Shutdown(reboot);
      case SLEEP: return new ExecutionResult.Sleep(100);
      case INVOKE: return new ExecutionResult.Sleep(0);
      default: return new ExecutionResult.Sleep(100);
    }
  }
}

enum State {
  SHUTDOWN,SLEEP,INVOKE
}
