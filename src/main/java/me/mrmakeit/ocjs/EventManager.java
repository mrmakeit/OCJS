package me.mrmakeit.ocjs;

import org.mozilla.javascript.Scriptable;


import li.cil.oc.api.Machine;

class EventManager {
  Machine machine;
  Scriptable scope;
  EventManager(Machine machine, Scriptable scope){
    this.machine = machine;
    this.scope = scope;
  }
}
