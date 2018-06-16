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
  public static final String VERSION = "0.6.3";
  boolean enableBabel = false;

  @EventHandler
  public void init(FMLInitializationEvent event)
  {
    LoaderAPI.get().getReady();
    li.cil.oc.api.Machine.add(NashornArch.class);
    if(enableBabel){
      if(LoaderAPI.get().isReady()){
        li.cil.oc.api.Machine.add(NashornBabelArch.class);
      }
    }

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
  }
}


