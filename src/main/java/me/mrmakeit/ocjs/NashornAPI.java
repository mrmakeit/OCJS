package me.mrmakeit.ocjs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import javax.script.*;

import com.google.common.io.ByteStreams;

import delight.nashornsandbox.*;

import jdk.nashorn.api.scripting.*;

import li.cil.oc.api.machine.*; 
public class NashornAPI {

  Machine machine;
  NashornSandbox sandbox;
  State state;
  LoaderAPI loader;
  boolean runBabel;
  boolean reboot = false;
  int sleepTime = 0;
  List<InvokeCallback> invokeList = new ArrayList<InvokeCallback>();

  public boolean initialized = false;
  
  public NashornAPI(Machine m,boolean enableBabel) {
    this.runBabel = enableBabel;
    machine = m;
    sandbox = NashornSandboxes.create();
    if(enableBabel){
      sandbox.setMaxCPUTime(4000);
    }else{
      sandbox.setMaxCPUTime(500);
    }
    sandbox.setExecutor(Executors.newSingleThreadExecutor());
    sandbox.inject("computer", new ComputerAPI(machine,this));
    loader = LoaderAPI.get();
    if(enableBabel){
    sandbox.inject("loader", loader);
    try{
      String coreCode = new String(ByteStreams.toByteArray(OCJS.class.getClassLoader().getResourceAsStream("core.js")));
      String bootCode = new String(ByteStreams.toByteArray(OCJS.class.getClassLoader().getResourceAsStream("boot.js")));
      sandbox.eval(coreCode);
      sandbox.eval(bootCode);
      sandbox.eval("babelEval = loader.es6Eval"); 
    }catch(IOException e){
      System.err.println("Couldn't find core.js");
    }catch(ScriptException e){
      System.err.println("Can't rebuild eval.  No babel support. Error Message: "+e.getMessage());
    }
    }
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
      String eepromAddress = "";
      Map<String, String> components = machine.components();
      for( Map.Entry<String,String> entry: components.entrySet()){
        if("eeprom".equals(entry.getValue())){
          eepromAddress = entry.getKey();
        }
      }
      if(eepromAddress.isEmpty()){
        return new ExecutionResult.Error("No EEPROM");
      }
      String bios = "";
      try{
        byte[] biosIn = (byte[])machine.invoke(eepromAddress,"get",new Object[0])[0];
        bios = new String(biosIn);
        if(this.runBabel){
          bios = loader.eval(bios);
        }
        sandbox.eval(bios);
      } catch(LimitReachedException e){
        return new ExecutionResult.Error("Shouldn't run out of invoke requests on the first one.  Report to mod author");
      } catch(ScriptException e){
        System.err.println("Javascript invoke error: "+e.getMessage());
        return new ExecutionResult.Error(e.getMessage());
      } catch(Exception e){
        e.printStackTrace();
        return new ExecutionResult.Error(e.getMessage());
      }
      initialized=true;
    }else{
      if(invokeList.size()>0){
        int size = Math.min(invokeList.size(),10);
        for (int i = 0; i < size; i++){
          invokeList.get(i).call(this);
        }
        invokeList.subList(0,size).clear();
      }
      ScriptObjectMirror onSignal = (ScriptObjectMirror)sandbox.get("onSignal");
      Signal next = machine.popSignal();
      if(onSignal==null){
        machine.crash("No event loop.");
      }else{
        if(next != null){
          Object[] resp = new Object[next.args().length+1];
          resp[0] = next.name();
          Object[] args = next.args();
          for(int i = 0; i < args.length; i = i+1) {
            resp[i+1] = args[i];
          }
          onSignal.call(null,resp);
        }else{
          onSignal.call(null,null);
        }
      }
    }
    switch(state){
      case SHUTDOWN: return new ExecutionResult.Shutdown(reboot);
      case SLEEP: return new ExecutionResult.Sleep(this.sleepTime);
      case INVOKE: return new ExecutionResult.Sleep(0);
      default: return new ExecutionResult.Sleep(this.sleepTime);
    }
  }
}

enum State {
  SHUTDOWN,SLEEP,INVOKE
}
