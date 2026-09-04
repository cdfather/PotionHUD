package com.potionhud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PotionRenderHandler {

    private final Minecraft mc = Minecraft.getMinecraft();
    private final Map<Integer, Integer> maxDurationMap = new HashMap<>();

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return;
        if (mc.thePlayer == null || mc.gameSettings.showDebugInfo) return;

        Collection<PotionEffect> activeEffects = mc.thePlayer.getActivePotionEffects();
        if (activeEffects.isEmpty()) return;

        ScaledResolution sr = new ScaledResolution(mc);
        int screenWidth = sr.getScaledWidth();

        int x = screenWidth - 110;
        int y = 10;
        int barMaxWidth = 100;

        for (PotionEffect effect : activeEffects) {
            int potionId = effect.getPotionID();
            if (potionId < 0 || potionId >= Potion.potionTypes.length) continue;

            Potion potion = Potion.potionTypes[potionId];
            if (potion == null) continue;

            String effectName = I18n.format(potion.getName());
            if (effect.getAmplifier() == 1) {
                effectName += " II";
            } else if (effect.getAmplifier() == 2) {
                effectName += " III";
            } else if (effect.getAmplifier() == 3) {
                effectName += " IV";
            } else if (effect.getAmplifier() > 3) {
                effectName += " " + (effect.getAmplifier() + 1);
            }

            int currentDuration = effect.getDuration();
            if (!maxDurationMap.containsKey(potionId) || maxDurationMap.get(potionId) < currentDuration) {
                maxDurationMap.put(potionId, currentDuration);
            }
            int maxDuration = maxDurationMap.get(potionId);
            if (maxDuration <= 0) maxDuration = currentDuration;

            int totalSeconds = currentDuration / 20;
            String timeText = String.format("%02d:%02d", totalSeconds / 60, totalSeconds % 60);

            int timeColor = (totalSeconds < 10) ? 0xFF5555 : 0x55FF55;

            mc.fontRendererObj.drawStringWithShadow(effectName, x, y, 0xFFFFFF);

            int timeTextWidth = mc.fontRendererObj.getStringWidth(timeText);
            mc.fontRendererObj.drawStringWithShadow(timeText, x + barMaxWidth - timeTextWidth, y, timeColor);

            float ratio = Math.max(0.0f, Math.min(1.0f, (float) currentDuration / maxDuration));
            int currentBarWidth = (int) (barMaxWidth * ratio);

            int barY = y + 10;
            int color = potion.getLiquidColor() | 0xFF000000;

            Gui.drawRect(x, barY, x + barMaxWidth, barY + 1, 0x55000000);

            if (currentBarWidth > 0) {
                Gui.drawRect(x, barY, x + currentBarWidth, barY + 1, color);
            }

            y += 16;
        }
    }
}
