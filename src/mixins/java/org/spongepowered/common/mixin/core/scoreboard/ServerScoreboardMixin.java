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
package org.spongepowered.common.mixin.core.scoreboard;

import net.kyori.adventure.text.Component;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SDisplayObjectivePacket;
import net.minecraft.network.play.server.SScoreboardObjectivePacket;
import net.minecraft.network.play.server.STeamsPacket;
import net.minecraft.network.play.server.SUpdateScorePacket;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.ServerScoreboard.Action;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.registry.Registries;
import org.spongepowered.api.scoreboard.Team;
import org.spongepowered.api.scoreboard.criteria.Criterion;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlot;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.scoreboard.ScorePlayerTeamAccessor;
import org.spongepowered.common.accessor.scoreboard.ScoreboardAccessor;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.scoreboard.ScoreBridge;
import org.spongepowered.common.bridge.scoreboard.ScoreObjectiveBridge;
import org.spongepowered.common.bridge.scoreboard.ServerScoreboardBridge;
import org.spongepowered.common.scoreboard.SpongeDisplaySlot;
import org.spongepowered.common.scoreboard.SpongeObjective;
import org.spongepowered.common.scoreboard.SpongeScore;
import org.spongepowered.common.util.Constants;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

@SuppressWarnings({"ConstantConditions", "rawtypes"})
@Mixin(ServerScoreboard.class)
public abstract class ServerScoreboardMixin extends Scoreboard implements ServerScoreboardBridge {

    @Shadow protected abstract void shadow$setDirty();

    private final List<ServerPlayerEntity> impl$players = new ArrayList<>();

    // Update objective in display slot

    @Override
    public void bridge$updateDisplaySlot(@Nullable final Objective objective, final DisplaySlot displaySlot) throws IllegalStateException {
        if (objective != null && !objective.getScoreboards().contains(this)) {
            throw new IllegalStateException("Attempting to set an objective's display slot that does not exist on this scoreboard!");
        }
        final SimpleRegistry<DisplaySlot> registry = (SimpleRegistry<DisplaySlot>) Sponge.getGame().registries().registry(Registries.DISPLAY_SLOT);
        final int index = registry.getId(displaySlot);
        ((ScoreboardAccessor) this).accessor$displayObjectives()[index] = objective == null ? null: ((SpongeObjective) objective).getObjectiveFor(this);
        ((ServerScoreboardBridge) this).bridge$sendToPlayers(new SDisplayObjectivePacket(index, ((ScoreboardAccessor) this).accessor$displayObjectives()[index]));
    }

    // Get objectives

    @Override
    public void bridge$addObjective(final Objective objective) {
        final ScoreObjective nmsObjective = this.getObjective(objective.getName());

        if (nmsObjective != null) {
            throw new IllegalArgumentException(String.format("An objective with the name '%s' already exists!", objective.getName()));
        }
        final ScoreObjective scoreObjective = ((SpongeObjective) objective).getObjectiveFor(this);
        List<ScoreObjective> objectives = ((ScoreboardAccessor) this).accessor$objectivesByCriteria().get(objective.getCriterion());
        if (objectives == null) {
            objectives = new ArrayList<>();
            ((ScoreboardAccessor) this).accessor$objectivesByCriteria().put((ScoreCriteria) objective.getCriterion(), objectives);
        }

        objectives.add(scoreObjective);
        ((ScoreboardAccessor) this).accessor$objectivesByName().put(objective.getName(), scoreObjective);
        this.onObjectiveAdded(scoreObjective);

        ((SpongeObjective) objective).updateScores(this);
    }

    @Override
    public Optional<Objective> bridge$getObjective(final String name) {
        final ScoreObjective objective = this.getObjective(name);
        return Optional.ofNullable(objective == null ? null : ((ScoreObjectiveBridge) objective).bridge$getSpongeObjective());
    }

