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
package org.spongepowered.vanilla.launch.plugin;

import com.google.common.base.Preconditions;
import com.google.inject.Singleton;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.spongepowered.common.launch.plugin.SpongePluginManager;
import org.spongepowered.plugin.PluginContainer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Singleton
public final class VanillaPluginManager implements SpongePluginManager {

    private final Map<String, PluginContainer> plugins;
    private final Map<Object, PluginContainer> instancesToPlugins;
    private final List<PluginContainer> sortedPlugins;

    public VanillaPluginManager() {
        this.plugins = new Object2ObjectOpenHashMap<>();
        this.instancesToPlugins = new IdentityHashMap<>();
        this.sortedPlugins = new ArrayList<>();
    }

    @Override
    public Optional<PluginContainer> fromInstance(Object instance) {
        return Optional.ofNullable(this.instancesToPlugins.get(Preconditions.checkNotNull(instance)));
    }

    @Override
    public Optional<PluginContainer> getPlugin(String id) {
        return Optional.ofNullable(this.plugins.get(Preconditions.checkNotNull(id)));
    }

    @Override
    public Collection<PluginContainer> getPlugins() {
        return Collections.unmodifiableCollection(this.sortedPlugins);
    }

    @Override
    public boolean isLoaded(String id) {
        return this.plugins.containsKey(Preconditions.checkNotNull(id));
    }

    @Override
    public void addPlugin(PluginContainer plugin) {
        this.plugins.put(plugin.getMetadata().getId(), Preconditions.checkNotNull(plugin));
        this.instancesToPlugins.put(plugin.getInstance(), plugin);
        this.sortedPlugins.add(plugin);
    }
}
