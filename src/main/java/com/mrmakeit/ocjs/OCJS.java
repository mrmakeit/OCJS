package com.mrmakeit.ocjs;

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
    }
}


