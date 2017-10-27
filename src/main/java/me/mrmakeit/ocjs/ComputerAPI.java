package me.mrmakeit.ocjs;

import me.mrmakeit.ocjs.Callback.*;

import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.JavaScriptException;
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
  public void invoke(String address, String method, Object[] params,Function callback, Function error){
    Context cx = Context.getCurrentContext();
    System.out.println("Running "+method+" on "+address);
    try{ 
      Object[] result = machine.invoke(address,method,params);
      if(result==null){
        result = new Object[]{};
      }
      callback.call(cx,scope,scope,result);
    } catch (LimitReachedException e){
      resp.addInvoke(new InvokeCallback(machine,address,method,params,callback,error));
    } catch(Exception e){
      e.printStackTrace();
      error.call(cx,scope,scope,new Object[]{e.getMessage()});
    }
  }

  @JSFunction
  public Object[] invokeSync(String address, String method, Object[] params){
    Context cx = Context.getCurrentContext();
    System.out.println("Running "+method+" on "+address+" Sync.");
    Object[] result = null;
    try{ 
      result = machine.invoke(address,method,params);
    } catch(LimitReachedException e){
      result = new Object[] {"Error","Too many invoke calls.  Use async to avoid this"};
    } catch(Exception e){
      e.printStackTrace();
      throw new JavaScriptException(e.getMessage(), "invoke", 0);
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
