package me.mrmakeit.ocjs;

import me.mrmakeit.ocjs.ContinuationResponse;
import me.mrmakeit.ocjs.ContinuationResponse.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.ContinuationPending;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.annotations.*;

import li.cil.oc.api.machine.*;

public class JavascriptAPI {

  Scriptable scope;
  Machine machine;
  ContextFactory factory = new ContextFactory();
  ContinuationPending pending;

  public boolean initialized = false;
  
  public JavascriptAPI(Machine m) {
    factory.addListener((ContextFactory.Listener)OCJS.debugger);
    machine = m;
  }
  
  public void init() {
    Context cx = factory.enterContext();
    scope = cx.initStandardObjects();
    cx.setOptimizationLevel(-1);
    Context.exit();
  }

  public void addComputer() {
    factory.enterContext();
    Object jsComp = Context.javaToJS(new ComponentAPI(), scope);
    ScriptableObject.putProperty(scope,"component",jsComp);
    Context.exit();
  }

  public void runSync() {
    Context cx = factory.enterContext();
    ContinuationPending c = pending;
    ContinuationResponse resp = (ContinuationResponse)c.getApplicationState();
    switch(resp.type) {
      case DIRECT:
        resp.resume(cx,scope,pending.getContinuation());
        break;
      case INVOKE:
        resp.resume(cx,scope,pending.getContinuation());
        break;
      case SLEEP:
        System.out.println("How did we get here.  Sleep shouldn't lead to the runSync() method.  Please contact the mod author.");
    }
    Context.exit();
  }

  public ExecutionResult runThreaded(boolean syncReturn) {
    Context cx = factory.enterContext();
    ExecutionResult result = null;
    //First Run
    if(initialized!=true) {
      InputStream stream = JavascriptAPI.class.getClassLoader().getResourceAsStream("boot.js");
      Reader read = new InputStreamReader(stream);
      try {
        Script script = cx.compileReader(read, "<kernel>", 1, null);
        cx.executeScriptWithContinuations(script,scope);
      }catch (IOException e) {
        System.out.println("Couldn't Find Boot.js in resources.  Can't start computer!");
        result = new ExecutionResult.Error("No Boot.js in mod resource.  Please notify the mod author");
      }catch (ContinuationPending c) {
        ContinuationResponse resp = (ContinuationResponse)c.getApplicationState();
        pending = c;
        switch(resp.type) {
          case DIRECT:
            result = new ExecutionResult.SynchronizedCall();
            machine.update();
            break;
          case INVOKE:
            result = new ExecutionResult.SynchronizedCall();
            machine.update();
            break;
          case SLEEP:
            int time = ((SleepResponse)resp).time;
            result = new ExecutionResult.Sleep(time);
            machine.update();
            break;
        }
        initialized=true;
      }
    }else{      
      ContinuationResponse response = (ContinuationResponse)pending.getApplicationState();
      try {
        response.resume(cx,scope,pending.getContinuation());
      }catch (ContinuationPending c) {
        ContinuationResponse resp = (ContinuationResponse)c.getApplicationState();
        pending = c;
        switch(resp.type) {
          case DIRECT:
            result = new ExecutionResult.SynchronizedCall();
            machine.update();
          case INVOKE:
            result = new ExecutionResult.SynchronizedCall();
            machine.update();
          case SLEEP:
            int time = ((SleepResponse)resp).time;
            result = new ExecutionResult.Sleep(time);
            machine.update();
        }
      }
    }

    //No Continuation was created.  This means the script closed, which means the system did not shutdown correctly
    if(result==null) {
      result = new ExecutionResult.Error("Kernel Unexpectedly quit.");
    }
    Context.exit();
    return result;
  }
      
  public class ComponentAPI {

    public ComponentAPI() {
    }

    @JSFunction
    public NativeObject list() {
      Map<String, String> components = machine.components();
      NativeObject nobj = new NativeObject();
      for (Map.Entry<String, String> entry : components.entrySet()) {
          nobj.defineProperty(entry.getKey(), entry.getValue(), NativeObject.READONLY);
      }
      return nobj;
    }

    @JSFunction
    public Object[] invoke(String address, String method, Object[] params){
      System.out.println("Running "+method+" on "+address);
      try {
        Object[] result = machine.invoke(address,method,params);
        return result;
      }
      catch (LimitReachedException e){
        System.out.println("Limit Reached");
        ContinuationPending continuation = Context.getCurrentContext().captureContinuation();
        continuation.setApplicationState(new LimitResponse(machine, address,method,params));
        throw continuation;
      }
      catch (Exception e){
        Object[] result = null;
        return result;
      }
    }
    @JSFunction
    public NativeObject pullSignal(){
      System.out.println("Fetching Signal");
      Signal signal = machine.popSignal();
      NativeObject nobj = new NativeObject();
      nobj.defineProperty("name",signal.name(),NativeObject.READONLY);
      nobj.defineProperty("args",signal.args(),NativeObject.READONLY);
      return nobj;
    }
    @JSFunction
    public void sleep(int time){
      System.out.println("Sleeping for "+time+" seconds");
      ContinuationPending continuation = Context.getCurrentContext().captureContinuation();
      continuation.setApplicationState(new SleepResponse(machine, time));
      throw continuation;
    }
    public void direct(){
      System.out.println("Direct Function Call");
      ContinuationPending continuation = Context.getCurrentContext().captureContinuation();
      continuation.setApplicationState(new DirectResponse(machine));
      throw continuation;
    }
    @JSFunction
    public Object[] error(String message){
      //boolean state = machine.crash(message);
      boolean state = false;
      System.out.println(message);
      return new Object[]{state};
    }
  }
}