    @Override
    public Optional<Objective> bridge$getObjective(final DisplaySlot slot) {
        final SimpleRegistry<DisplaySlot> registry = (SimpleRegistry<DisplaySlot>) Sponge.getGame().registries().registry(Registries.DISPLAY_SLOT);
        final ScoreObjective objective = ((ScoreboardAccessor) this).accessor$displayObjectives()[registry.getId(slot)];
        if (objective != null) {
            return Optional.of(((ScoreObjectiveBridge) objective).bridge$getSpongeObjective());
        }
        return Optional.empty();
    }

    @Override
    public Set<Objective> bridge$getObjectivesByCriterion(final Criterion criterion) {
        if (((ScoreboardAccessor) this).accessor$objectivesByCriteria().containsKey(criterion)) {
            return ((ScoreboardAccessor) this).accessor$objectivesByCriteria().get(criterion).stream()
                .map(objective -> ((ScoreObjectiveBridge) objective).bridge$getSpongeObjective()).collect(Collectors.toSet());
        }
        return new HashSet<>();
    }

    @Override
    public void bridge$removeObjective(final Objective objective) {
        final ScoreObjective scoreObjective = ((SpongeObjective) objective).getObjectiveFor(this);
        ((ScoreboardAccessor) this).accessor$objectivesByName().remove(scoreObjective.getName());

        for (int i = 0; i < 19; ++i) {
            if (this.getDisplayObjective(i) == scoreObjective) {
                this.setDisplayObjective(i, null);
            }
        }

        ((ServerScoreboardBridge) this).bridge$sendToPlayers(new SScoreboardObjectivePacket(scoreObjective, Constants.Scoreboards.OBJECTIVE_PACKET_REMOVE));

        final List list = ((ScoreboardAccessor) this).accessor$objectivesByCriteria().get(scoreObjective.getCriteria());

        if (list != null)
        {
            list.remove(scoreObjective);
        }

        for (final Map<ScoreObjective, Score> scoreMap : ((ScoreboardAccessor) this).accessor$playerScores().values()) {
            final Score score = scoreMap.remove(scoreObjective);
            if (score != null) {
                ((ScoreBridge) score).bridge$getSpongeScore().removeScoreFor(scoreObjective);
            }
        }

        this.shadow$setDirty();

        ((SpongeObjective) objective).removeObjectiveFor(this);
    }

    // Add team

    @Override
    public void bridge$registerTeam(final Team spongeTeam) {
        final ScorePlayerTeam team = (ScorePlayerTeam) spongeTeam;
        if (this.getPlayerTeam(spongeTeam.getName()) != null) {
            throw new IllegalArgumentException(String.format("A team with the name '%s' already exists!", spongeTeam.getName()));
        }

        if (((ScorePlayerTeamAccessor) team).accessor$scoreboard() != null) {
            throw new IllegalArgumentException("The passed in team is already registered to a scoreboard!");
        }

        ((ScorePlayerTeamAccessor) team).accessor$scoreboard(this);
        ((ScoreboardAccessor) this).accessor$teamsByName().put(team.getName(), team);

        for (final String entry : team.getPlayers()) {
            this.addPlayerToTeam(entry, team);
        }
        this.onTeamAdded(team);
    }

    @Override
    public void bridge$sendToPlayers(final IPacket<?> packet) {
        for (final ServerPlayerEntity player: this.impl$players) {
            player.connection.send(packet);
        }
    }

    @Override
    public void bridge$addPlayer(final ServerPlayerEntity player, final boolean sendPackets) {
        this.impl$players.add(player);
        if (sendPackets) {
            for (final ScorePlayerTeam team : this.getPlayerTeams()) {
                player.connection.send(new STeamsPacket(team, 0));
            }

            for (final ScoreObjective objective : this.getObjectives()) {
                player.connection.send(new SScoreboardObjectivePacket(objective, 0));
                for (int i = 0; i < 19; ++i) {
                    if (this.getDisplayObjective(i) == objective) {
                        player.connection.send(new SDisplayObjectivePacket(i, objective));
                    }
                }
                for (final Score score : this.getPlayerScores(objective)) {
                    final SUpdateScorePacket packetIn = new SUpdateScorePacket(
                            Action.CHANGE,
                            score.getObjective().getName(),
                            score.getOwner(),
                            score.getScore());
                    player.connection.send(packetIn);
                }
            }
        }
    }

