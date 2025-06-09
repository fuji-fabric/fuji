package io.github.sakurawald.module.initializer.top_chunks.config.model;

import java.util.HashMap;

public class TopChunksConfigModel {

    public Top top = new Top();
    public int nearest_distance = 128;
    public boolean hide_location = true;
    public HashMap<String, Integer> type2score = new HashMap<>() {
        {
            this.put("default", 1);
            this.put("block.minecraft.chest", 1);
            this.put("block.minecraft.trapped_chest", 2);
            this.put("block.minecraft.barrel", 1);
            this.put("block.minecraft.furnace", 3);
            this.put("block.minecraft.blast_furnace", 4);
            this.put("block.minecraft.smoker", 3);
            this.put("block.minecraft.jukebox", 35);
            this.put("block.minecraft.beacon", 35);
            this.put("block.minecraft.conduit", 40);
            this.put("block.minecraft.hopper", 8);
            this.put("block.minecraft.piston", 10);
            this.put("block.minecraft.dispenser", 10);
            this.put("block.minecraft.dropper", 10);
            this.put("block.minecraft.comparator", 5);
            this.put("block.minecraft.daylight_detector", 25);
            this.put("block.minecraft.beehive", 15);
            this.put("block.minecraft.mob_spawner", 100);
            this.put("entity.minecraft.player", 15);
            this.put("entity.minecraft.falling_block", 10);
            this.put("entity.minecraft.zombie", 4);
            this.put("entity.minecraft.zombie_villager", 8);
            this.put("entity.minecraft.zombified_piglin", 5);
            this.put("entity.minecraft.zoglin", 8);
            this.put("entity.minecraft.ravager", 80);
            this.put("entity.minecraft.pillager", 20);
            this.put("entity.minecraft.evoker", 20);
            this.put("entity.minecraft.vindicator", 20);
            this.put("entity.minecraft.vex", 20);
            this.put("entity.minecraft.piglin", 2);
            this.put("entity.minecraft.drowned", 2);
            this.put("entity.minecraft.guardian", 6);
            this.put("entity.minecraft.spider", 2);
            this.put("entity.minecraft.skeleton", 2);
            this.put("entity.minecraft.creeper", 3);
            this.put("entity.minecraft.endermite", 5);
            this.put("entity.minecraft.enderman", 4);
            this.put("entity.minecraft.wither", 55);
            this.put("entity.minecraft.villager", 25);
            this.put("entity.minecraft.sheep", 5);
            this.put("entity.minecraft.cow", 3);
            this.put("entity.minecraft.mooshroom", 3);
            this.put("entity.minecraft.chicken", 3);
            this.put("entity.minecraft.panda", 5);
            this.put("entity.minecraft.wolf", 8);
            this.put("entity.minecraft.cat", 8);
            this.put("entity.minecraft.bee", 15);
            this.put("entity.minecraft.boat", 5);
            this.put("entity.minecraft.chest_boat", 5);
            this.put("entity.minecraft.item_frame", 3);
            this.put("entity.minecraft.glow_item_frame", 3);
            this.put("entity.minecraft.armor_stand", 10);
            this.put("entity.minecraft.item", 10);
            this.put("entity.minecraft.experience_orb", 3);
            this.put("entity.minecraft.tnt", 70);
            this.put("entity.minecraft.hopper_minecart", 20);
        }
    };

    public static class Top {
        public int rows = 10;
        public int columns = 10;
    }
}
