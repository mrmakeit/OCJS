package me.mrmakeit.ocjs;

import org.mozilla.javascript.*;


public class ContinuationResponse{

  public static enum ContinuationType {
    SLEEP, DIRECT, INVOKE
  }

  ContinuationType type;
  int time;
  String address;
  String method;
  Object[] params;

  public ContinuationResponse(){
  }

  public void resume(Context cx, Scriptable scope, Object continuation) throws ContinuationPending{
  }
  public static class SleepResponse extends ContinuationResponse{
    public SleepResponse(int time){
      this.type = ContinuationType.DIRECT;
      this.time = time;
    }
    @Override
    public void resume(Context cx, Scriptable scope, Object continuation){
      cx.resumeContinuation(continuation,scope,null);
    }
  }
  public static class DirectResponse extends ContinuationResponse{
    public DirectResponse(){
      type = ContinuationType.DIRECT;
    }
    @Override
    public void resume(Context cx, Scriptable scope, Object continuation){
      cx.resumeContinuation(continuation,scope,null);
    }
  }
  public static class LimitResponse extends ContinuationResponse{
    public LimitResponse(String address, String method, Object[] params){
      type = ContinuationType.INVOKE;
    }
    @Override
    public void resume(Context cx, Scriptable scope, Object continuation){
      cx.resumeContinuation(continuation,scope,null);
    }
  }
}
