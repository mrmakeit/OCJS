package me.mrmakeit.ocjs;

import org.mozilla.javascript.*;

import li.cil.oc.api.machine.Machine;


public abstract class ContinuationResponse {

  public static enum ContinuationType {
    SLEEP, DIRECT, INVOKE
  }

  ContinuationType type;
  Machine machine;

  public ContinuationResponse(Machine machine) {
    this.machine = machine;
  }

  public void resume(Context cx, Scriptable scope, Object continuation) throws ContinuationPending {
  }

  public static class SleepResponse extends ContinuationResponse {
    int time;
    public SleepResponse(Machine machine, int time){
      super(machine);
      this.time = time;
      this.type = ContinuationType.DIRECT;
    }
    @Override
    public void resume(Context cx, Scriptable scope, Object continuation) {
      cx.resumeContinuation(continuation,scope,null);
    }
  }
  public static class DirectResponse extends ContinuationResponse {
    public DirectResponse(Machine machine){
      super(machine);
      this.type = ContinuationType.DIRECT;
    }
    @Override
    public void resume(Context cx, Scriptable scope, Object continuation) {
      cx.resumeContinuation(continuation,scope,null);
    }
  }
  public static class LimitResponse extends ContinuationResponse {
    private String address;
    private String method;
    private Object[] params;
    public LimitResponse(Machine machine, String address, String method, Object[] params) {
      super(machine);
      this.address = address;
      this.method = method;
      this.params = params;
      this.type = ContinuationType.INVOKE;
    }
    @Override
    public void resume(Context cx, Scriptable scope, Object continuation) {
      Object[] result = null;
      try {
        result = machine.invoke(address,method,params);
      } catch (Exception e) {
      }
      cx.resumeContinuation(continuation,scope,result);
    }
  }
}
