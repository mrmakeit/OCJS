package me.mrmakeit.ocjs;

import java.io.IOException;

import javax.script.*;

import com.google.common.io.ByteStreams;

public class LoaderAPI{
  private boolean ready = false;
  private ScriptEngine engine;
  private SimpleBindings bindings;
  private static LoaderAPI loader = new LoaderAPI();

  private LoaderAPI(){
  }

  public static LoaderAPI get(){
    return loader;
  }

  public void getReady(){
    if(!ready){
      engine = new ScriptEngineManager().getEngineByName("nashorn");
      try{
        String coreCode = new String(ByteStreams.toByteArray(OCJS.class.getClassLoader().getResourceAsStream("core.js")));
        String babelCode = new String(ByteStreams.toByteArray(OCJS.class.getClassLoader().getResourceAsStream("babel.js")));
        String pluginCode = new String(ByteStreams.toByteArray(OCJS.class.getClassLoader().getResourceAsStream("evalPlugin.js")));
        bindings = new SimpleBindings();
        engine.eval(coreCode,bindings);
        engine.eval(babelCode,bindings);
        engine.eval(pluginCode,bindings);
      }catch(IOException e){
        System.err.println("Couldn't find babel.js");
      }catch(ScriptException e){
        System.err.println("Couldn't initialize babel "+e.getMessage());
      }finally{
        ready = true;
      }
    }
  }

  public boolean isReady(){
    return ready;
  }

  public String eval(String code) throws ScriptException{
    if(!this.ready){
      System.out.println("Babel processor not ready!");
      return code;
    }
    String result = "";
    bindings.put("input",code);
    result = (String) engine.eval("Babel.transform(input, { plugins:['babel-eval'], presets: [['es2015',{modules:false}]]}).code",bindings);
    return result;
  }
}
