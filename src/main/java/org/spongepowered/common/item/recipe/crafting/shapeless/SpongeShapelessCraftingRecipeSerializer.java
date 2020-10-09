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
package org.spongepowered.common.item.recipe.crafting.shapeless;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.common.item.recipe.SpongeRecipeRegistration;
import org.spongepowered.common.item.recipe.ingredient.IngredientUtil;
import org.spongepowered.common.item.recipe.ingredient.ResultUtil;
import org.spongepowered.common.util.Constants;

import java.util.function.Function;

/**
 * Custom ShapelessRecipe.Serializer with support for:
 * result full ItemStack instead of ItemType+Count
 * result functions
 * ingredient ItemStacks
 * remaining items function
 */
public class SpongeShapelessCraftingRecipeSerializer extends ShapelessRecipe.Serializer {

    public static IRecipeSerializer<?> SPONGE_CRAFTING_SHAPELESS = SpongeRecipeRegistration.register("crafting_shapeless", new SpongeShapelessCraftingRecipeSerializer());

    public ShapelessRecipe read(ResourceLocation recipeId, JsonObject json) {
        final String s = JSONUtils.getString(json, Constants.Recipe.GROUP, "");
        final NonNullList<Ingredient> nonnulllist = this.readIngredients(JSONUtils.getJsonArray(json, Constants.Recipe.SHAPELESS_INGREDIENTS));
        if (nonnulllist.isEmpty()) {
            throw new JsonParseException("No ingredients for shapeless recipe");
        }
        if (nonnulllist.size() > 9) {
            throw new JsonParseException("Too many ingredients for shapeless recipe");
        }
        final ItemStack itemstack = ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(json, Constants.Recipe.RESULT));
        final ItemStack spongeStack = ResultUtil.deserializeItemStack(json.getAsJsonObject(Constants.Recipe.SPONGE_RESULT));
        final Function<CraftingInventory, ItemStack> resultFunction = ResultUtil.deserializeResultFunction(json);
        final Function<CraftingInventory, NonNullList<ItemStack>> remainingItemsFunction = ResultUtil.deserializeRemainingItemsFunction(json);
        return new SpongeShapelessRecipe(recipeId, s, spongeStack == null ? itemstack : spongeStack, nonnulllist, resultFunction, remainingItemsFunction);
    }

    private NonNullList<Ingredient> readIngredients(JsonArray json) {
        final NonNullList<Ingredient> nonnulllist = NonNullList.create();
        for (JsonElement element : json) {
            final Ingredient ingredient = IngredientUtil.spongeDeserialize(element);
            if (!ingredient.hasNoMatchingItems()) {
                nonnulllist.add(ingredient);
            }
        }
        return nonnulllist;
    }

    @Override
    public ShapelessRecipe read(ResourceLocation p_199426_1_, PacketBuffer p_199426_2_) {
        throw new UnsupportedOperationException("custom serializer needs client side support");
    }

    @Override
    public void write(PacketBuffer p_199427_1_, ShapelessRecipe p_199427_2_) {
        throw new UnsupportedOperationException("custom serializer needs client side support");
    }
}