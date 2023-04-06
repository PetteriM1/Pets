package me.petterim1.pets.entities;

import cn.nukkit.Player;
import cn.nukkit.entity.passive.EntityChicken;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import me.petterim1.pets.EntityPet;

public class PetChicken extends EntityPet {

    public PetChicken(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return EntityChicken.NETWORK_ID;
    }

    @Override
    public float getWidth() {
        return 0.4f;
    }

    @Override
    public float getHeight() {
        return 0.7f;
    }

    @Override
    protected boolean isFeedItem(int id) {
        return id == Item.SEEDS || id == Item.PUMPKIN_SEEDS || id == Item.MELON_SEEDS || id == Item.BEETROOT_SEEDS;
    }

    @Override
    public boolean onInteract(Player player, Item item, Vector3 clickedPos) {
        if (this.isFeedItem(item.getId())) {
            this.feed(player, item);
            return true;
        }
        return super.onInteract(player, item, clickedPos);
    }

    @Override
    protected String getType() {
        return "'s chicken";
    }

    @Override
    protected String getSaveName() {
        return "Chicken";
    }
}
