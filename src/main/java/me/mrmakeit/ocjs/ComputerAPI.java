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
  private boolean sync = false;

  public ComputerAPI(Machine machine, Scriptable scope) {
    this.machine = machine;
    this.scope = scope;
  }

  public void syncOn(){
    sync = true;
  }

  public void syncOff(){
    sync = false;
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
  public void invoke(String address, String method, Object[] params,Function callback){
    Context cx = Context.getCurrentContext();
    if(sync){
      new InvokeCallback(machine,address,method,params,callback).call(cx,scope,null);
    }else{
      cx.evaluateString(scope, "throw { error:\"SyncOnly\",message:\"Machine.invoke can only be called inside an event loop or a Machine.direct callback\"}","<Error>",1,null);
    }
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
  }

  @JSFunction
  public Object[] error(String message){
    boolean state = machine.crash(message);
    return new Object[]{state};
  }
}
