package com.mrmakeit.ocjs;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import li.cil.oc.api.machine.ExecutionResult;
import li.cil.oc.api.machine.Signal;

public class javascriptAPI {
	Scriptable scope;
	public javascriptAPI() {
		Context cx = Context.enter();
		scope = cx.initStandardObjects();
		this.init(cx);
		Context.exit();
	}
	
	private Object init(Context cx){
		String s = "function touch(){return 'Dont touch me!'}";
		Object result = cx.evaluateString(scope, s, "<cmd>", 1, null);
		return result;
	}
	
	public ExecutionResult run(Signal signal){
		Context cx = Context.enter();
		String s = signal.name()+"(";
		for (int i=1; i<signal.args().length; i++){
			s += "\""+signal.args()[i]+"\"";
			if (i<signal.args().length-1){
				s+=",";
			}
		}
		s += ")";
		Object result = cx.evaluateString(scope, s, "<cmd>", 1, null);
		System.out.println(Context.toString(result));
		Context.exit();
		return new ExecutionResult.Sleep(0);
	}
}
