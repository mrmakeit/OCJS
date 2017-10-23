package me.mrmakeit.ocjs;

import me.mrmakeit.ocjs.Callback.*;

import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.annotations.*;

import li.cil.oc.api.machine.*;

public class ComputerAPI {

  private Machine machine;
  private Scriptable scope;
  private ThreadResponse resp;

  public ComputerAPI(Machine machine, Scriptable scope, ThreadResponse resp) {
    this.machine = machine;
    this.scope = scope;
    this.resp = resp;
  }

  @JSFunction
  public NativeObject list() {
    Map<String, String> components = machine.components();
    NativeObject nobj = new NativeObject();
    System.out.println("Getting component list");
    for (Map.Entry<String, String> entry : components.entrySet()) {
        nobj.defineProperty(entry.getKey(), entry.getValue(), NativeObject.READONLY);
    }
    return nobj;
  }

  //TODO: Create invoke callback list in JavascriptAPI to handle invokes that failed due to
  //    | InvokeLimitReached exception. This will allow the function to work in threaded
  //    | mode as well.
  @JSFunction
  public void invoke(String address, String method, Object[] params,Function callback){
    Context cx = Context.getCurrentContext();
    System.out.println("Running "+method+" on "+address);
    if(resp.inSync){
      new InvokeCallback(machine,address,method,params,callback).call(cx,scope,null);
    }else{
      cx.evaluateString(scope, "throw { error:\"SyncOnly\",message:\"Machine.invoke can only be called inside a Machine.direct callback\"}","<Error>",1,null);
    }
  }

  //TODO: Make this function return success/failure instead of actual result to encourage using above format.
  @JSFunction
  public Object[] invoke(String address, String method, Object[] params){
    Context cx = Context.getCurrentContext();
    System.out.println("Running "+method+" on "+address+" Directly.");
    Object[] result = null;
    if(resp.inSync){
      try{ 
        result = machine.invoke(address,method,params);
      } catch(Exception e){
  e.printStackTrace();
        cx.evaluateString(scope, "throw { error:\"InvokeError\",message:\""+e.getMessage()+"\"}","<Error>",1,null);
      }
    }else{
      cx.evaluateString(scope, "throw { error:\"SyncOnly\",message:\"Machine.invoke can only be called inside a Machine.direct callback\"}","<Error>",1,null);
    }
    return result;
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
  public void direct(){
    System.out.println("Direct Function Call");
    resp.makeSync();
  }

  @JSFunction
  public void sleep(int time){
    resp.sleep(time);
  }

  @JSFunction
  public void setDefaultSleep(int time){
    resp.defaultSleep(time);
  }

  @JSFunction
  public void shutdown(boolean reboot){
    resp.shutdown(reboot);
  }

  @JSFunction
  public void next(Function cb){
    resp.nextFunction(cb);
  }

  @JSFunction
  public void print(String message){
    System.out.println(message);
  }

  @JSFunction
  public Object[] error(String message){
    boolean state = machine.crash(message);
    return new Object[]{state};
  }
}
