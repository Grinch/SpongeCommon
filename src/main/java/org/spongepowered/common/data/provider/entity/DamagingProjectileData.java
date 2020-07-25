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

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.DamagingProjectileEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.projectile.source.ProjectileSource;
import org.spongepowered.common.accessor.entity.projectile.DamagingProjectileEntityAccessor;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.math.vector.Vector3d;

public final class DamagingProjectileData {

    private DamagingProjectileData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(DamagingProjectileEntity.class)
                    .create(Keys.ACCELERATION)
                        .get(h -> new Vector3d(h.accelerationX, h.accelerationY, h.accelerationZ))
                        .set((h, v) -> {
                            ((DamagingProjectileEntityAccessor) h).accessor$setAccelerationX(v.getX());
                            ((DamagingProjectileEntityAccessor) h).accessor$setAccelerationY(v.getY());
                            ((DamagingProjectileEntityAccessor) h).accessor$setAccelerationZ(v.getZ());
                        })
                    .create(Keys.SHOOTER)
                        .get(h -> (ProjectileSource) h.shootingEntity)
                        .set((h, v) -> h.shootingEntity = (LivingEntity) v);
    }
    // @formatter:on
}
