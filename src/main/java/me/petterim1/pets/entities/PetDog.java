package me.petterim1.pets.entities;

import cn.nukkit.Player;
import cn.nukkit.entity.data.ByteEntityData;
import cn.nukkit.entity.data.LongEntityData;
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

public class PetDog extends EntityPet {

    private DyeColor collarColor = DyeColor.RED;

    public PetDog(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    protected void initEntity() {
        super.initEntity();

        this.sitting = this.namedTag.getBoolean("Sitting");
        this.setDataFlag(DATA_FLAGS, DATA_FLAG_SITTING, this.sitting);

        if (this.namedTag.contains("CollarColor")) {
            this.setCollarColor(DyeColor.getByDyeData(this.namedTag.getByte("CollarColor")));
        }
    }

    @Override
    public void saveNBT() {
        super.saveNBT();

        this.namedTag.putBoolean("Sitting", this.isSitting());
        if (this.collarColor != null) {
            this.namedTag.putByte("CollarColor", this.collarColor.getDyeData());
        }
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
    public boolean onInteract(Player player, Item item, Vector3 clickedPos) {
        switch (player.getInventory().getItemInHand().getId()) {
            case Item.BONE:
            case Item.ROTTEN_FLESH:
            case Item.RAW_BEEF:
            case Item.RAW_MUTTON:
            case Item.RAW_RABBIT:
            case Item.RAW_CHICKEN:
            case Item.RAW_PORKCHOP:
                player.getInventory().decreaseCount(player.getInventory().getHeldItemIndex());
                this.level.addParticle(new ItemBreakParticle(
                        this.add(Utils.rand(-0.5, 0.5), this.getMountedYOffset(), Utils.rand(-0.5, 0.5)),
                        player.getInventory().getItemInHand()));

                this.inLoveTicks = 10;
                this.setDataFlag(DATA_FLAGS, DATA_FLAG_INLOVE);
                player.addExperience(Main.getInstance().getPluginConfig().getInt("feedXp"));
                return true;
            case Item.DYE:
                player.getInventory().decreaseCount(player.getInventory().getHeldItemIndex());
                this.setCollarColor(((ItemDye) item).getDyeColor());
                return true;
            default:
                if (isOwner(player)) {
                    this.setSitting();
                }
                return super.onInteract(player, item, clickedPos);
        }
    }

    public void setCollarColor(DyeColor color) {
        if (color == null) {
            this.collarColor = DyeColor.RED;
        } else {
            this.collarColor = color;
        }
        this.namedTag.putByte("CollarColor", this.collarColor.getDyeData());
        if (this.owner != null) {
            Player pl = server.getPlayerExact(this.owner);
            if (pl != null) {
                this.setDataProperty(new LongEntityData(DATA_OWNER_EID, pl.getId()));
            }
        }
        this.setDataProperty(new ByteEntityData(DATA_COLOUR, this.collarColor.getWoolData()));
    }

    @Override
    protected String getType() {
        return "'s dog";
    }
}
