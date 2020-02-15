package stevekung.mods.indicatia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import stevekung.mods.indicatia.config.ConfigManagerIN;
import stevekung.mods.indicatia.gui.api.GuiSkyBlockData;
import stevekung.mods.indicatia.renderer.LayerGlowingTexture;

@Mixin(RenderPlayer.class)
public abstract class RenderPlayerMixin
{
    private final RenderPlayer that = (RenderPlayer) (Object) this;

    @Shadow
    public abstract ModelPlayer getMainModel();

    @Inject(method = "<init>(Lnet/minecraft/client/renderer/entity/RenderManager;Z)V", at = @At("RETURN"))
    private void init(RenderManager renderManager, boolean useSmallArms, CallbackInfo info)
    {
        this.that.addLayer(new LayerGlowingTexture(this.getMainModel().bipedHead));
    }

    @Inject(method = "doRender(Lnet/minecraft/client/entity/AbstractClientPlayer;DDDFF)V", at = @At(value = "INVOKE", target = "net/minecraft/client/renderer/entity/RenderPlayer.setModelVisibilities(Lnet/minecraft/client/entity/AbstractClientPlayer;)V", shift = At.Shift.AFTER))
    private void renderPre(AbstractClientPlayer entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo info)
    {
        if (ConfigManagerIN.enableTransparentSkinRender)
        {
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        }
    }

    @Inject(method = "doRender(Lnet/minecraft/client/entity/AbstractClientPlayer;DDDFF)V", at = @At(value = "INVOKE", target = "net/minecraft/client/renderer/entity/RendererLivingEntity.doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V", shift = At.Shift.AFTER))
    private void renderPost(AbstractClientPlayer entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo info)
    {
        if (ConfigManagerIN.enableTransparentSkinRender)
        {
            GlStateManager.disableBlend();
        }
    }

    @Redirect(method = "setModelVisibilities(Lnet/minecraft/client/entity/AbstractClientPlayer;)V", at = @At(value = "INVOKE", target = "net/minecraft/client/entity/AbstractClientPlayer.isWearing(Lnet/minecraft/entity/player/EnumPlayerModelParts;)Z"))
    private boolean fixSecondLayer(AbstractClientPlayer clientPlayer, EnumPlayerModelParts part)
    {
        return GuiSkyBlockData.renderSecondLayer || clientPlayer.isWearing(part);
    }
}