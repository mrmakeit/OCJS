package me.mrmakeit.ocjs;

import java.io.IOException;
import com.google.common.io.ByteStreams;

import li.cil.oc.api.Items;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;

@Mod(modid = OCJS.MODID, version = OCJS.VERSION)
public class OCJS
{
    public static final String MODID = "OCJS";
    public static final String VERSION = "1.0";
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    	li.cil.oc.api.Machine.add(JavascriptArch.class);
    	try {
			byte[] code = ByteStreams.toByteArray(OCJS.class.getClassLoader().getResourceAsStream("bios.js"));
			Items.registerEEPROM("EEPROM (JS BIOS)", code , new byte[0], false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}


