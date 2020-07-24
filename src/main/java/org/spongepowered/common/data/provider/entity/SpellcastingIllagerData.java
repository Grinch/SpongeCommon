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

import net.minecraft.entity.monster.SpellcastingIllagerEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.SpellType;
import org.spongepowered.common.accessor.entity.monster.SpellcastingIllagerEntityAccessor;
import org.spongepowered.common.data.provider.DataProviderRegistrator;

public final class SpellcastingIllagerData {

    private SpellcastingIllagerData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(SpellcastingIllagerEntityAccessor.class)
                    .create(Keys.CASTING_TIME)
                        .get(SpellcastingIllagerEntityAccessor::accessor$getSpellTicks)
                        .set(SpellcastingIllagerEntityAccessor::accessor$setSpellTicks)
                .asMutable(SpellcastingIllagerEntity.class)
                    .create(Keys.CURRENT_SPELL)
                        .get(h -> (SpellType) (Object) ((SpellcastingIllagerEntityAccessor) h).accessor$getSpellType())
                        .set((h, v) -> h.setSpellType((SpellcastingIllagerEntity.SpellType) (Object) v));
    }
    // @formatter:on
}
