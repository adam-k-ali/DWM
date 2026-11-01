package com.adamkali.dwm.world.event;

import com.adamkali.dwm.entity.DWMEntityTypes;
import com.adamkali.dwm.entity.DalekEntity;
import com.adamkali.dwm.entity.ThalEntity;
import com.adamkali.dwm.entity.ThalSpawnRules;
import com.adamkali.dwm.world.SkaroDimensions;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

/**
 * Random and commanded Dalek/Thal skirmishes near Thal settlements. State is in memory only: a restart
 * simply forgets the battle (see {@code docs/feature-dalek-thal-battles.md}).
 */
public final class DalekThalBattleService {
    private static final List<Battle> ACTIVE = new ArrayList<>();
    private static long lastEndTick = Long.MIN_VALUE / 2;

    private DalekThalBattleService() {
    }

    /** One running skirmish: the entities it spawned or armed, tracked by UUID. */
    private static final class Battle {
        final ServerLevel level;
        final BlockPos center;
        final long startTick;
        final Set<UUID> daleks = new HashSet<>();
        final Set<UUID> thals = new HashSet<>();
        /** Thals this battle spawned and must remove if they survive. */
        final Set<UUID> reinforcements = new HashSet<>();

        Battle(ServerLevel level, BlockPos center, long startTick) {
            this.level = level;
            this.center = center;
            this.startTick = startTick;
        }
    }

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(DalekThalBattleService::onEndTick);
    }

    static void onEndTick(MinecraftServer server) {
        long now = server.getTickCount();
        tickBattles(now);
        if (now % BattleEventLogic.TICK_INTERVAL != 0) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!(player.level() instanceof ServerLevel level) || !SkaroDimensions.isSkaroWorld(level)) {
                continue;
            }
            boolean nearSettlement = nearSettlement(level, player);
            double roll = level.getRandom().nextDouble();
            if (BattleEventLogic.shouldTrigger(nearSettlement, isActive(), now, lastEndTick, roll)) {
                start(level, player.blockPosition(), now);
            }
        }
    }

    public static boolean isActive() {
        return !ACTIVE.isEmpty();
    }

    /** Ends every running skirmish immediately. Returns how many were stopped. */
    public static int stopAll(long now) {
        int stopped = ACTIVE.size();
        for (Battle battle : new ArrayList<>(ACTIVE)) {
            finish(battle, BattleEventLogic.Outcome.TIMED_OUT, now);
        }
        return stopped;
    }

    private static boolean nearSettlement(ServerLevel level, ServerPlayer player) {
        AABB area = player.getBoundingBox().inflate(BattleEventLogic.SETTLEMENT_SEARCH_RADIUS);
        for (ThalEntity thal : level.getEntitiesOfClass(ThalEntity.class, area)) {
            if (ThalSpawnRules.isInsideSettlement(level, thal.blockPosition())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Starts a skirmish around {@code center}: arms nearby resident Thals, adds reinforcements and sends a Dalek
     * squad in from 40-60 blocks out. Used by the random trigger and by {@code /dwm battle start}.
     */
    public static int start(ServerLevel level, BlockPos center, long now) {
        Battle battle = new Battle(level, center, now);
        var random = level.getRandom();

        AABB area = new AABB(center).inflate(BattleEventLogic.ARM_RADIUS);
        for (ThalEntity thal : level.getEntitiesOfClass(ThalEntity.class, area)) {
            if (!thal.isArmed()) {
                thal.setArmed(true);
                battle.thals.add(thal.getUUID());
            }
        }
        int reinforcements = BattleEventLogic.reinforcements(random.nextDouble());
        for (int i = 0; i < reinforcements; i++) {
            ThalEntity thal = spawnNear(level, DWMEntityTypes.THAL, center, 6, 12);
            if (thal != null) {
                thal.setArmed(true);
                thal.setPersistenceRequired();
                battle.thals.add(thal.getUUID());
                battle.reinforcements.add(thal.getUUID());
            }
        }

        int squad = BattleEventLogic.daleks(random.nextDouble());
        for (int i = 0; i < squad; i++) {
            BattleEventLogic.Offset offset = BattleEventLogic.spawnOffset(random.nextDouble(), random.nextDouble());
            BlockPos spot = center.offset(offset.dx(), 0, offset.dz());
            if (!level.isLoaded(spot)) {
                continue;
            }
            EntityType<? extends DalekEntity> type = i % 2 == 0 ? DWMEntityTypes.DALEK : DWMEntityTypes.PARADIGM_DALEK;
            DalekEntity dalek = type.spawn(level, level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, spot), EntitySpawnReason.EVENT);
            if (dalek != null) {
                dalek.setPersistenceRequired();
                battle.daleks.add(dalek.getUUID());
            }
        }
        if (battle.daleks.isEmpty() || battle.thals.isEmpty()) {
            finish(battle, BattleEventLogic.Outcome.TIMED_OUT, now);
            return 0;
        }
        ACTIVE.add(battle);
        retarget(battle);
        broadcast(battle, Component.translatable("dwm.battle.start"));
        return battle.daleks.size();
    }

    private static void tickBattles(long now) {
        Iterator<Battle> it = ACTIVE.iterator();
        List<Battle> done = new ArrayList<>();
        List<BattleEventLogic.Outcome> outcomes = new ArrayList<>();
        while (it.hasNext()) {
            Battle battle = it.next();
            BattleEventLogic.Outcome outcome = BattleEventLogic.evaluate(
                    alive(battle.level, battle.daleks), alive(battle.level, battle.thals), now - battle.startTick);
            if (outcome != BattleEventLogic.Outcome.ONGOING) {
                done.add(battle);
                outcomes.add(outcome);
            } else if ((now - battle.startTick) % 40 == 0) {
                retarget(battle);
            }
        }
        for (int i = 0; i < done.size(); i++) {
            finish(done.get(i), outcomes.get(i), now);
        }
    }

    /** Points every Dalek at the closest armed Thal so the squad commits even before it sees one. */
    private static void retarget(Battle battle) {
        for (UUID id : battle.daleks) {
            if (battle.level.getEntity(id) instanceof Mob dalek && dalek.isAlive()) {
                ThalEntity closest = null;
                double best = Double.MAX_VALUE;
                for (UUID thalId : battle.thals) {
                    if (battle.level.getEntity(thalId) instanceof ThalEntity thal && thal.isAlive()) {
                        double distance = dalek.distanceToSqr(thal);
                        if (distance < best) {
                            best = distance;
                            closest = thal;
                        }
                    }
                }
                if (closest != null && dalek.getTarget() == null) {
                    dalek.setTarget(closest);
                }
            }
        }
    }

    private static void finish(Battle battle, BattleEventLogic.Outcome outcome, long now) {
        ACTIVE.remove(battle);
        lastEndTick = now;
        for (UUID id : battle.daleks) {
            Entity dalek = battle.level.getEntity(id);
            if (dalek != null) {
                dalek.discard();
            }
        }
        for (UUID id : battle.thals) {
            if (battle.level.getEntity(id) instanceof ThalEntity thal) {
                if (battle.reinforcements.contains(id)) {
                    thal.discard();
                } else {
                    thal.setArmed(false);
                }
            }
        }
        String key = switch (outcome) {
            case THALS_WON -> "dwm.battle.thals_won";
            case DALEKS_WON -> "dwm.battle.daleks_won";
            default -> "dwm.battle.ended";
        };
        broadcast(battle, Component.translatable(key));
    }

    private static int alive(ServerLevel level, Set<UUID> ids) {
        int count = 0;
        for (UUID id : ids) {
            Entity entity = level.getEntity(id);
            if (entity != null && entity.isAlive()) {
                count++;
            }
        }
        return count;
    }

    private static void broadcast(Battle battle, Component message) {
        AABB area = new AABB(battle.center).inflate(BattleEventLogic.MAX_SPAWN_DISTANCE * 2.0);
        for (ServerPlayer player : battle.level.getEntitiesOfClass(ServerPlayer.class, area)) {
            player.sendSystemMessage(message, true);
        }
    }

    @Nullable
    private static <T extends Mob> T spawnNear(ServerLevel level, EntityType<T> type, BlockPos center, int min, int max) {
        var random = level.getRandom();
        double angle = random.nextDouble() * Math.PI * 2.0;
        double distance = min + random.nextDouble() * (max - min);
        BlockPos spot = center.offset((int) Math.round(Math.cos(angle) * distance), 0, (int) Math.round(Math.sin(angle) * distance));
        if (!level.isLoaded(spot)) {
            return null;
        }
        return type.spawn(level, level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, spot), EntitySpawnReason.EVENT);
    }
}
