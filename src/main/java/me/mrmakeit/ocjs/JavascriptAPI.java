package me.mrmakeit.ocjs;

import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.JavaScriptException;
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
    try{
      resp.next.call(cx,scope,scope,new Object[0]);
    } catch(JavaScriptException e){
      Context.exit();
      e.printStackTrace();
      machine.crash((String)e.getValue());
      return;
    } catch(EvaluatorException e){
      Context.exit();
      e.printStackTrace();
      machine.crash((String)e.getMessage());
      return;
    }
    Context.exit();
  }

  public ExecutionResult runThreaded(boolean syncReturn) {
    Context cx = factory.enterContext();
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
        Context.exit();
        return new ExecutionResult.Error("No EEPROM");
      }
      System.out.println("Found EEPROM "+eepromAddress);
      String bios = "";
      System.out.println("Getting BIOS");
      try{
        byte[] biosIn = (byte[])machine.invoke(eepromAddress,"get",new Object[0])[0];
        bios = new String(biosIn);
      } catch(LimitReachedException e){
        Context.exit();
        return new ExecutionResult.Error("Shouldn't run out of invoke requests on the first one.  Report to mod author");
      } catch(Exception e){
        Context.exit();
        e.printStackTrace();
        return new ExecutionResult.Error(e.getMessage());
      }
      System.out.println("Got BIOS");
      System.out.println(bios);
      try{
        cx.evaluateString(scope,bios,"<bios>",1,null);
      } catch(JavaScriptException e){
        e.printStackTrace();
        return new ExecutionResult.Error((String)e.getValue());
      } catch(EvaluatorException e){
        e.printStackTrace();
        return new ExecutionResult.Error((String)e.getMessage());
      }
      System.out.println("Ready");
      initialized=true;
    }else{
      try{
        resp.processInvoke(cx,scope);
        resp.processLoop(cx,scope,machine);
      } catch(JavaScriptException e){
        Context.exit();
        e.printStackTrace();
        return new ExecutionResult.Error(e.getMessage());
      } catch(Exception e){
        Context.exit();
        e.printStackTrace();
        return new ExecutionResult.Error(e.getMessage());
      }
    }
    Context.exit();
    return resp.processResult();
  }
}
