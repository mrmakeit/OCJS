package me.mrmakeit.ocjs;

import java.util.ArrayList;
import java.util.List;

import org.mozilla.javascript.Function;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import li.cil.oc.api.machine.Machine;
import li.cil.oc.api.machine.ExecutionResult;

import me.mrmakeit.ocjs.Callback.InvokeCallback;

class ThreadResponse {
  boolean shutdown = false;
  boolean reboot = false;
  boolean sync = false;
  int sleep = 0;
  int defaultSleep = 0;
  boolean sleepSet = false;
  Function next;
  boolean inSync = false;
  boolean finalLoop = false;
  List<InvokeCallback> invokeList = new ArrayList<InvokeCallback>();

  void shutdown(boolean andReboot){
    if(!shutdown){
      shutdown = true;
      reboot = andReboot;
    }
  }

  void addInvoke(InvokeCallback cb){
    invokeList.add(cb);
  }

  void sleep(int time){
    if(!sleepSet||time<sleep){
      this.sleep=time;
    }
  }

  void defaultSleep(int time){
    defaultSleep=time;
  }
  
  void nextFunction(Function next){
    System.out.println("Next function appended");
    this.next = next;
  }

  void makeSync(){
    sync = true;
  }

  void syncDone(){
    sync = false;
    inSync = false;
  }

  void processLoop(Context cx, Scriptable scope, Machine machine){
    if(next==null){
      machine.crash("No Event Loop Function!");
      return;
    }
    next.call(cx,scope,scope,new Object[]{});
  }

  void processInvoke(Context cx,Scriptable scope){
    //TODO: limit size based on cpu tier;
    if(invokeList.size()<=0){
      return;
    }
    int size = Math.min(invokeList.size(),10);
    for (int i = 0; i < size; i++){
      invokeList.get(i).call(cx,scope,null);
    }
    invokeList.subList(0,size).clear();
  }

  ExecutionResult processResult(){
    System.out.println("Processing next loop response");
    if(finalLoop){
      return new ExecutionResult.Shutdown(reboot);
    }
    if(shutdown){
      finalLoop=true;
    }
    if(sync){
      inSync = true;
      return new ExecutionResult.SynchronizedCall();
    }
    ExecutionResult res = new ExecutionResult.Sleep(sleep);
    sleepSet=false;
    sleep=defaultSleep;
    return res;
  }
}
