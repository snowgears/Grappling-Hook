package com.snowgears.grapplinghook.utils;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class HookSettings {

    private String id;
    private int maxUses;
    private double velocityThrow;
    private double velocityPull;
    private int timeBetweenGrapples;
    private boolean fallDamage;
    private boolean slowFall;
    private boolean lineBreak;
    private boolean stickyHook;
    private int customModelData;

    private HashMap<EntityType, Boolean> entityTypes = new HashMap<>();
    private HashMap<Material, Boolean> materials = new HashMap<>();

    public HookSettings(String id,
        int maxUses,
        double velocityThrow,
        double velocityPull,
        int timeBetweenGrapples,
        boolean fallDamage,
        boolean slowFall,
        boolean lineBreak,
        boolean stickyHook,
        int customModelData){

        this.id = id;
        this.maxUses = maxUses;
        this.velocityThrow = velocityThrow;
        this.velocityPull = velocityPull;
        this.timeBetweenGrapples = timeBetweenGrapples;
        this.fallDamage = fallDamage;
        this.slowFall = slowFall;
        this.lineBreak = lineBreak;
        this.stickyHook = stickyHook;
        this.customModelData = customModelData;
    }

    public void setEntityList(boolean isBlackList, List<EntityType> entityTypeList){
        if(isBlackList){
            List<EntityType> entities = new ArrayList(Arrays.asList(EntityType.values()));
            for(EntityType entityType : entityTypeList){
                entities.remove(entityType);
            }

            for(EntityType entityType : entities){
                this.entityTypes.put(entityType, true);
            }
        }
        else{
            for(EntityType entityType : entityTypeList){
                this.entityTypes.put(entityType, true);
            }
        }
    }

    public void setMaterialList(boolean isBlackList, List<Material> materialList){
        if(isBlackList){
            List<Material> materials = new ArrayList(Arrays.asList(Material.values()));
            for(Material material : materialList){
                materials.remove(material);
            }

            for(Material material : materials){
                this.materials.put(material, true);
            }
        }
        else{
            for(Material material : materialList){
                this.materials.put(material, true);
            }
        }
    }

    public String getId() {
        return id;
    }

    public int getMaxUses(){
        return maxUses;
    }

    public double getVelocityThrow() {
        return velocityThrow;
    }

    public double getVelocityPull() {
        return velocityPull;
    }

    public int getTimeBetweenGrapples() {
        return timeBetweenGrapples;
    }

    public boolean isFallDamage() {
        return fallDamage;
    }

    public boolean isSlowFall() {
        return slowFall;
    }

    public boolean isLineBreak() {
        return lineBreak;
    }

    public boolean isStickyHook() {
        return stickyHook;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public boolean canHookEntityType(EntityType entityType){
        return entityTypes.containsKey(entityType);
    }

    public boolean canHookMaterial(Material material){
        return materials.containsKey(material);
    }
}
