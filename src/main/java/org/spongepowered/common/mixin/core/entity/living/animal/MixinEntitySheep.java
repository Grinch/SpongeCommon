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
package org.spongepowered.common.mixin.core.entity.living.animal;

import static org.spongepowered.api.data.DataQuery.of;

import net.minecraft.entity.passive.EntitySheep;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.manipulator.DyeableData;
import org.spongepowered.api.entity.living.animal.Sheep;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@NonnullByDefault
@Mixin(EntitySheep.class)
@Implements(@Interface(iface = Sheep.class, prefix = "sheep$"))
public abstract class MixinEntitySheep extends MixinEntityAnimal implements Sheep {

    @Shadow public abstract boolean getSheared();

    public boolean isSheared() {
        return this.getSheared();
    }

    public void setSheared(boolean sheared) {
        byte b0 = this.dataWatcher.getWatchableObjectByte(16);

        if (sheared) {
            this.dataWatcher.updateObject(16, (byte) (b0 | 16));
        } else {
            this.dataWatcher.updateObject(16, (byte) (b0 & -17));
        }
    }


    @Override
    public DyeableData getDyeData() {
        return getData(DyeableData.class).get();
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(of("Sheared"), getSheared())
                .set(of("Dye"), getDyeData());
    }
}
