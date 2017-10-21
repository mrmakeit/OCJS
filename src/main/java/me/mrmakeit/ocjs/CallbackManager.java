package me.mrmakeit.ocjs;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Context;

import li.cil.oc.api.machine.Machine;
import li.cil.oc.api.machine.Signal;

import java.util.List;

abstract class CallbackManager {
  Machine machine;
  Scriptable scope;
  List<Callback> callbacks;
  CallbackManager(Machine machine,Scriptable scope){
    this.machine = machine;
    this.scope = scope;
  }
  public abstract void addCallback(Function cb);
  public abstract void runCallbacks(Context cx);

  static class EventLoop extends CallbackManager {
    EventLoop(Machine machine, Scriptable scope){
      super(machine,scope);
    }
    @Override
    public void addCallback(Function cb){
      this.callbacks.add(new Callback(cb));
    }
    @Override
    public void runCallbacks(Context cx){
      for(Callback cb: callbacks){
        cb.call(cx,scope,null);
      }
    }
  } 

  static class SignalLoop extends CallbackManager {
    SignalLoop(Machine machine, Scriptable scope){
      super(machine,scope);
    }
    @Override
    public void addCallback(Function cb){
      this.callbacks.add(new Callback(cb));
    }
    @Override
    public void runCallbacks(Context cx){
      Signal signal = machine.popSignal();
      if(signal==null){
        return;
      }
      Object[] args = {signal.name(),signal.args()};
      for(Callback cb: callbacks){
        cb.call(cx,scope,args);
      }
    }
  } 
}
