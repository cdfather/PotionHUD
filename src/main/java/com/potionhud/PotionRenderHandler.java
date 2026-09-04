package com.potionhud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PotionRenderHandler {

    private final Map<Integer, Integer> maxDurationMap = new HashMap<>();

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.HOTBAR) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.gameSettings.showDebugInfo) {
            return;
        }

        Collection<PotionEffect> activePotions = mc.thePlayer.getActivePotionEffects();
        if (activePotions.isEmpty()) {
            maxDurationMap.clear();
            return;
        }

        Set<Integer> currentKeys = new HashSet<>();
        for (PotionEffect effect : activePotions) {
            int key = effect.getPotionID() + (effect.getAmplifier() * 100);
            currentKeys.add(key);
            int currentDuration = effect.getDuration();
            if (!maxDurationMap.containsKey(key) || currentDuration > maxDurationMap.get(key)) {
                maxDurationMap.put(key, currentDuration);
            }
        }
        maxDurationMap.keySet().retainAll(currentKeys);

        FontRenderer fontRenderer = mc.fontRendererObj;
        ScaledResolution scaledResolution = new ScaledResolution(mc);
        int screenWidth = scaledResolution.getScaledWidth();

        int yPos = 10;
        int barWidth = 100; // Sabit bar genişliği

        GlStateManager.pushMatrix();
        GlStateManager.scale(1.0F, 1.0F, 1.0F);

        for (PotionEffect effect : activePotions) {
            Potion potion = Potion.potionTypes[effect.getPotionID()];
            if (potion == null) continue;

            String localizedName = net.minecraft.client.resources.I18n.format(potion.getName());
            if (effect.getAmplifier() > 0) {
                localizedName += " " + (effect.getAmplifier() + 1);
            }

            int durationInTicks = effect.getDuration();
            int totalSeconds = durationInTicks / 20;
            
            String durationText;
            if (totalSeconds >= 60) {
                int minutes = totalSeconds / 60;
                int seconds = totalSeconds % 60;
                durationText = String.format("%d:%02d", minutes, seconds);
            } else {
                durationText = totalSeconds + "s";
            }

            int textColor;
            if (durationInTicks > 600) {
                textColor = 0xFF55FF55; // Yeşil
            } else if (durationInTicks > 240) {
                textColor = 0xFFFFFF55; // Sarı
            } else {
                textColor = 0xFFFF5555; // Kırmızı
            }

            // Sağ üst köşeye sabitlemek için ekran genişliğini kullanıyoruz
            int xPos = screenWidth - barWidth - 10;

            // Metinleri ve sayacı barın üstüne yazdırıyoruz
            fontRenderer.drawStringWithShadow(localizedName, xPos, yPos, 0xFFFFFFFF);
            int durationWidth = fontRenderer.getStringWidth(durationText);
            fontRenderer.drawStringWithShadow(durationText, xPos + barWidth - durationWidth, yPos, textColor);

            int barY = yPos + fontRenderer.FONT_HEIGHT + 2;

            int key = effect.getPotionID() + (effect.getAmplifier() * 100);
            int maxDuration = maxDurationMap.getOrDefault(key, durationInTicks);
            if (maxDuration <= 0) maxDuration = 1;

            double progress = (double) durationInTicks / maxDuration;
            if (progress > 1.0) progress = 1.0;
            if (progress < 0.0) progress = 0.0;
            int filledWidth = (int) (barWidth * progress);

            // Arka plan ve doluluk barları
            drawRect(xPos, barY, xPos + barWidth, barY + 1, 0x50000000);
            drawRect(xPos, barY, xPos + filledWidth, barY + 1, textColor);

            yPos += 18; // Alt alta binen satırlar için boşluk ayarı
        }

        GlStateManager.popMatrix();
    }

    private void drawRect(int left, int top, int right, int bottom, int color) {
        net.minecraft.client.gui.Gui.drawRect(left, top, right, bottom, color);
    }
}
