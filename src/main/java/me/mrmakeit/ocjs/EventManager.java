package me.mrmakeit.ocjs;

import org.mozilla.javascript.Scriptable;

import java.util.List;

import li.cil.oc.api.Machine;
import li.cil.oc.api.machine.Signal;

class EventManager {
  Machine machine;
  Scriptable scope;
  EventManager(Machine machine, Scriptable scope){
    this.machine = machine;
    this.scope = scope;
  }
}
