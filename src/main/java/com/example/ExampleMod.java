package com.example;

/*
 * MIT License
 *
 * Copyright (c) 2024 明天会更好
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleMod implements ModInitializer {
    public static final String MOD_ID = "endless_items";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Item SIMPLE_SWORD = new SwordItem(ToolMaterials.NETHERITE, 3, -2.4F, new Item.Settings().fireproof()) {
        @Override
        public boolean hasGlint(ItemStack stack) {
            return true;
        }

        @Override
        public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
            if (!target.getWorld().isClient && target instanceof LivingEntity) {
                target.setHealth(0.0F);
            }
            return super.postHit(stack, target, attacker);
        }

        @Override
        public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
            if (!world.isClient && entity instanceof PlayerEntity player) {
                boolean holdingSword = player.getMainHandStack().getItem() == this || player.getOffHandStack().getItem() == this;
                
                if (holdingSword) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 40, 255, false, false));
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 40, 255, false, false));
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 40, 255, false, false));
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 40, 255, false, false));
                    
                    if (!player.getAbilities().allowFlying) {
                        player.getAbilities().allowFlying = true;
                        player.sendAbilitiesUpdate();
                    }
                } else if (!player.isCreative() && player.getAbilities().allowFlying) {
                    player.getAbilities().allowFlying = false;
                    player.getAbilities().flying = false;
                    player.sendAbilitiesUpdate();
                }
            }
        }
        @Override
        public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
            if (!world.isClient) {
                ServerWorld serverWorld = (ServerWorld) world;
                int mobCount = 0;
                
                for (Entity entity : serverWorld.iterateEntities()) {
                    if (entity instanceof MobEntity mob && entity != user) {
                        mob.discard();
                        mobCount++;
                    }
                }
                
                user.sendMessage(Text.literal("清除了 " + mobCount + " 个生物"), true);
            }
            return TypedActionResult.success(user.getStackInHand(hand), true);
        }
    };

    public static final Block STREET_BLOCK = new Block(
        AbstractBlock.Settings.copy(Blocks.STONE).strength(1.5f)
    );

    public static final Block TEST_STONE = new Block(
        AbstractBlock.Settings.copy(Blocks.STONE)
            .strength(-1.0f, 3600000.0f)
            .dropsNothing()
    ) {
        @Override
        public float getHardness() {
            return -1.0f;
        }
    };

    public static final BlockItem TEST_STONE_ITEM = new BlockItem(TEST_STONE, new Item.Settings()) {
        @Override
        public boolean hasGlint(ItemStack stack) {
            return true;
        }
    };
    public static final RegistryKey<ItemGroup> ENDLESS_ITEMS_GROUP = RegistryKey.of(RegistryKeys.ITEM_GROUP, new Identifier(MOD_ID, "endless_items"));

    @Override
    public void onInitialize() {
        final ItemGroup ENDLESS_GROUP = FabricItemGroup.builder()
            .displayName(Text.translatable("itemGroup.endless_items.endless_items"))
            .icon(() -> new ItemStack(SIMPLE_SWORD))
            .build();

        Registry.register(Registries.ITEM_GROUP, ENDLESS_ITEMS_GROUP, ENDLESS_GROUP);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "simple_sword"), SIMPLE_SWORD);
        Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "street_block"), STREET_BLOCK);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "street_block"), new BlockItem(STREET_BLOCK, new Item.Settings()));
        Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "test_stone"), TEST_STONE);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "test_stone"), TEST_STONE_ITEM);

        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (entity instanceof PlayerEntity player) {
                if (player.getMainHandStack().getItem() == SIMPLE_SWORD || player.getOffHandStack().getItem() == SIMPLE_SWORD) {
                    return false;
                }
            }
            return true;
        });

        ItemGroupEvents.modifyEntriesEvent(ENDLESS_ITEMS_GROUP).register(entries -> {
            entries.add(SIMPLE_SWORD);
            entries.add(STREET_BLOCK.asItem());
            entries.add(TEST_STONE.asItem());
        });

        LOGGER.info("\n" +
            "╔══════════════════════════════════════╗\n" +
            "║             无 尽 物 品              ║\n" +
            "╠══════════════════════════════════════╣\n" +
            "║ Endless Items Mod v1.0.2            ║\n" +
            "║ 添加了3个特殊物品                   ║\n" +
            "║ Author: 明天会更好                  ║\n" +
            "║ © 2024 保留所有权限                 ║\n" +
            "╚══════════════════════════════════════╝\n" +
            "模组加载成功！"
        );
    }
}
