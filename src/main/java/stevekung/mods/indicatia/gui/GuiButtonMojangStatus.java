//package stevekung.mods.indicatia.gui;
//
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.Gui;
//import net.minecraft.client.gui.GuiButton;
//import net.minecraft.client.renderer.GlStateManager;
//import net.minecraft.util.ResourceLocation;
//import net.minecraftforge.api.distmarker.Dist;
//import net.minecraftforge.api.distmarker.OnlyIn;
//
//@OnlyIn(Dist.CLIENT)
//public class GuiButtonMojangStatus extends GuiButton
//{
//    private static final ResourceLocation MOJANG_TEXTURES = new ResourceLocation("indicatia:textures/gui/mojang.png");
//
//    public GuiButtonMojangStatus(int buttonID, int xPos, int yPos)
//    {
//        super(buttonID, xPos, yPos, 20, 20, "");
//    }
//
//    @Override
//    public void render(int mouseX, int mouseY, float partialTicks)
//    {
//        if (this.visible)
//        {
//            Minecraft.getInstance().getTextureManager().bindTexture(GuiButtonMojangStatus.MOJANG_TEXTURES);
//            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
//            boolean flag = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
//            Gui.drawModalRectWithCustomSizedTexture(this.x, this.y, flag ? 20 : 0, 0, this.width, this.height, 40, 20);
//        }
//    }
//}