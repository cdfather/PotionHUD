package com.potionhud;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(
    modid = PotionHUD.MODID,
    name = PotionHUD.NAME,
    version = PotionHUD.VERSION,
    acceptedMinecraftVersions = "[1.8.9]"
)
public class PotionHUD {
    public static final String MODID = "potionhud";
    public static final String NAME = "1-Pixel Potion HUD";
    public static final String VERSION = "1.0.0";

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new PotionRenderHandler());
    }
}
