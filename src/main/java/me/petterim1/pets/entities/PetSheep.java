package me.petterim1.pets.entities;

import cn.nukkit.Player;
import cn.nukkit.entity.data.ByteEntityData;
import cn.nukkit.entity.passive.EntitySheep;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemDye;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.DyeColor;
import me.petterim1.pets.EntityPet;
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
        this.setDataFlag(DATA_FLAGS, DATA_FLAG_BABY, true);
        this.setScale(0.6f);
    }

    @Override
    public int getNetworkId() {
        return EntitySheep.NETWORK_ID;
    }

    @Override
    public float getWidth() {
        return 0.45f;
    }

    @Override
    public float getHeight() {
        return 1f;
    }

    @Override
    protected boolean isFeedItem(int id) {
        return id == Item.WHEAT;
    }

    @Override
    public boolean onInteract(Player player, Item item, Vector3 clickedPos) {
        if (this.isFeedItem(item.getId())) {
            this.feed(player, item);
            return true;
        } else if (item.getId() == Item.DYE) {
            this.setColor(((ItemDye) item).getDyeColor().getWoolData());
            return true;
        }
        return super.onInteract(player, item, clickedPos);
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

    @Override
    protected String getType() {
        return "'s sheep";
    }

    @Override
    protected String getSaveName() {
        return "Sheep";
    }
}
