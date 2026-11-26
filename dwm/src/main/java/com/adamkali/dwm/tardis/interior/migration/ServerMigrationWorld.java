package com.adamkali.dwm.tardis.interior.migration;

import com.adamkali.dwm.tardis.interior.TardisPlotAllocator;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;

/** {@link MigrationWorld} over the real {@code dwm:tardis} level. */
public final class ServerMigrationWorld implements MigrationWorld {
    private final MinecraftServer server;
    private final ServerLevel level;


    public ServerMigrationWorld(MinecraftServer server, ServerLevel level) {
        this.server = server;
        this.level = level;
    }

    /** The legacy 64-block window of {@code tardisId} as a box. */
    public static InteriorBlockOps.Box legacyBox(UUID tardisId) {
        return InteriorBlockOps.legacyWindowBox(TardisPlotAllocator.legacyPlotOrigin(tardisId));
    }

    @Override
    public InteriorSnapshot capture(UUID tardisId) {
        return InteriorBlockOps.capture(level, TardisPlotAllocator.legacyPlotOrigin(tardisId), legacyBox(tardisId));
    }

    @Override
    public void clearLegacy(UUID tardisId) {
        InteriorBlockOps.clear(level, legacyBox(tardisId));
    }

    @Override
    public void prepareNew(UUID tardisId) {
        TardisPlotPreparer.prepare(level, TardisPlotAllocator.plotOrigin(tardisId));
    }

    @Override
    public void paste(UUID tardisId, InteriorSnapshot snapshot) {
        Vec3i delta = TardisPlotAllocator.migrationDelta(snapshot.legacyAnchor(), TardisPlotAllocator.plotOrigin(tardisId));
        InteriorBlockOps.Box source = InteriorBlockOps.legacyWindowBox(snapshot.legacyAnchor());
        InteriorBlockOps.paste(level, snapshot, source, delta);
    }

    @Override
    public void flush() {
        // Only the interior level changes: save its chunks and entities and flush them to disk.
        level.save(null, true, false);
        level.getChunkSource().chunkMap.synchronize(true).join();
    }

    @Override
    public Set<UUID> knownPlayers() {
        Set<UUID> players = new HashSet<>();
        Path playerData = server.getWorldPath(LevelResource.PLAYER_DATA_DIR);
        if (!Files.isDirectory(playerData)) {
            return players;
        }
        try (Stream<Path> files = Files.list(playerData)) {
            files.map(path -> path.getFileName().toString())
                    .filter(name -> name.endsWith(".dat"))
                    .map(name -> name.substring(0, name.length() - ".dat".length()))
                    .forEach(id -> {
                        try {
                            players.add(UUID.fromString(id));
                        } catch (IllegalArgumentException ignored) {
                            // Not a player file.
                        }
                    });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return players;
    }

    /** Snapshots as compressed NBT files {@code <dir>/<tardisId>.nbt}. */
    public static final class FileSnapshotStore implements SnapshotStore {
        private final Path directory;
        private final ServerLevel level;

        public FileSnapshotStore(Path directory, ServerLevel level) {
            this.directory = directory;
            this.level = level;
        }

        private Path file(UUID tardisId) {
            return directory.resolve(tardisId + ".nbt");
        }

        @Override
        public void write(UUID tardisId, InteriorSnapshot snapshot) {
            try {
                Files.createDirectories(directory);
                Path temp = directory.resolve(tardisId + ".nbt.tmp");
                NbtIo.writeCompressed(snapshot.toTag(), temp);
                Files.move(temp, file(tardisId), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException e) {
                throw new UncheckedIOException("Could not save the interior snapshot of TARDIS " + tardisId, e);
            }
        }

        @Override
        public Optional<InteriorSnapshot> read(UUID tardisId) {
            Path file = file(tardisId);
            if (!Files.isRegularFile(file)) {
                return Optional.empty();
            }
            try {
                return Optional.of(InteriorSnapshot.fromTag(
                        NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap()),
                        level.registryAccess().lookupOrThrow(Registries.BLOCK)));
            } catch (IOException e) {
                throw new UncheckedIOException("Could not read the interior snapshot of TARDIS " + tardisId, e);
            }
        }

        @Override
        public void delete(UUID tardisId) {
            try {
                Files.deleteIfExists(file(tardisId));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
}
