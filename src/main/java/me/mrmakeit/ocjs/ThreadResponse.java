package me.mrmakeit.ocjs;

import org.mozilla.javascript.Function;

import li.cil.oc.api.machine.ExecutionResult;

class ThreadResponse {
  boolean shutdown = false;
  boolean reboot = false;
  boolean sync = false;
  int sleep = 0;
  int defaultSleep = 0;
  boolean sleepSet = false;
  Function next;
  Function doNext;
  boolean inSync = false;
  boolean finalLoop = false;

  void shutdown(boolean andReboot){
    if(!shutdown){
      shutdown = true;
      reboot = andReboot;
    }
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
    this.doNext = next;
  }

  void makeSync(){
    sync = true;
  }

  void syncDone(){
    sync = false;
    inSync = false;
  }

  ExecutionResult processResult(){
    next=doNext;
    doNext=null;
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
