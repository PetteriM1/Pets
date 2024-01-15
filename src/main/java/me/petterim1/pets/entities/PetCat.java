package me.petterim1.pets.entities;

import cn.nukkit.Player;
import cn.nukkit.entity.data.IntEntityData;
import cn.nukkit.entity.passive.EntityCat;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import me.petterim1.pets.EntityPet;
import me.petterim1.pets.Utils;

public class PetCat extends EntityPet {

    protected int type;

    public PetCat(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    protected void initEntity() {
        super.initEntity();

        this.sitting = this.namedTag.getBoolean("Sitting");
        this.setDataFlag(DATA_FLAGS, DATA_FLAG_SITTING, this.isSitting());

        this.type = this.namedTag.getInt("CatType");
        this.setDataProperty(new IntEntityData(DATA_VARIANT, this.type));

        this.pitch = 100;
    }

    @Override
    public int getNetworkId() {
        return EntityCat.NETWORK_ID;
    }

    @Override
    public float getWidth() {
        return 0.6f;
    }

    @Override
    public float getHeight() {
        return 0.7f;
    }

    @Override
    protected boolean isFeedItem(int id) {
        return id == Item.RAW_FISH || id == Item.RAW_SALMON || id == Item.CLOWNFISH;
    }

    @Override
    public boolean onInteract(Player player, Item item, Vector3 clickedPos) {
        if (this.isFeedItem(item.getId())) {
            this.feed(player, item);
            return true;
        }
        if (this.isOwner(player)) {
            this.setSitting();
        }
        return super.onInteract(player, item, clickedPos);
    }

    @Override
    public void setRandomType() {
        this.type = Utils.rand(1, 3);
        this.setDataProperty(new IntEntityData(DATA_VARIANT, this.type));
        this.saveNBT();
    }

    @Override
    public void saveNBT() {
        super.saveNBT();

        this.namedTag.putInt("CatType", this.type);
        this.namedTag.putBoolean("Sitting", this.isSitting());
    }

    @Override
    protected String getType() {
        return "'s cat";
    }

    @Override
    protected String getSaveName() {
        return "Cat";
    }
}
