package stevekung.mods.indicatia.gui.toasts;

import java.nio.FloatBuffer;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.MathHelper;
import stevekung.mods.indicatia.utils.ColorUtils;

public class GuiToast extends Gui
{
    protected final Minecraft mc;
    private final GuiToast.ToastInstance<?>[] visible = new GuiToast.ToastInstance[5];
    private final Deque<IToast> toastsQueue = new ArrayDeque<>();

    public GuiToast(Minecraft mc)
    {
        this.mc = mc;
    }

    public void drawToast(ScaledResolution resolution)
    {
        if (!this.mc.gameSettings.hideGUI && !this.mc.gameSettings.showDebugInfo)
        {
            RenderHelper.disableStandardItemLighting();

            for (int i = 0; i < this.visible.length; ++i)
            {
                GuiToast.ToastInstance<?> toastinstance = this.visible[i];

                if (toastinstance != null && toastinstance.render(resolution.getScaledWidth(), i))
                {
                    this.visible[i] = null;
                }
                if (this.visible[i] == null && !this.toastsQueue.isEmpty())
                {
                    this.visible[i] = new GuiToast.ToastInstance(this.toastsQueue.removeFirst());
                }
            }
        }
    }

    public static void drawLongItemName(GuiToast toastGui, long delta, long firstDrawTime, FloatBuffer buffer, String itemName)
    {
        int x = 30;
        int textWidth = toastGui.mc.fontRendererObj.getStringWidth(itemName);
        int maxSize = textWidth - 135;
        long timeElapsed = delta - firstDrawTime - 500L;
        long textSpeed = 8000L;

        if (timeElapsed > 0 && textWidth > maxSize && textWidth > 120)
        {
            x = Math.max((int) (-textWidth * timeElapsed / textSpeed + x), -maxSize + 16);
        }

        ScaledResolution res = new ScaledResolution(toastGui.mc);
        double height = res.getScaledHeight();
        double scale = res.getScaleFactor();
        float[] trans = new float[16];

        buffer.clear();
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, buffer);
        buffer.get(trans);
        float xpos = trans[12];

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor((int) ((xpos + 29) * scale), (int) ((height - 195) * scale), (int) (126 * scale), (int) (195 * scale));
        toastGui.mc.fontRendererObj.drawString(itemName, x, 18, ColorUtils.rgbToDecimal(255, 255, 255));
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    @Nullable
    public <T extends IToast> T getToast(Class<? extends T> clazz, Object obj)
    {
        for (GuiToast.ToastInstance<?> ins : this.visible)
        {
            if (ins != null && clazz.isAssignableFrom(ins.getToast().getClass()) && ins.getToast().getType().equals(obj))
            {
                return (T)ins.getToast();
            }
        }
        for (IToast toast : this.toastsQueue)
        {
            if (clazz.isAssignableFrom(toast.getClass()) && toast.getType().equals(obj))
            {
                return (T)toast;
            }
        }
        return null;
    }

    public void clear()
    {
        Arrays.fill(this.visible, null);
        this.toastsQueue.clear();
    }

    public boolean add(IToast toast)
    {
        return this.toastsQueue.add(toast);
    }

    class ToastInstance<T extends IToast>
    {
        private final T toast;
        private long animationTime;
        private long visibleTime;
        private IToast.Visibility visibility;

        private ToastInstance(T toast)
        {
            this.animationTime = -1L;
            this.visibleTime = -1L;
            this.visibility = IToast.Visibility.SHOW;
            this.toast = toast;
        }

        public T getToast()
        {
            return this.toast;
        }

        private float getVisibility(long delta)
        {
            float f = MathHelper.clamp_float((delta - this.animationTime) / 600.0F, 0.0F, 1.0F);
            f = f * f;
            return this.visibility == IToast.Visibility.HIDE ? 1.0F - f : f;
        }

        public boolean render(int x, int z)
        {
            long i = Minecraft.getSystemTime();

            if (this.animationTime == -1L)
            {
                this.animationTime = i;
                this.visibility.playSound(GuiToast.this.mc.getSoundHandler());
            }
            if (this.visibility == IToast.Visibility.SHOW && i - this.animationTime <= 600L)
            {
                this.visibleTime = i;
            }

            float chatOffset = Minecraft.getMinecraft().ingameGUI.getChatGUI().getChatOpen() ? 41.0F : 0.0F;
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.translate(0.0F, chatOffset, 0.0F);
            GlStateManager.translate(x - 160.0F * this.getVisibility(i), z * 32, 500 + z);
            IToast.Visibility itoast$visibility = this.toast.draw(GuiToast.this, i - this.visibleTime);
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
            GlStateManager.enableAlpha();

            if (itoast$visibility != this.visibility)
            {
                this.animationTime = i - (int)((1.0F - this.getVisibility(i)) * 600.0F);
                this.visibility = itoast$visibility;
                this.visibility.playSound(GuiToast.this.mc.getSoundHandler());
            }
            return this.visibility == IToast.Visibility.HIDE && i - this.animationTime > 600L;
        }
    }
}