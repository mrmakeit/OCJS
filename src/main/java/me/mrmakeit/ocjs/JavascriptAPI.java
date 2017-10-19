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
import org.mozilla.javascript.Script; import org.mozilla.javascript.ContinuationPending;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.annotations.*;

import li.cil.oc.api.machine.*;

public class JavascriptAPI {

  Scriptable scope;
  Machine machine;
  ContextFactory factory = new APIContextFactory();
  ContinuationPending pending;

  public boolean initialized = false;
  
  public JavascriptAPI(Machine m) {
    try{
    factory.addListener((ContextFactory.Listener)OCJS.debugger);
    }catch(Throwable e){
    }
    machine = m;
  }
  
  public void init() {
    Context cx = factory.enterContext();
    scope = cx.initSafeStandardObjects();
    Context.exit();
  }

  public void addComputer() {
    Context cx = factory.enterContext();
    Object jsComp = Context.javaToJS(new ComputerAPI(machine), scope);
    ScriptableObject.putProperty(scope,"computer",jsComp);
    Function eval = cx.compileFunction(scope,"function eval(a){error('No, Dont Eval!')}","eval",1,null);
    ScriptableObject.putProperty(scope,"eval",eval);
    Context.exit();
  }

  public void runSync() {
    ContinuationPending c = pending;
    ManageSyncPending(c);
    Context.exit();
  }

  public void ManageSyncPending(ContinuationPending c){
    Context cx = factory.enterContext();
    ContinuationResponse resp = (ContinuationResponse)c.getApplicationState();
    switch(resp.type) {
      case DIRECT:
        resp.resume(cx,scope,pending.getContinuation());
        break;
      case INVOKE:
        resp.resume(cx,scope,pending.getContinuation());
        break;
      case SLEEP:
        break;
      case EVAL:
        System.out.println("Running Sync Eval");
        try{
          ((EvalResponse)resp).executeScript(cx,scope);
        } catch(ContinuationPending d){
          ManageSyncPending(d);
        } catch(Exception e){
          System.out.println("Oops? ");
          e.printStackTrace();
	  machine.crash(e.getMessage());
	  return;
        }finally{
          try{
            cx.resumeContinuation(c.getContinuation(),scope,null);
          } catch(ContinuationPending d){
            ManageSyncPending(d);
          } catch(Exception e){
            System.out.println("Oops? ");
            e.printStackTrace();
	    machine.crash(e.getMessage());
          }
        }
    }
  }
  public ExecutionResult ManagePending(ContinuationPending c){
    ExecutionResult result = null;
    ContinuationResponse resp = (ContinuationResponse)c.getApplicationState();
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
      case EVAL:
        System.out.println("Running Eval");
        Context cx = Context.getCurrentContext();
        try{
          ((EvalResponse)resp).executeScript(cx,scope);
        } catch(ContinuationPending d){
          result = ManagePending(d);
        } catch(Exception e){
          System.out.println("Oops? ");
          e.printStackTrace();
	  machine.crash(e.getMessage());
	  return new ExecutionResult.Error(e.getMessage());
        }finally{
          try{
            cx.resumeContinuation(c.getContinuation(),scope,null);
          } catch(ContinuationPending d){
            result = ManagePending(d);
          } catch(Exception e){
            System.out.println("Oops? ");
            e.printStackTrace();
	    machine.crash(e.getMessage());
          }
        }
    }
    return result;
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
        pending = c;
        result = ManagePending(c);
        initialized=true;
      }
    }else{      
      ContinuationResponse response = (ContinuationResponse)pending.getApplicationState();
      try {
        response.resume(cx,scope,pending.getContinuation());
      }catch (ContinuationPending c) {
        result = ManagePending(c);
      }
    }

    //No Continuation was created.  This means the script closed, which means the system did not shutdown correctly
    if(result==null) {
      result = new ExecutionResult.Error("Kernel Unexpectedly quit.");
    }
    Context.exit();
    return result;
  }
      
}
