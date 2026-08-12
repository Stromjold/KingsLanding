package com.war.reino.mineros;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.component.WrittenBookContent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegistroMineria {

    /**
     * Compara el inventario del cofre con los materiales necesarios y anota el progreso.
     *
     * @param nivel        El mundo del servidor
     * @param posCofre     La posición del cofre de depósito
     * @param posAtril     La posición del mostrador/atril
     * @param requeridos   Mapa con los nombres de los ítems que se necesitan y su cantidad meta.
     */
    public static void actualizarRegistro(ServerLevel nivel, BlockPos posCofre, BlockPos posAtril, Map<String, Integer> requeridos) {

        BlockEntity entidadCofre = nivel.getBlockEntity(posCofre);
        if (!(entidadCofre instanceof Container cofre)) {
            return;
        }

        Map<String, Integer> inventario = new HashMap<>();

        for (int i = 0; i < cofre.getContainerSize(); i++) {
            ItemStack item = cofre.getItem(i);
            if (!item.isEmpty()) {
                String nombre = item.getHoverName().getString();
                inventario.put(nombre, inventario.getOrDefault(nombre, 0) + item.getCount());
            }
        }

        StringBuilder pagina = new StringBuilder();
        pagina.append("=== PROGRESO DE OBRA ===\n\n");

        if (requeridos == null || requeridos.isEmpty()) {
            pagina.append("No hay proyectos de construcción activos.\n");
        } else {
            boolean proyectoCompletado = true;

            for (Map.Entry<String, Integer> meta : requeridos.entrySet()) {
                String material = meta.getKey();
                int cantidadNecesaria = meta.getValue();
                int cantidadActual = inventario.getOrDefault(material, 0);

                if (cantidadActual < cantidadNecesaria) {
                    proyectoCompletado = false;
                }

                pagina.append("• ").append(material).append(": ")
                        .append(cantidadActual).append(" / ").append(cantidadNecesaria).append("\n");
            }

            pagina.append("\n");
            if (proyectoCompletado) {
                pagina.append("✅ ¡MATERIALES LISTOS!\n");
            } else {
                pagina.append("⏳ Faltan materiales...\n");
            }
        }

        BlockEntity entidadAtril = nivel.getBlockEntity(posAtril);
        if (!(entidadAtril instanceof LecternBlockEntity atril)) {
            return;
        }

        ItemStack libroRegistro = new ItemStack(Items.WRITTEN_BOOK);
        WrittenBookContent contenidoLibro = new WrittenBookContent(
                Filterable.passThrough("Reporte de Construcción"),
                "Arquitecto Real",
                0,
                List.of(Filterable.passThrough(Component.literal(pagina.toString()))),
                false
        );

        libroRegistro.set(DataComponents.WRITTEN_BOOK_CONTENT, contenidoLibro);
        atril.setBook(libroRegistro);
    }
}