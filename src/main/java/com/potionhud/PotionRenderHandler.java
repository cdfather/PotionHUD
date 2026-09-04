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

public class PotionRenderHandler {

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
            return;
        }

        FontRenderer fontRenderer = mc.fontRendererObj;
        ScaledResolution scaledResolution = new ScaledResolution(mc);
        int screenWidth = scaledResolution.getScaledWidth();

        int yPos = 10; // Sağ üst köşe için başlangıç Y koordinatı

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
            
            // Dakika ve Saniye formatı mantığı
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

            // Metinlerin toplam genişliğini hesapla (Sağdan hizalama için)
            int nameWidth = fontRenderer.getStringWidth(localizedName);
            int durationWidth = fontRenderer.getStringWidth(durationText);
            int totalWidth = nameWidth + 6 + durationWidth;

            // X pozisyonunu sağ üst köşeye göre ayarla (Kenardan 10 piksel içeride)
            int xPos = screenWidth - totalWidth - 10;

            fontRenderer.drawStringWithShadow(localizedName, xPos, yPos, 0xFFFFFFFF);
            fontRenderer.drawStringWithShadow(durationText, xPos + nameWidth + 6, yPos, textColor);

            int barWidth = totalWidth;
            int barY = yPos + fontRenderer.FONT_HEIGHT + 2;

            int maxDuration = 1200; 
            double progress = (double) durationInTicks / maxDuration;
            if (progress > 1.0) progress = 1.0;
            int filledWidth = (int) (barWidth * progress);

            drawRect(xPos, barY, xPos + barWidth, barY + 1, 0x50000000);
            drawRect(xPos, barY, xPos + filledWidth, barY + 1, textColor);

            yPos += 14;
        }

        GlStateManager.popMatrix();
    }

    private void drawRect(int left, int top, int right, int bottom, int color) {
        net.minecraft.client.gui.Gui.drawRect(left, top, right, bottom, color);
    }
}
