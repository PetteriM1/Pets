package me.petterim1.pets.entities;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.particle.ItemBreakParticle;
import cn.nukkit.nbt.tag.CompoundTag;
import me.petterim1.pets.EntityPet;
import me.petterim1.pets.Main;
import me.petterim1.pets.Utils;

public class PetDog extends EntityPet {

    public PetDog(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return 14;
    }

    @Override
    public float getWidth() {
        return 0.6f;
    }

    @Override
    public float getHeight() {
        return 0.85f;
    }

    @Override
    public boolean onInteract(Player player, Item item) {
        if (player.getInventory().getItemInHand().equals(Item.get(Item.BONE, 0))) {
            player.getInventory().removeItem(Item.get(Item.BONE, 0, 1));
            this.level.addParticle(new ItemBreakParticle(
                    this.add(Utils.rand(-0.5, 0.5), this.getMountedYOffset(), Utils.rand(-0.5, 0.5)),
                    Item.get(Item.BONE)));

            this.inLoveTicks = 10;
            this.setDataFlag(DATA_FLAGS, DATA_FLAG_INLOVE);
            player.addExperience(Main.getInstance().getPluginConfig().getInt("feedXp"));
            return true;
        } else {
            return super.onInteract(player, item);
        }
    }
}