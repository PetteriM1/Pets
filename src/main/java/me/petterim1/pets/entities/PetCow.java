package me.petterim1.pets.entities;

import cn.nukkit.Player;
import cn.nukkit.entity.passive.EntityCow;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.particle.ItemBreakParticle;
import cn.nukkit.nbt.tag.CompoundTag;
import me.petterim1.pets.EntityPet;
import me.petterim1.pets.Main;
import me.petterim1.pets.Utils;

public class PetCow extends EntityPet {

    public PetCow(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return EntityCow.NETWORK_ID;
    }

    @Override
    public float getWidth() {
        return 0.45f;
    }

    @Override
    public float getHeight() {
        return 1.4f;
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
            default:
                return false;
        }
    }
}
