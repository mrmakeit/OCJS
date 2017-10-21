package me.mrmakeit.ocjs;

import me.mrmakeit.ocjs.CallbackManager.*;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import li.cil.oc.api.machine.*;

public class JavascriptAPI {

  Scriptable scope;
  Machine machine;
  boolean sync;
  ContextFactory factory = new APIContextFactory();
  EventLoop eventLoop;
  SignalLoop signalLoop;

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
    this.eventLoop = new EventLoop(machine,scope);
    this.signalLoop = new SignalLoop(machine,scope);
    Context.exit();
  }

  public void addComputer() {
    Context cx = factory.enterContext();
    Object jsComp = Context.javaToJS(new ComputerAPI(machine,scope), scope);
    ScriptableObject.putProperty(scope,"computer",jsComp);
    Context.exit();
  }

  public void runSync(boolean newSignal) {
    Context cx = factory.enterContext();
    eventLoop.runCallbacks(cx);
    signalLoop.runCallbacks(cx);
    Context.exit();
  }

  public ExecutionResult runThreaded(boolean syncReturn) {
    Context cx = factory.enterContext();
    ExecutionResult result = null;
    if(initialized){
    }
    Context.exit();
    return result;
  }
}
