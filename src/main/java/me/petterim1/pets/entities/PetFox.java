package me.petterim1.pets.entities;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import me.petterim1.pets.EntityPet;

public class PetFox extends EntityPet {

    public PetFox(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    protected void initEntity() {
        super.initEntity();

        this.sitting = this.namedTag.getBoolean("Sitting");
        this.setDataFlag(DATA_FLAGS, DATA_FLAG_SITTING, this.isSitting());
    }

    @Override
    public void saveNBT() {
        super.saveNBT();

        this.namedTag.putBoolean("Sitting", this.isSitting());
    }

    @Override
    public int getNetworkId() {
        return 121;
    }

    @Override
    public float getWidth() {
        return 0.7f;
    }

    @Override
    public float getHeight() {
        return 0.6f;
    }

    @Override
    protected boolean isFeedItem(int id) {
        return id == Item.ROTTEN_FLESH || id == Item.RAW_BEEF || id == Item.RAW_MUTTON || id == Item.RAW_RABBIT || id == Item.RAW_CHICKEN || id == Item.RAW_PORKCHOP || id == Item.SWEET_BERRIES;
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
    protected String getType() {
        return "'s fox";
    }

    @Override
    protected String getSaveName() {
        return "Fox";
    }
}
