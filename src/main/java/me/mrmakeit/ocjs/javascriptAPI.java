package me.mrmakeit.ocjs;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.annotations.*;

import li.cil.oc.api.machine.*;

public class javascriptAPI {
	Scriptable scope;
	Machine machine;
	Object[] next;
	public boolean limited;
	public javascriptAPI(Machine m) {
		machine = m;
		Context cx = Context.enter();
		scope = cx.initStandardObjects();
		Context.exit();
	}
	
	public void init(){
		Context cx = Context.enter();
		InputStream stream = javascriptAPI.class.getClassLoader().getResourceAsStream("boot.js");
		Reader read = new InputStreamReader(stream);
		try {
			cx.evaluateReader(scope, read, "boot", 1, null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Context.exit();
	}
	public Object[] rerun(){
		limited=false;
		try {
			return machine.invoke((String)next[0],(String)next[1],(Object[])next[2]);
		} catch (Exception e) {
			limited=true;
			return new Object[]{false};
		}
	}
	public void addComputer(){
		Context.enter();
		Object jsComp = Context.javaToJS(new ComponentAPI(), scope);
		ScriptableObject.putProperty(scope,"component",jsComp);
		Context.exit();
	}
	public void addOut(){
		Context.enter();
		Object jsOut = Context.javaToJS(System.out, scope);
		ScriptableObject.putProperty(scope, "out", jsOut);
		Context.exit();
	}
	public ExecutionResult run(Signal signal){
		Context cx = Context.enter();
		String s = "event(\""+signal.name()+"\"";
		for (int i=0; i<signal.args().length; i++){
			s += ",\""+signal.args()[i]+"\"";
		}
		s += ")";
		Object result = cx.evaluateString(scope, s, "<cmd>", 1, null);
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
		public NativeObject list(){
			Map<String, String> components = machine.components();
			NativeObject nobj = new NativeObject();
			for (Map.Entry<String, String> entry : components.entrySet()) {
			    nobj.defineProperty(entry.getKey(), entry.getValue(), NativeObject.READONLY);
			}
			return nobj;
		}
		@JSFunction
		public NativeArray invoke(String address, String method, Object[] params){
			try {
				Object[] result = machine.invoke(address,method,params);
				NativeArray toReturn = new NativeArray(result.length);
				for (Object element : result){
					toReturn.add(element);
				}
				return toReturn;
			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				next[0]=address;
				next[1]=method;
				next[2]=params;
				limited=true;
				NativeArray result = new NativeArray(1);
				result.add(false);
				return result;
			}
		}

		@JSFunction
		public Object[] error(String message){
			boolean state = machine.crash(message);
			return new Object[]{state};
		}

	}
}
