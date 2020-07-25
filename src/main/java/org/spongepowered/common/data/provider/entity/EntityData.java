/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.data.provider.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.common.accessor.entity.EntityAccessor;
import org.spongepowered.common.bridge.entity.EntityBridge;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.VecHelper;

import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public final class EntityData {

    private EntityData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(Entity.class)
                    .create(Keys.AGE)
                        .get(h -> h.ticksExisted)
                        .set((h, v) -> h.ticksExisted = v)
                    .create(Keys.BASE_SIZE)
                        .get(h -> (double) h.getWidth())
                    .create(Keys.BASE_VEHICLE)
                        .get(h -> (org.spongepowered.api.entity.Entity) h.getLowestRidingEntity())
                    .create(Keys.EYE_HEIGHT)
                        .get(h -> (double) h.getEyeHeight())
                    .create(Keys.EYE_POSITION)
                        .get(h -> VecHelper.toVector3d(h.getEyePosition(1f)))
                    .create(Keys.FIRE_DAMAGE_DELAY)
                        .get(h -> ((EntityAccessor) h).accessor$getFireImmuneTicks())
                        .setAnd((h, v) -> {
                            ((EntityBridge) h).bridge$setFireImmuneTicks(v);
                            return ((EntityAccessor) h).accessor$getFireImmuneTicks() == v;
                        })
                    .create(Keys.FIRE_TICKS)
                        .get(h -> ((EntityAccessor) h).accessor$getFire() > 0 ? ((EntityAccessor) h).accessor$getFire() : null)
                        .set((h, v) -> ((EntityAccessor) h).accessor$setFire(Math.max(v, Constants.Entity.MINIMUM_FIRE_TICKS)))
                        .deleteAndGet(h -> {
                            final EntityAccessor accessor = ((EntityAccessor) h);
                            if (accessor.accessor$getFire() < Constants.Entity.MINIMUM_FIRE_TICKS) {
                                return DataTransactionResult.failNoData();
                            }
                            final DataTransactionResult.Builder dtrBuilder = DataTransactionResult.builder();
                            ((GenericMutableDataProvider<EntityAccessor, Integer>) accessor).getValueFrom(accessor).map(Value::asImmutable).ifPresent(dtrBuilder::replace);
                            ((DataHolder) h).getValue(Keys.FIRE_DAMAGE_DELAY).map(Value::asImmutable).map(dtrBuilder::replace);
                            h.extinguish();
                            return dtrBuilder.result(DataTransactionResult.Type.SUCCESS).build();
                        })
                    .create(Keys.HEIGHT)
                        .get(h -> (double) h.getHeight())
                    .create(Keys.INVULNERABILITY_TICKS)
                        .get(h -> h.hurtResistantTime)
                        .set((h, v) -> {
                            h.hurtResistantTime = v;
                            if (h instanceof LivingEntity) {
                                ((LivingEntity) h).hurtTime = v;
                            }
                        })
                    .create(Keys.IS_CUSTOM_NAME_VISIBLE)
                        .get(Entity::isCustomNameVisible)
                        .set(Entity::setCustomNameVisible)
                    .create(Keys.IS_FLYING)
                        .get(h -> h.isAirBorne)
                        .set((h, v) -> h.isAirBorne = v)
                    .create(Keys.IS_GLOWING)
                        .get(Entity::isGlowing)
                        .set(Entity::setGlowing)
                    .create(Keys.IS_GRAVITY_AFFECTED)
                        .get(h -> !h.hasNoGravity())
                        .set((h, v) -> h.setNoGravity(!v))
                    .create(Keys.IS_SNEAKING)
                        .get(Entity::isSneaking)
                        .set(Entity::setSneaking)
                    .create(Keys.IS_SPRINTING)
                        .get(Entity::isSprinting)
                        .set(Entity::setSprinting)
                    .create(Keys.IS_SILENT)
                        .get(Entity::isSilent)
                        .set(Entity::setSilent)
                    .create(Keys.IS_WET)
                        .get(Entity::isWet)
                    .create(Keys.ON_GROUND)
                        .get(h -> h.onGround)
                    .create(Keys.PASSENGERS)
                        .get(h -> h.getPassengers().stream().map(org.spongepowered.api.entity.Entity.class::cast).collect(Collectors.toList()))
                        .set((h, v) -> {
                            h.getPassengers().clear();
                            v.forEach(v1 -> h.getPassengers().add((Entity) v1));
                        })
                    .create(Keys.SCALE)
                        .get(h -> 1d)
                    .create(Keys.SCOREBOARD_TAGS)
                        .get(Entity::getTags)
                        .set((h, v) -> {
                            h.getTags().clear();
                            h.getTags().addAll(v);
                        })
                    .create(Keys.VEHICLE)
                        .get(h -> (org.spongepowered.api.entity.Entity) h.getRidingEntity())
                        .set((h, v) -> h.startRiding((Entity) v, true))
                    .create(Keys.VELOCITY)
                        .get(h -> VecHelper.toVector3d(h.getMotion()))
                        .set((h, v) -> h.setMotion(VecHelper.toVec3d(v)))
                .asMutable(EntityBridge.class)
                    .create(Keys.DISPLAY_NAME)
                        .defaultValue(null)
                        .get(EntityBridge::bridge$getDisplayNameText)
                        .set(EntityBridge::bridge$setDisplayName);
    }
    // @formatter:on
}
