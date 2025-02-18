package com.telepathicgrunt.the_bumblezone.mixin.entities;

import com.telepathicgrunt.the_bumblezone.entities.BeeInteractivity;
import com.telepathicgrunt.the_bumblezone.entities.CreatingHoneySlime;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(PlayerEntity.class)
public class PlayerInteractsEntityMixin {
    // Feeding bees honey or sugar water
    // Or make honey slime
    @Inject(method = "interactOn",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z", ordinal = 1),
            cancellable = true)
    private void thebumblezone_onBeeFeeding(Entity entity, Hand hand, CallbackInfoReturnable<ActionResultType> cir) {
        if(entity instanceof BeeEntity) {
            if(BeeInteractivity.beeFeeding(entity.level, ((PlayerEntity)(Object)this), hand, (BeeEntity)entity) == ActionResultType.SUCCESS)
                cir.setReturnValue(ActionResultType.SUCCESS);
            else if(BeeInteractivity.beeUnpollinating(entity.level, ((PlayerEntity)(Object)this), hand, (BeeEntity)entity) == ActionResultType.SUCCESS)
                cir.setReturnValue(ActionResultType.SUCCESS);
        }
        else if (entity instanceof SlimeEntity) {
            if(CreatingHoneySlime.createHoneySlime(entity.level, ((PlayerEntity)(Object)this), hand, (SlimeEntity)entity) == ActionResultType.SUCCESS)
                cir.setReturnValue(ActionResultType.SUCCESS);
        }
    }
}