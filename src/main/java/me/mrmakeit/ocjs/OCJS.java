package me.mrmakeit.ocjs;

import java.io.IOException;
import com.google.common.io.ByteStreams;

import li.cil.oc.api.Items;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.*;

@Mod(modid = OCJS.MODID, version = OCJS.VERSION)
public class OCJS
{
  public static final String MODID = "ocjs";
  public static final String VERSION = "0.5.1";
  static final String rhino = "transport=socket,suspend=n,address=9000";
  Class<?> clazz;
  public static Object debugger;

  @EventHandler
  public void init(FMLInitializationEvent event)
  {
    try {
      clazz = Class.forName("org.eclipse.wst.jsdt.debug.rhino.debugger.RhinoDebugger");
      debugger = clazz.getConstructor(String.class).newInstance(rhino);
      clazz.getMethod("start").invoke(debugger);
      System.out.println("[JAVASCRIPT] Debugging enabled.  Portn 9000");
    } catch (Throwable e) {
      System.out.println("[JAVASCRIPT] No debugging libraries available.  Not enabled");
    }

    li.cil.oc.api.Machine.add(JavascriptArch.class);

    try {
      byte[] code = ByteStreams.toByteArray(OCJS.class.getClassLoader().getResourceAsStream("bios.js"));
      Items.registerEEPROM("EEPROM (JS BIOS)", code , new byte[0], false);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  public void stopping(FMLServerStoppingEvent event)
  {
    try{
      clazz.getMethod("stop").invoke(debugger);
    }catch(Exception e){

    }
  }
}


