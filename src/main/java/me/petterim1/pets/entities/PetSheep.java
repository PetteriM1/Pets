package me.petterim1.pets.entities;

import cn.nukkit.Player;
import cn.nukkit.entity.data.ByteEntityData;
import cn.nukkit.entity.passive.EntitySheep;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemDye;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.particle.ItemBreakParticle;
import cn.nukkit.math.Vector3;
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
        
        this.setColor(this.namedTag.getByte("Color"));
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
    public boolean onInteract(Player player, Item item, Vector3 clickedPos) {
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
                this.setColor(((ItemDye) item).getDyeColor().getWoolData());
                this.saveNBT();
                return true;
            default:
                return false;
        }
    }
    
    @Override
    public void setRandomType() {
        this.setColor(randomColor());
    }
    
    @Override
    public void saveNBT() {
        super.saveNBT();

        this.namedTag.putByte("Color", this.color);
    }

    public void setColor(int woolColor) {
        this.color = woolColor;
        this.namedTag.putByte("Color", woolColor);
        this.setDataProperty(new ByteEntityData(DATA_COLOUR, woolColor));
    }

    private int randomColor() {
        int rand = Utils.rand(1, 200);

        if (rand == 1) return DyeColor.PINK.getWoolData();
        else if (rand < 8) return DyeColor.BROWN.getWoolData();
        else if (rand < 18) return DyeColor.GRAY.getWoolData();
        else if (rand < 28) return DyeColor.LIGHT_GRAY.getWoolData();
        else if (rand < 38) return DyeColor.BLACK.getWoolData();
        else return DyeColor.WHITE.getWoolData();
    }
}
