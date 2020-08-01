package me.petterim1.pets.entities;

import cn.nukkit.Player;
import cn.nukkit.entity.data.ByteEntityData;
import cn.nukkit.entity.passive.EntitySheep;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemDye;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.particle.ItemBreakParticle;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.DyeColor;
import me.petterim1.pets.EntityPet;
import me.petterim1.pets.Main;
import me.petterim1.pets.Utils;

public class PetSheep extends EntityPet {
    
    protected int color = 0;
    
    public PetSheep(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }
    
    @Override
    protected void initEntity() {
        super.initEntity();
        
        this.color = this.namedTag.getByte("Color");
        this.setDataProperty(new ByteEntityData(DATA_COLOUR, this.color));
    }

    @Override
    public int getNetworkId() {
        return EntitySheep.NETWORK_ID;
    }

    @Override
    public float getWidth() {
        return 0.9f;
    }

    @Override
    public float getHeight() {
        return 1.3f;
    }

    @Override
    public boolean onInteract(Player player, Item item) {
        switch (player.getInventory().getItemInHand().getId()) {
            case Item.WHEAT:
                player.getInventory().decreaseCount(player.getInventory().getHeldItemIndex());
                this.level.addParticle(new ItemBreakParticle(
                        this.add(Utils.rand(-0.5, 0.5), this.getMountedYOffset(), Utils.rand(-0.5, 0.5)),
                        player.getInventory().getItemInHand()));

                this.inLoveTicks = 10;
                this.setDataFlag(DATA_FLAGS, DATA_FLAG_INLOVE);
                player.addExperience(Main.getInstance().getPluginConfig().getInt("feedXp"));
                return true;
            case Item.DYE:
                this.color = ((ItemDye) item).getDyeColor().getWoolData();
                this.saveNBT();
                return true;
            default:
                return false;
        }
    }
    
    @Override
    public void setRandomType() {
        this.color = randomColor();
        this.saveNBT();
    }
    
    @Override
    public void saveNBT() {
        super.saveNBT();
        this.color = this.namedTag.getByte("Color");
        this.setDataProperty(new ByteEntityData(DATA_COLOUR, this.color));
    }
    
    private int randomColor() {
        int rand = Utils.rand(0, 2500);

        if (rand < 125 && 0 <= rand) return DyeColor.WHITE.getDyeData();
        else if (rand < 250 && 125 <= rand) return DyeColor.GRAY.getDyeData();
        else if (rand < 375 && 250 <= rand) return DyeColor.LIGHT_GRAY.getDyeData();
        else if (rand < 500 && 375 <= rand) return DyeColor.GRAY.getDyeData();
        else if (rand < 541 && 500 <= rand) return DyeColor.PINK.getDyeData();
        else return DyeColor.BLACK.getDyeData();
    }
}