    @Override
    public void bridge$removePlayer(final ServerPlayerEntity player, final boolean sendPackets) {
        this.impl$players.remove(player);
        if (sendPackets) {
            this.impl$removeScoreboard(player);
        }
    }

    @Override
    public ScoreObjective addObjective(final String name, final ScoreCriteria criteria, final ITextComponent text,
            final ScoreCriteria.RenderType type) {
        final SpongeObjective objective = new SpongeObjective(name, (Criterion) criteria);
        objective.setDisplayMode((ObjectiveDisplayMode) (Object) type);
        objective.setDisplayName(SpongeAdventure.asAdventure(text));
        ((org.spongepowered.api.scoreboard.Scoreboard) this).addObjective(objective);
        return objective.getObjectiveFor(this);
    }

    @Override
    public void removeObjective(final ScoreObjective objective) {
        this.bridge$removeObjective(((ScoreObjectiveBridge) objective).bridge$getSpongeObjective());
    }

    @Override
    public void removePlayerTeam(final ScorePlayerTeam team) {
        super.removePlayerTeam(team);
        ((ScorePlayerTeamAccessor) team).accessor$scoreboard(null);
    }

    @Override
    public Score getOrCreatePlayerScore(final String name, final ScoreObjective objective) {
        return ((SpongeScore) ((ScoreObjectiveBridge) objective).bridge$getSpongeObjective().getOrCreateScore(SpongeAdventure.legacySection(name)))
                .getScoreFor(objective);
    }

    @Override
    public void resetPlayerScore(final String name, final ScoreObjective objective) {
        if (objective != null) {
            final SpongeObjective spongeObjective = ((ScoreObjectiveBridge) objective).bridge$getSpongeObjective();
            final Optional<org.spongepowered.api.scoreboard.Score> score = spongeObjective.getScore(SpongeAdventure.legacySection(name));
            if (score.isPresent()) {
                spongeObjective.removeScore(score.get());
            } else {
                SpongeCommon.getLogger().warn("Objective {} did have have the score", name);
            }
        } else {
            final Component textName = SpongeAdventure.legacySection(name);
            for (final ScoreObjective scoreObjective : this.getObjectives()) {
                ((ScoreObjectiveBridge) scoreObjective).bridge$getSpongeObjective().removeScore(textName);
            }
        }
    }

