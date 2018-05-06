package me.mrmakeit.ocjs;

import javax.script.*;

import li.cil.oc.api.machine.*;

public class LoaderAPI{
  Machine machine;
  NashornAPI vm;
  ScriptEngine engine;
  SimpleBindings bindings;

  LoaderAPI(Machine machine, ScriptEngine engine, SimpleBindings bindings){
    this.machine = machine;
    this.bindings = bindings;
    this.engine = engine;
  }

  public String eval(String code){
    System.out.println("Babel Processing");
    String result = "";
    try{
      bindings.put("input",code);
      result = (String)engine.eval("Babel.transform(input,{presets:['es2015']}).code",bindings);
    }catch (ScriptException e){
      machine.crash("Error processing babel.transform "+e.getMessage());
    }
    System.out.println("Babel Done");
    System.out.println(result);
    return result;
  }
}
