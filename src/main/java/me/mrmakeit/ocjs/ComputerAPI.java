package me.mrmakeit.ocjs;

import me.mrmakeit.ocjs.ContinuationResponse;
import me.mrmakeit.ocjs.ContinuationResponse.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.ContinuationPending;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.annotations.*;

import li.cil.oc.api.machine.*;

public class ComputerAPI {

  private Machine machine;

  public ComputerAPI(Machine machine) {
    this.machine = machine;
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
  public void eval(String scriptText){
    ContinuationPending continuation = Context.getCurrentContext().captureContinuation();
    Script script = Context.getCurrentContext().compileString(scriptText,"(eval)",1,null);
    continuation.setApplicationState(new EvalResponse(machine,script));
    throw continuation;
  }
  @JSFunction
  public void load(String scriptText, String name){
    ContinuationPending continuation = Context.getCurrentContext().captureContinuation();
    Script script = Context.getCurrentContext().compileString(scriptText,name,1,null);
    continuation.setApplicationState(new EvalResponse(machine,script));
    System.out.println("Compiled script under "+name);
    throw continuation;
  }
  @JSFunction
  public NativeObject pullSignal(){
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
    boolean state = machine.crash(message);
    return new Object[]{state};
  }
}
