package me.mrmakeit.ocjs;

import java.io.IOException;
import com.google.common.io.ByteStreams;

import li.cil.oc.api.Items;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.*;

import org.eclipse.wst.jsdt.debug.rhino.debugger.RhinoDebugger;

@Mod(modid = OCJS.MODID, version = OCJS.VERSION)
public class OCJS
{
    public static final String MODID = "ocjs";
    public static final String VERSION = "0.4.0";
    static final String rhino = "transport=socket,suspend=n,address=9000";
    public static RhinoDebugger debugger = new RhinoDebugger(rhino);
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
      try{
        debugger.start();
      }catch(Exception e){

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
        debugger.stop();
      }catch(Exception e){

      }
    }
}


