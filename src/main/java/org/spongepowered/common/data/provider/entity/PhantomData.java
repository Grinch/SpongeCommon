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

import net.minecraft.entity.monster.PhantomEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.PhantomPhase;
import org.spongepowered.common.accessor.entity.monster.PhantomEntityAccessor;
import org.spongepowered.common.data.provider.DataProviderRegistrator;

public final class PhantomData {

    private PhantomData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(PhantomEntity.class)
                    .create(Keys.SCALE)
                        .get(h -> (double) (h.getWidth() + (0.2F * (float) h.getPhantomSize())) / h.getWidth())
                    .create(Keys.SIZE)
                        .get(PhantomEntity::getPhantomSize)
                        .set(PhantomEntity::setPhantomSize)
                .asMutable(PhantomEntityAccessor.class)
                    .create(Keys.PHANTOM_PHASE)
                        .get(h -> (PhantomPhase) (Object) h.accessor$getAttackPhase())
                        .set((h, v) -> h.accessor$setAttackPhase((PhantomEntity.AttackPhase) (Object) v));
    }
    // @formatter:on
}
