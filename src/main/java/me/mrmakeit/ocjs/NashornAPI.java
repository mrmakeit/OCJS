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
  JSObject babel;
  ScriptEngine babelEngine;
  State state;
  LoaderAPI loader;
  boolean runBabel;
  boolean reboot = false;
  List<InvokeCallback> invokeList = new ArrayList<InvokeCallback>();

  public boolean initialized = false;
  
  public NashornAPI(Machine m,boolean enableBabel) {
    babelEngine = new ScriptEngineManager().getEngineByName("nashorn");
    this.runBabel = enableBabel;
    machine = m;
    sandbox = NashornSandboxes.create();
    sandbox.setMaxCPUTime(200);
    sandbox.setExecutor(Executors.newSingleThreadExecutor());
    sandbox.inject("computer", new ComputerAPI(machine,this));
    if(enableBabel){
      try{
        String coreCode = new String(ByteStreams.toByteArray(OCJS.class.getClassLoader().getResourceAsStream("core.js")));
        String babelCode = new String(ByteStreams.toByteArray(OCJS.class.getClassLoader().getResourceAsStream("babel.js")));
        SimpleBindings bindings = new SimpleBindings();
        babelEngine.eval(coreCode,bindings);
        babelEngine.eval(babelCode,bindings);
        loader = new LoaderAPI(machine,babelEngine,bindings);
        babel = (JSObject) babelEngine.eval("Babel",bindings);
        sandbox.inject("loader", loader);
        sandbox.eval("var temp = function(eval){return function(code){eval(loader.eval(code))}}(eval); eval = temp;");
      }catch(IOException e){
        m.crash("Couldn't find babel.js");
      }catch(ScriptException e){
        m.crash("Couldn't initialize babel "+e.getMessage());
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
        System.out.println("Got BIOS");
        if(runBabel){
          bios = loader.eval(bios);
        }
        sandbox.eval(bios);
      } catch(LimitReachedException e){
        return new ExecutionResult.Error("Shouldn't run out of invoke requests on the first one.  Report to mod author");
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
      Signal next = machine.popSignal();
      if(next != null){
        Object[] resp = new Object[next.args().length+1];
        resp[0] = next.name();
        Object[] args = next.args();
        for(int i = 0; i < args.length; i = i+1) {
          resp[i+1] = args[i];
        }
        ScriptObjectMirror onSignal = (ScriptObjectMirror)sandbox.get("onSignal");
        if(onSignal!=null){
          onSignal.call(null,resp);
        }else{
          machine.crash("No event loop.");
        }
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
