package com.telepathicgrunt.the_bumblezone.entities;

import com.telepathicgrunt.the_bumblezone.Bumblezone;
import com.telepathicgrunt.the_bumblezone.effects.WrathOfTheHiveEffect;
import com.telepathicgrunt.the_bumblezone.modcompat.ModChecker;
import com.telepathicgrunt.the_bumblezone.modinit.BzEffects;
import com.telepathicgrunt.the_bumblezone.modinit.BzItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistries;

public class BeeInteractivity {

    private static final ResourceLocation STICKY_HONEY_WAND = new ResourceLocation("buzzier_bees:sticky_honey_wand");
    private static final ResourceLocation BEE_SOUP = new ResourceLocation("buzzier_bees:bee_soup");
    private static final ResourceLocation HONEY_TREAT = new ResourceLocation("productivebees:honey_treat");

    // heal bees with sugar water bottle or honey bottle
    public static void beeFeeding(World world, PlayerEntity playerEntity, Hand hand, Entity target) {
        if (!world.isClientSide && target instanceof BeeEntity) {

            BeeEntity beeEntity = (BeeEntity) target;
            ItemStack itemstack = playerEntity.getItemInHand(hand);
            ResourceLocation itemRL = itemstack.getItem().getRegistryName();

            if (itemstack.getItem() == Items.HONEY_BOTTLE || itemstack.getItem() == BzItems.SUGAR_WATER_BOTTLE.get()) {

                world.playSound(
                        playerEntity,
                        playerEntity.getX(),
                        playerEntity.getY(),
                        playerEntity.getZ(),
                        SoundEvents.BOTTLE_EMPTY,
                        SoundCategory.NEUTRAL,
                        1.0F,
                        1.0F);

                if (itemstack.getItem() == Items.HONEY_BOTTLE) {

                    // Heal bee a lot
                    beeEntity.addEffect(new EffectInstance(
                            Effects.HEAL,
                            1,
                            1,
                            false,
                            false,
                            false));

                    // high chance to remove wrath of the hive from player
                    calmAndSpawnHearts(world, playerEntity, beeEntity, 0.3f, 3);

                }
                // Sugar water bottle
                else {
                    // Heal bee slightly but they remain angry
                    beeEntity.addEffect(new EffectInstance(
                            Effects.HEAL,
                            1,
                            0,
                            false,
                            false,
                            false));

                    // very low chance to remove wrath of the hive from player
                    calmAndSpawnHearts(world, playerEntity, beeEntity, 0.07f, 1);
                }

                if (!playerEntity.isCreative()) {

                    // remove current honey bottle
                    itemstack.shrink(1);

                    if (itemstack.isEmpty()) {
                        // places empty bottle in hand
                        playerEntity.setItemInHand(hand, new ItemStack(Items.GLASS_BOTTLE));
                    }
                    // places empty bottle in inventory
                    else if (!playerEntity.inventory.add(new ItemStack(Items.GLASS_BOTTLE))) {
                        // drops empty bottle if inventory is full
                        playerEntity.drop(new ItemStack(Items.GLASS_BOTTLE), false);
                    }
                }

                playerEntity.swing(hand, true);
            }
            else if (ModChecker.productiveBeesPresent && Bumblezone.BzModCompatibilityConfig.allowHoneyTreatCompat.get()
                    && itemRL != null && itemRL.equals(HONEY_TREAT))
            {

                // Heal bee a ton
                beeEntity.addEffect(new EffectInstance(Effects.HEAL, 2, 1, false, false, false));

                // very high chance to remove wrath of the hive from player
                calmAndSpawnHearts(world, playerEntity, beeEntity, 0.4f, 5);

                playerEntity.swing(hand, true);
            }
            else if (ModChecker.buzzierBeesPresent && itemRL != null &&
                    (itemRL.equals(BEE_SOUP) ||
                    (itemRL.equals(STICKY_HONEY_WAND) && Bumblezone.BzModCompatibilityConfig.allowHoneyWandCompat.get())))
            {

                // Heal bee a bit
                beeEntity.addEffect(new EffectInstance(Effects.HEAL, 1, 1, false, false, false));

                // neutral chance to remove wrath of the hive from player
                calmAndSpawnHearts(world, playerEntity, beeEntity, 0.3f, 3);


                if (itemRL.equals(BEE_SOUP)) {
                    consumeItem(playerEntity, hand, itemstack, Items.BOWL);
                }
                else if (itemRL.equals(STICKY_HONEY_WAND)) {
                    consumeItem(playerEntity, hand, itemstack, ForgeRegistries.ITEMS.getValue(STICKY_HONEY_WAND));
                }
            }
        }
    }

    private static void calmAndSpawnHearts(World world, PlayerEntity playerEntity, BeeEntity beeEntity, float calmChance, int hearts) {
        boolean calmed = world.random.nextFloat() < calmChance;
        if (calmed) {
            if(playerEntity.hasEffect(BzEffects.WRATH_OF_THE_HIVE.get())){
                playerEntity.removeEffect(BzEffects.WRATH_OF_THE_HIVE.get());
                WrathOfTheHiveEffect.calmTheBees(playerEntity.level, playerEntity);
            }
            else{
                playerEntity.addEffect(new EffectInstance(
                        BzEffects.PROTECTION_OF_THE_HIVE.get(),
                        Bumblezone.BzBeeAggressionConfig.howLongProtectionOfTheHiveLasts.get(),
                        2,
                        false,
                        false,
                        true));
            }
        }

        if (!beeEntity.isAngry() || calmed)
            ((ServerWorld) world).sendParticles(
                    ParticleTypes.HEART,
                    beeEntity.getX(),
                    beeEntity.getY(),
                    beeEntity.getZ(),
                    hearts,
                    world.getRandom().nextFloat() * 0.5 - 0.25f,
                    world.getRandom().nextFloat() * 0.2f + 0.2f,
                    world.getRandom().nextFloat() * 0.5 - 0.25f,
                    world.getRandom().nextFloat() * 0.4 + 0.2f);
    }

    private static void consumeItem(PlayerEntity playerEntity, Hand hand, ItemStack handItemstack, Item replacementItem) {
        if (!playerEntity.isCreative()) {

            // remove current bee soup
            handItemstack.shrink(1);

            if (handItemstack.isEmpty()) {
                // places empty bottle in hand
                playerEntity.setItemInHand(hand, new ItemStack(replacementItem));
            }
            // places empty bottle in inventory
            else if (!playerEntity.inventory.add(new ItemStack(replacementItem))) {
                // drops empty bottle if inventory is full
                playerEntity.drop(new ItemStack(replacementItem), false);
            }
        }
        else {
            playerEntity.swing(hand, true);
        }
    }
}