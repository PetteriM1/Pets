package me.petterim1.pets;

import cn.nukkit.Player;
import cn.nukkit.block.*;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.particle.CriticalParticle;
import cn.nukkit.level.particle.HeartParticle;
import cn.nukkit.level.particle.ItemBreakParticle;
import cn.nukkit.math.NukkitMath;
import cn.nukkit.math.Vector2;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;

public abstract class EntityPet extends EntityCreature {

    protected String owner;
    protected Vector3 target;
    protected int stayTime;
    protected int moveTime;
    protected int inLoveTicks;
    protected boolean findingPlayer;
    protected boolean sitting;

    public EntityPet(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);

        this.setOwner(nbt.getString("Owner"));
        this.setNameTagVisible(true);
        this.setDataFlag(DATA_FLAGS, DATA_FLAG_TAMED, true);

        this.pitch = 0;
    }

    public void setRandomType() {
    }

    @Override
    public String getName() {
        return Main.getInstance().getNameTagColor() + this.owner + this.getType();
    }

    protected abstract String getType();

    @Override
    public void saveNBT() {
        super.saveNBT();

        this.namedTag.putString("Owner", this.owner);
    }

    @Override
    public boolean attack(EntityDamageEvent ev) {
        this.level.addParticle(new CriticalParticle(this.add(Utils.rand(-0.5, 0.5), this.getMountedYOffset(), Utils.rand(-0.5, 0.5))));
        return true;
    }

    @Override
    public boolean onInteract(Player player, Item item, Vector3 clickedPos) {
        return false;
    }

    protected void feed(Player player, Item item) {
        player.addExperience(Main.getInstance().getFeedXp());
        this.level.addParticle(new ItemBreakParticle(
                this.add(Utils.rand(-0.5, 0.5), this.getMountedYOffset(), Utils.rand(-0.5, 0.5)),
                item));

        this.inLoveTicks = 10;
        this.setDataFlag(DATA_FLAGS, DATA_FLAG_INLOVE);
    }

    public boolean isOwner(Player p) {
        return p.getName().equalsIgnoreCase(this.owner);
    }

    public void setOwner(String owner) {
        this.owner = owner;
        this.setNameTag(this.getName());
        this.saveNBT();
    }

    protected boolean targetOption(EntityCreature creature, double distance) {
        if (creature instanceof Player) {
            Player player = (Player) creature;
            return player.isAlive() && !player.closed && isOwner(player) && distanceCheck(distance);
        }

        return false;
    }

    private boolean distanceCheck(double distance) {
        boolean dis = distance < 400 && distance > 36;

        PlayerInventory inv;
        if (this.target instanceof Player && (inv = ((Player) this.target).getInventory()) != null && this.isFeedItem(inv.getItemInHand().getId())) { //feed item
            dis = true;
        }

        if (dis && findingPlayer) {
            findingPlayer = false;
        }

        return dis || findingPlayer;
    }

    protected boolean isFeedItem(int id) {
        return false;
    }

    public void setSitting() {
        this.moveTime = 0;
        this.stayTime = 60;
        this.motionX = 0;
        this.motionZ = 0;

        if (this.namedTag.getByte("Sitting") == 0) {
            this.namedTag.putByte("Sitting", 1);
            this.setDataFlag(DATA_FLAGS, DATA_FLAG_SITTING, true);
            this.sitting = true;
        } else {
            this.namedTag.putByte("Sitting", 0);
            this.setDataFlag(DATA_FLAGS, DATA_FLAG_SITTING, false);
            this.sitting = false;
        }

        this.onGround = false;
    }

    public boolean isSitting() {
        return this.sitting;
    }

    protected void checkTarget() {
        Vector3 target = this.target;
        if (!(target instanceof EntityCreature)
                || !this.targetOption((EntityCreature) target, this.distanceSquared(target))) {
            double near = Integer.MAX_VALUE;

            for (Player player : this.getLevel().getPlayers().values()) {
                if (player == null) {
                    continue;
                }

                double distance = this.distanceSquared(player);
                if (distance > near || !this.targetOption(player, distance)) {
                    continue;
                }
                near = distance;

                this.stayTime = 0;
                this.moveTime = 0;
                this.target = player;
            }
        }

        if (this.target instanceof EntityCreature && !((EntityCreature) this.target).closed
                && ((EntityCreature) this.target).isAlive()
                && this.targetOption((EntityCreature) this.target, this.distanceSquared(this.target))) {
            return;
        }

        int x, z;
        if (this.stayTime > 0) {
            if (Utils.rand(1, 100) > 5) {
                return;
            }
            x = Utils.rand(10, 30);
            z = Utils.rand(10, 30);
            this.target = this.add(Utils.rand() ? x : -x, Utils.rand(-20, 20) / 10, Utils.rand() ? z : -z);
        } else if (Utils.rand(1, 400) == 1) {
            x = Utils.rand(10, 30);
            z = Utils.rand(10, 30);
            this.stayTime = Utils.rand(100, 300);
            this.target = this.add(Utils.rand() ? x : -x, Utils.rand(-20, 20) / 10, Utils.rand() ? z : -z);
        } else if (this.moveTime <= 0 || this.target == null) {
            x = Utils.rand(10, 30);
            z = Utils.rand(10, 30);
            this.stayTime = 0;
            this.moveTime = Utils.rand(60, 100);
            this.target = this.add(Utils.rand() ? x : -x, 0, Utils.rand() ? z : -z);
        }
    }

    protected boolean checkJump(double dx, double dz) {
        if (this.motionY == 0.16) {
            int b = Utils.getBlockId(level, chunk, NukkitMath.floorDouble(this.x), (int) this.y, NukkitMath.floorDouble(this.z));
            return b == BlockID.WATER || b == BlockID.STILL_WATER;
        } else {
            int b = Utils.getBlockId(level, chunk, NukkitMath.floorDouble(this.x), (int) (this.y + 0.8), NukkitMath.floorDouble(this.z));
            if (b == BlockID.WATER || b == BlockID.STILL_WATER) {
                this.motionY = 0.16;
                return true;
            }
        }

        if (!this.onGround || this.stayTime > 0) {
            return false;
        }

        Block that = this.getLevel().getBlock(NukkitMath.floorDouble(this.x + dx), (int) this.y, NukkitMath.floorDouble(this.z + dz));
        Block block = that.getSide(this.getHorizontalFacing());
        if (!block.canPassThrough() && block.up().canPassThrough() && that.up(2).canPassThrough()) {
            if (block instanceof BlockFence || block instanceof BlockFenceGate) {
                this.motionY = 0.08;
            } else if (this.motionY <= 0.32) {
                this.motionY = 0.32;
            } else if (block instanceof BlockStairs) {
                this.motionY = 0.32;
            } else if (this.motionY <= 0.64) {
                this.motionY = 0.64;
            } else {
                this.motionY += 0.02;
            }
            return true;
        }
        return false;
    }

    public void updateMove(int tickDiff) {
        if (this.isSitting()) {
            if (this.onGround) {
                if (Utils.getBlockId(level, chunk, getFloorX(), getFloorY() - 1, getFloorZ()) == 0) {
                    this.onGround = false;
                }
            }
            if (this.onGround) {
                this.motionY = 0;
            } else {
                this.motionY -= getGravity();
            }
            this.move(this.motionX, this.motionY, this.motionZ);
            this.motionY *= 0.9;
            this.updateMovement();
            return;
        }

        Vector3 before = this.target;
        this.checkTarget();
        if (this.target instanceof EntityCreature || before != this.target) {
            double x = this.target.x - this.x;
            double z = this.target.z - this.z;

            double diff = Math.abs(x) + Math.abs(z);
            if (diff == 0 || this.stayTime > 0 || (!findingPlayer && this.distance(this.target) <= this.getWidth() + 1)) {
                this.motionX = 0;
                this.motionZ = 0;
            } else {
                if (this.isInsideOfWater()) {
                    this.motionX = 0.06 * (x / diff);
                    this.motionZ = 0.06 * (z / diff);
                } else {
                    this.motionX = 0.18 * (x / diff);
                    this.motionZ = 0.18 * (z / diff);
                }
            }
            if (diff != 0) this.yaw = Math.toDegrees(-Math.atan2(x / diff, z / diff));
        }

        double dx = this.motionX * tickDiff;
        double dz = this.motionZ * tickDiff;
        boolean isJump = this.checkJump(dx, dz);
        if (this.stayTime > 0) {
            this.stayTime -= tickDiff;
            this.move(0, this.motionY * tickDiff, 0);
        } else {
            Vector2 be = new Vector2(this.x + dx, this.z + dz);
            this.move(dx, this.motionY * tickDiff, dz);
            Vector2 af = new Vector2(this.x, this.z);

            if ((be.x != af.x || be.y != af.y) && !isJump) {
                this.moveTime -= 90 * tickDiff;
            }
        }

        if (!isJump) {
            if (this.onGround) {
                this.motionY = 0;
            } else if (this.motionY > -0.32) {
                if (!(this.level.getBlock(NukkitMath.floorDouble(this.x), (int) (this.y + 0.8),
                        NukkitMath.floorDouble(this.z)) instanceof BlockLiquid)) {
                    this.motionY -= 0.08;
                }
            } else {
                this.motionY -= 0.08 * tickDiff;
            }
        }

        this.updateMovement();
    }

    @Override
    public boolean onUpdate(int currentTick) {
        if (this.closed) {
            return false;
        }

        if (!this.isAlive()) {
            this.close();
            return false;
        }

        int tickDiff = currentTick - this.lastUpdate;
        this.lastUpdate = currentTick;
        this.entityBaseTick(tickDiff);

        this.updateMove(tickDiff);

        if (!this.closed && this.ticksLived++ > 1 && this.ticksLived % 500 == 0) {
            for (Entity entity : this.getLevel().getEntities()) {
                if (entity instanceof EntityPet && entity != this && ((EntityPet) entity).owner.equalsIgnoreCase(this.owner)) {
                    this.close();
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean entityBaseTick(int tickDiff) {
        if (this.moveTime > 0) {
            this.moveTime -= tickDiff;
        }

        if (this.y < 0 && Main.getInstance().canTeleportPet()) {
            Player pl = this.getServer().getPlayerExact(this.owner);
            if (pl != null && !pl.isSpectator() && pl.getY() > 0) {
                if (this.distanceSquared(pl) > 500) {
                    this.despawnFromAll();
                }
                this.teleport(pl);
                this.onGround = false;
            }
        }

        if (this.inLoveTicks > 0) {
            this.inLoveTicks -= tickDiff;
            if (this.age % 20 == 0) {
                for (int i = 0; i < 3; i++) {
                    this.level.addParticle(new HeartParticle(this.add(Utils.rand(-1.0, 1.0), this.getMountedYOffset() + Utils.rand(-1.0, 1.0), Utils.rand(-1.0, 1.0))));
                }
            }
        }

        return true;
    }

    @Override
    public boolean move(double x, double y, double z) {
        if (y < -5 || y > 5) {
            return false;
        }

        return super.move(x, y, z);
    }

    protected float getMountedYOffset() {
        return getHeight() * 0.75F;
    }

    protected abstract String getSaveName();
}
