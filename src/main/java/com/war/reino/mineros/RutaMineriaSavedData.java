package com.war.reino.mineros;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.server.level.ServerLevel;

import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.resources.Identifier;
import com.mojang.serialization.Codec;
import net.minecraft.util.datafix.DataFixTypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RutaMineriaSavedData {

    private static final Map<ServerLevel, RutaMineriaSavedData> REGISTRY = new WeakHashMap<>();
    private final List<BlockPos> nodos = new ArrayList<>();

    public static RutaMineriaSavedData get(ServerLevel level) {
        return REGISTRY.computeIfAbsent(level, l -> new RutaMineriaSavedData());
    }

    public int agregarNodo(BlockPos pos) {
        if (!nodos.contains(pos)) {
            BlockPos inm = pos.immutable();
            nodos.add(inm);
            return nodos.indexOf(inm);
        }
        return nodos.indexOf(pos);
    }

    public boolean quitarNodo(BlockPos pos) {
        boolean removido = nodos.remove(pos);
        if (removido) {
            // not persisted in this in-memory implementation
        }
        return removido;
    }

    public void limpiarRuta() {
        nodos.clear();
    }

    public List<BlockPos> getNodos() {
        return Collections.unmodifiableList(nodos);
    }

}