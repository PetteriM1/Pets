package me.petterim1.pets.entities;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.data.ByteEntityData;
import cn.nukkit.entity.data.LongEntityData;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemDye;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.EntityEventPacket;
import cn.nukkit.utils.DyeColor;
import me.petterim1.pets.EntityPet;

public class PetDog extends EntityPet {

    private DyeColor collarColor = DyeColor.RED;
    private int afterInWater = -1;

    public PetDog(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    protected void initEntity() {
        super.initEntity();

        this.sitting = this.namedTag.getBoolean("Sitting");
        this.setDataFlag(DATA_FLAGS, DATA_FLAG_SITTING, this.isSitting());

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
    protected boolean isFeedItem(int id) {
        return id == Item.BONE || id == Item.ROTTEN_FLESH || id == Item.RAW_BEEF || id == Item.RAW_MUTTON || id == Item.RAW_RABBIT || id == Item.RAW_CHICKEN || id == Item.RAW_PORKCHOP;
    }

    @Override
    public boolean onInteract(Player player, Item item, Vector3 clickedPos) {
        if (this.isFeedItem(item.getId())) {
            this.feed(player, item);
            return true;
        } else if (item.getId() == Item.DYE) {
            this.setCollarColor(((ItemDye) item).getDyeColor());
            return true;
        }
        if (this.isOwner(player)) {
            this.setSitting();
        }
        return super.onInteract(player, item, clickedPos);
    }

    @Override
    public boolean onUpdate(int currentTick) {
        boolean update = super.onUpdate(currentTick);
        if (update) {
            if (this.isInsideOfWater()) {
                this.afterInWater = 0;
            } else if (this.afterInWater != -1) {
                this.afterInWater++;
            }
            if (this.afterInWater > 60) {
                this.afterInWater = -1;
                this.stayTime = 40;
                EntityEventPacket packet = new EntityEventPacket();
                packet.eid = this.getId();
                packet.event = EntityEventPacket.SHAKE_WET;
                Server.broadcastPacket(this.getViewers().values(), packet);
            }
        }
        return update;
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

    @Override
    protected String getSaveName() {
        return "Dog";
    }
}
