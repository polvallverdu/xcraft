package dev.pov.xcraft.listeners;

import com.google.common.collect.Lists;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.BlockEvent;
import dev.architectury.event.events.common.ChunkEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.dimension.DimensionType;
import org.intellij.lang.annotations.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkListener {

    public static final String XCRAFT_MINED_DATA = "xcraft_cdata";

    private static final ConcurrentHashMap<ResourceKey<DimensionType>, ConcurrentHashMap<ChunkPos, List<BlockPos>>> minedData = new ConcurrentHashMap<>();

    public static void initializeChunkEvents() {
        ChunkEvent.LOAD_DATA.register((chunk, world, compoundTag) -> {
            int[] data = compoundTag.contains(XCRAFT_MINED_DATA) ? compoundTag.getIntArray(XCRAFT_MINED_DATA) : new int[0];
            if (data.length % 3 != 0) {
                return;
            }

            List<BlockPos> positions = minedData.computeIfAbsent(world.dimensionTypeId(), k -> new ConcurrentHashMap<>()).computeIfAbsent(chunk.getPos(), k -> Collections.synchronizedList(new ArrayList<>(data.length / 3)));
            for (int i = 0; i < data.length; i += 3) {
                positions.add(new BlockPos(data[i], data[i + 1], data[i + 2]));
            }
        });

        ChunkEvent.SAVE_DATA.register((chunk, world, compoundTag) -> {
            List<BlockPos> positions = minedData.get(world.dimensionTypeId()).get(chunk.getPos());

            int[] data = new int[positions.size() * 3];
            for (int i = 0; i < positions.size(); i++) {
                BlockPos pos = positions.get(i);
                data[i * 3] = pos.getX();
                data[i * 3 + 1] = pos.getY();
                data[i * 3 + 2] = pos.getZ();
            }

            compoundTag.putIntArray(XCRAFT_MINED_DATA, data);
        });

        BlockEvent.PLACE.register((level, blockPos, blockState, entity) -> {
            if (level instanceof ServerLevel && entity instanceof Player) {
                minedData.computeIfAbsent(level.dimensionTypeId(), k -> new ConcurrentHashMap<>()).computeIfAbsent(new ChunkPos(blockPos), k -> Collections.synchronizedList(new ArrayList<>())).add(blockPos);
            }

            return EventResult.pass();
        });
    }

    public static boolean isBlockPlaced(ServerLevel world, BlockPos pos) {
        var chunk = world.getChunk(pos);
        return minedData.get(world.dimensionTypeId()).get(chunk.getPos()).contains(pos);
    }

}
