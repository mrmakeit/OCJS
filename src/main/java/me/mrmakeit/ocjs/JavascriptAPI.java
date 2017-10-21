package me.mrmakeit.ocjs;

import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import li.cil.oc.api.machine.*;

public class JavascriptAPI {

  Scriptable scope;
  Machine machine;
  boolean sync;
  ContextFactory factory = new APIContextFactory();
  ThreadResponse resp;  

  public boolean initialized = false;
  
  public JavascriptAPI(Machine m) {
    try{
    factory.addListener((ContextFactory.Listener)OCJS.debugger);
    }catch(Throwable e){
    }
    machine = m;
    this.resp = new ThreadResponse();
  }
  
  public void init() {
    Context cx = factory.enterContext();
    scope = cx.initSafeStandardObjects();
    Context.exit();
  }

  public void addComputer() {
    factory.enterContext();
    Object jsComp = Context.javaToJS(new ComputerAPI(machine,scope,resp), scope);
    ScriptableObject.putProperty(scope,"computer",jsComp);
    Context.exit();
  }

  public void runSync() {
    Context cx = factory.enterContext();
    System.out.println("Running Sync");
    resp.next.call(cx,scope,scope,new Object[0]);
    Context.exit();
  }

  public ExecutionResult runThreaded(boolean syncReturn) {
    Context cx = factory.enterContext();
    if(!initialized){
      String eepromAddress = "";
      Map<String, String> components = machine.components();
      System.out.println("Getting EEPROM Address");
      for( Map.Entry<String,String> entry: components.entrySet()){
        if(entry.getValue()=="eeprom"){
          eepromAddress = entry.getKey();
        }
      }
      if(eepromAddress == ""){
	System.out.println("No EEPROM");
        Context.exit();
        return new ExecutionResult.Error("No EEPROM");
      }
      System.out.println("Found EEPROM "+eepromAddress);
      String bios = "";
      System.out.println("Getting BIOS");
      try{
        byte[] biosIn = (byte[])machine.invoke(eepromAddress,"get",new Object[0])[0];
	bios = new String(biosIn);
      } catch(Exception e){
        Context.exit();
	e.printStackTrace();
        return new ExecutionResult.Error(e.getMessage());
      }
      System.out.println("Got BIOS");
      System.out.println(bios);
      cx.evaluateString(scope,bios,"<bios>",1,null);
      System.out.println("Ready");
      initialized=true;
    }else{
      if(resp.next!=null){
        resp.next.call(cx,scope,scope,new Object[0]);
      }else{
        Context.exit();
        return new ExecutionResult.Error("No More Functions To Execute");
      }
    }
    Context.exit();
    return resp.processResult();
  }
}