    @Redirect(method = "onScoreChanged",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerList;broadcastAll(Lnet/minecraft/network/IPacket;)V"))
    private void onUpdateScoreValue(final PlayerList manager, final IPacket<?> packet) {
        this.bridge$sendToPlayers(packet);
    }

    @Redirect(method = "onScoreChanged", at = @At(value = "INVOKE", target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z", remap = false))
    private boolean onUpdateScoreValue(final Set<?> set, final Object object) {
        return true;
    }

    @Redirect(method = "onPlayerRemoved",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerList;broadcastAll(Lnet/minecraft/network/IPacket;)V"))
    private void impl$updatePlayersOnRemoval(final PlayerList manager, final IPacket<?> packet) {
        this.bridge$sendToPlayers(packet);
    }

    @Redirect(method = "onPlayerScoreRemoved",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerList;broadcastAll(Lnet/minecraft/network/IPacket;)V"))
    private void impl$updatePlayersOnRemovalOfObjective(final PlayerList manager, final IPacket<?> packet) {
        this.bridge$sendToPlayers(packet);
    }

    //@Redirect(method = "setObjectiveInDisplaySlot", at = @At(value = "INVOKE", target = SEND_PACKET_METHOD))
    /*public void onSetObjectiveInDisplaySlot(PlayerList manager, Packet<?> packet) {
        this.sendToPlayers(packet);
    }*/

    @Inject(method = "onObjectiveAdded", at = @At("RETURN"))
    private void impl$UpdatePlayersScoreObjective(final ScoreObjective objective, final CallbackInfo ci) {
        this.bridge$sendToPlayers(new SScoreboardObjectivePacket(objective, Constants.Scoreboards.OBJECTIVE_PACKET_ADD));
    }

    /**
     * @author Aaron1011 - December 28th, 2015
     * @reason use our mixin scoreboard implementation.
     *
     * @param slot The slot of the display
     * @param objective The objective
     */
    @Override
    @Overwrite
    public void setDisplayObjective(final int slot, @Nullable final ScoreObjective objective) {
        final Objective apiObjective = objective == null ? null : ((ScoreObjectiveBridge) objective).bridge$getSpongeObjective();
        final MappedRegistry<DisplaySlot, Integer> registry = SpongeCommon.getRegistry().getCatalogRegistry().getRegistry(DisplaySlot.class);
        final DisplaySlot displaySlot = registry.getReverseMapping(slot);
        this.bridge$updateDisplaySlot(apiObjective, displaySlot);
    }

    @Redirect(method = "addPlayerToTeam",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerList;broadcastAll(Lnet/minecraft/network/IPacket;)V"))
    private void impl$updatePlayersOnPlayerAdd(final PlayerList manager, final IPacket<?> packet) {
        this.bridge$sendToPlayers(packet);
    }

    @Redirect(method = "removePlayerFromTeam",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerList;broadcastAll(Lnet/minecraft/network/IPacket;)V"))
    private void impl$updatePlayersOnPlayerRemoval(final PlayerList manager, final IPacket<?> packet) {
        this.bridge$sendToPlayers(packet);
    }

    @Redirect(method = "onObjectiveChanged",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerList;broadcastAll(Lnet/minecraft/network/IPacket;)V"))
    private void impl$updatePlayersOnObjectiveDisplay(final PlayerList manager, final IPacket<?> packet) {
        this.bridge$sendToPlayers(packet);
    }

    @Redirect(method = "onObjectiveChanged",
        at = @At(value = "INVOKE", target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z", remap = false))
    private boolean impl$alwaysReturnTrueForObjectivesDisplayName(final Set<ScoreObjective> set, final Object object) {
        return true;
    }

    @Redirect(method = "onObjectiveRemoved",
        at = @At(value = "INVOKE", target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z", remap = false))
    private boolean impl$alwaysReturnTrueForObjectiveRemoval(final Set<ScoreObjective> set, final Object object) {
        return true;
    }

    @Redirect(method = "onTeamAdded",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerList;broadcastAll(Lnet/minecraft/network/IPacket;)V"))
    private void impl$updateAllPlayersOnTeamCreation(final PlayerList manager, final IPacket<?> packet) {
        this.bridge$sendToPlayers(packet);
    }

    @Redirect(method = "onTeamChanged",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerList;broadcastAll(Lnet/minecraft/network/IPacket;)V"))
    private void impl$updateAllPlayersOnTeamInfo(final PlayerList manager, final IPacket<?> packet) {
        this.bridge$sendToPlayers(packet);
    }

    @Redirect(method = "onTeamRemoved",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerList;broadcastAll(Lnet/minecraft/network/IPacket;)V"))
    private void impl$updateAllPlayersOnTeamRemoval(final PlayerList manager, final IPacket<?> packet) {
        this.bridge$sendToPlayers(packet);
    }

    @Redirect(method = "startTrackingObjective",
        at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;", ordinal = 0, remap = false))
    private Iterator impl$useOurScoreboardForPlayers(final List list) {
        return this.impl$players.iterator();
    }

    @Redirect(method = "stopTrackingObjective",
        at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;", ordinal = 0, remap = false))
    private Iterator impl$useOurScoreboardForPlayersOnRemoval(final List list) {
        return this.impl$players.iterator();
    }

    private void impl$removeScoreboard(final ServerPlayerEntity player) {
        this.impl$removeTeams(player);
        this.impl$removeObjectives(player);
    }

    private void impl$removeTeams(final ServerPlayerEntity player) {
        for (final ScorePlayerTeam team : this.getPlayerTeams()) {
            player.connection.send(new STeamsPacket(team, 1));
        }
    }

    private void impl$removeObjectives(final ServerPlayerEntity player) {
        for (final ScoreObjective objective : this.getObjectives()) {
            player.connection.send(new SScoreboardObjectivePacket(objective, 1));
        }
    }
}
