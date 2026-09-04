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

public class PotionRenderHandler extends Gui {

    private final Minecraft mc = Minecraft.getMinecraft();
    // Her etkilenmenin ilk başladığı maksimum süreyi saklayan harita
    private final Map<Integer, Integer> maxDurationMap = new HashMap<>();

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT || mc.thePlayer == null) {
            return;
        }

        // Oyuncunun üzerindeki tüm aktif etkileri (içilen, patlayan, fener, elma, dış etkenler) okuyoruz
        Collection<PotionEffect> activeEffects = mc.thePlayer.getActivePotionEffects();
        if (activeEffects.isEmpty()) {
            maxDurationMap.clear();
            return;
        }

        ScaledResolution sr = new ScaledResolution(mc);
        int x = sr.getScaledWidth() - 10; // Sağ tarafa hizalama
        int y = 10;

        for (PotionEffect effect : activeEffects) {
            int potionId = effect.getPotionID();
            int currentDuration = effect.getDuration();

            // Etki ilk kez geldiyse veya yenilendiyse başlangıç süresini kaydet
            if (!maxDurationMap.containsKey(potionId) || currentDuration > maxDurationMap.get(potionId)) {
                maxDurationMap.put(potionId, currentDuration);
            }

            int initialMaxDuration = maxDurationMap.get(potionId);

            // Dolum oranını hesapla (İksir süresi ne olursa olsun %100 tam dolu başlar)
            float progress = initialMaxDuration > 0 ? (float) currentDuration / initialMaxDuration : 0.0f;
            progress = Math.min(1.0f, Math.max(0.0f, progress));

            Potion potion = Potion.potionTypes[potionId];
            if (potion == null) continue;

            // İksir adı ve seviye okuması (Örn: Speed II)
            String effectName = I18n.format(potion.getName());
            if (effect.getAmplifier() > 0) {
                effectName += " " + I18n.format("enchantment.level." + (effect.getAmplifier() + 1));
            }

            // Dinamik Renk Belirleme (Yeşil -> Sarı -> Kırmızı)
            int barColor;
            if (progress > 0.50f) {
                barColor = 0xFF00FF00; // Yeşil (%50 ve üzeri)
            } else if (progress > 0.20f) {
                barColor = 0xFFFFFF00; // Sarı (%20 - %50 arası)
            } else {
                // %20 Altı: Kırmızı ve Yanıp Sönme (Kritik Uyarı Efekti)
                boolean blink = (mc.theWorld.getTotalWorldTime() % 10 < 5);
                barColor = blink ? 0xFFFF0000 : 0x88FF0000;
            }

            // Metin Çizimi
            int textWidth = mc.fontRendererObj.getStringWidth(effectName);
            int drawX = x - textWidth;
            mc.fontRendererObj.drawStringWithShadow(effectName, drawX, y, 0xFFFFFFFF);

            // 1-Pixel Dinamik Bar Çizimi
            int barWidth = textWidth;
            int currentBarWidth = (int) (barWidth * progress);
            int barY = y + mc.fontRendererObj.FONT_HEIGHT + 1;

            // Mat arka plan çizgisi ve dinamik renkli bar
            drawRect(drawX, barY, drawX + barWidth, barY + 1, 0x55000000);
            drawRect(drawX, barY, drawX + currentBarWidth, barY + 1, barColor);

            y += mc.fontRendererObj.FONT_HEIGHT + 6; // Bir sonraki etki için alt satıra geç
        }

        // Biten iksirleri hafızadan temizle
        maxDurationMap.keySet().removeIf(id -> mc.thePlayer.getActivePotionEffect(Potion.potionTypes[id]) == null);
    }
}
