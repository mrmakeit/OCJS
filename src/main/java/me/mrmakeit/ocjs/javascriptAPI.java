package me.mrmakeit.ocjs;

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


public class javascriptAPI {
	Scriptable scope;
	Machine machine;
	Object[] next;
	Object[] pendingEval;
  ContextFactory factory = new ContextFactory();

	public boolean limited;
	public boolean eval;
	public javascriptAPI(Machine m) {
    factory.addListener(OCJS.debugger);
		machine = m;
		Context cx = factory.enterContext();
		scope = cx.initStandardObjects();
		Context.exit();
	}
	
	public void init(){
		Context cx = factory.enterContext();
    cx.setOptimizationLevel(-1);
		InputStream stream = javascriptAPI.class.getClassLoader().getResourceAsStream("boot.js");
		Reader read = new InputStreamReader(stream);
		try {
			Script script = cx.compileReader(read, "boot", 1, null);
      cx.executeScriptWithContinuations(script,scope);
    }catch (IOException e){
      System.out.println("Couldn't Find Boot.js in resources.  Can't start computer!");
    }
    Context.exit();
	}
	public boolean rerun(){
    limited = false;
    ContinuationPending continuationpending = (ContinuationPending)next[3];
    Object[] result = null;
		try {
			result = machine.invoke((String)next[0],(String)next[1],(Object[])next[2]);
		}
    catch(Exception e){
      result = null;
    }
    Object continuation = continuationpending.getContinuation();
    Context cx = factory.enterContext();
    cx.resumeContinuation(continuation,scope,result);
    Context.exit();
    return false;
	}
	public void addComputer(){
		factory.enterContext();
		Object jsComp = Context.javaToJS(new ComponentAPI(), scope);
		ScriptableObject.putProperty(scope,"component",jsComp);
		Context.exit();
	}
	public ExecutionResult run(Signal signal){
		Context cx = factory.enterContext();
    cx.setOptimizationLevel(-1);
		String s = "event(\""+signal.name()+"\"";
		for (int i=0; i<signal.args().length; i++){
			s += ",\""+signal.args()[i]+"\"";
		}
		s += ")";
		Script script = cx.compileString(s, "<cmd>", 1, null);
    Object result = cx.executeScriptWithContinuations(script,scope);
		if(Context.toString(result).equals("shutdown")){
			Context.exit();
			return new ExecutionResult.Shutdown(false);
		}else{
			Context.exit();
			return new ExecutionResult.Sleep(0);
		}
	}
	public class ComponentAPI{
		public ComponentAPI(){
		}
    @JSFunction
    public void eval(String name,String code){
      Context cx = Context.getCurrentContext();
      ContinuationPending continuation = cx.captureContinuation();
      cx.setOptimizationLevel(-1);
      Script script = cx.compileString(code,name,1,null);
      pendingEval[0]=script;
      cx.executeScriptWithContinuations(script,scope);
      try{
        throw continuation;
      }catch (Exception c){
        cx.resumeContinuation(continuation.getContinuation(),scope,null);
      }
    }
		@JSFunction
		public NativeObject list(){
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
				// TODO Auto-generated catch block
				next[0]=address;
				next[1]=method;
				next[2]=params;
        ContinuationPending continuation = Context.getCurrentContext().captureContinuation();
        next[3]=continuation;
				limited=true;
        throw continuation;
			}
      catch (Exception e){
				Object[] result = null;
				return result;
      }
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
