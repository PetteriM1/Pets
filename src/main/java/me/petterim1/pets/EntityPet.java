package me.petterim1.pets;

import cn.nukkit.Player;
import cn.nukkit.block.*;
import cn.nukkit.command.CommandSender;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.entity.passive.EntityAnimal;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.particle.CriticalParticle;
import cn.nukkit.level.particle.HeartParticle;
import cn.nukkit.math.NukkitMath;
import cn.nukkit.math.Vector2;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;

public abstract class EntityPet extends EntityCreature {

    protected String owner;
    protected Vector3 target;
    protected Entity followTarget;
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

    public boolean isOwner(CommandSender p) {
        return p.getName().equals(this.owner);
    }

    public void setOwner(String owner) {
        this.owner = owner;
        this.setNameTag(this.getName());
        this.saveNBT();
    }

    public boolean targetOption(EntityCreature creature, double distance) {
        if (creature instanceof Player) {
            Player player = (Player) creature;
            return player.isAlive() && !player.closed && isOwner(player) && distanceCheck(distance);
        }

        return false;
    }

    private boolean distanceCheck(double distance) {
        boolean dis = distance < 300 && distance > 12;

        if (dis && findingPlayer) {
            findingPlayer = false;
        }

        return dis || findingPlayer;
    }

    public void setSitting() {
        this.moveTime = 0;
        this.stayTime = 60;

        if (this.namedTag.getByte("Sitting") == 0) {
            this.namedTag.putByte("Sitting", 1);
            this.setDataFlag(DATA_FLAGS, DATA_FLAG_SITTING, true);
            this.sitting = true;
        } else {
            this.namedTag.putByte("Sitting", 0);
            this.setDataFlag(DATA_FLAGS, DATA_FLAG_SITTING, false);
            this.sitting = false;
        }
    }

    public boolean isSitting() {
        return this.sitting;
    }

    protected void checkTarget() {
        if (this.followTarget != null && !this.followTarget.closed && this.followTarget.isAlive()) {
            return;
        }

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
            this.stayTime = Utils.rand(100, 200);
            this.target = this.add(Utils.rand() ? x : -x, Utils.rand(-20, 20) / 10, Utils.rand() ? z : -z);
        } else if (this.moveTime <= 0 || this.target == null) {
            x = Utils.rand(10, 30);
            z = Utils.rand(10, 30);
            this.stayTime = 0;
            this.moveTime = Utils.rand(60, 200);
            this.target = this.add(Utils.rand() ? x : -x, 0, Utils.rand() ? z : -z);
        }
    }

    protected boolean checkJump(double dx, double dz) {
        if (this.motionY == 0.16) {
            int b = level.getBlockIdAt(NukkitMath.floorDouble(this.x), (int) this.y, NukkitMath.floorDouble(this.z));
            return b == BlockID.WATER || b == BlockID.STILL_WATER;
        } else {
            int b = level.getBlockIdAt(NukkitMath.floorDouble(this.x), (int) (this.y + 0.8), NukkitMath.floorDouble(this.z));
            if (b == BlockID.WATER || b == BlockID.STILL_WATER) {
                this.motionY = 0.16;
                return true;
            }
        }

        if (!this.onGround || this.stayTime > 0) {
            return false;
        }

        Block that = this.getLevel().getBlock(new Vector3(NukkitMath.floorDouble(this.x + dx), (int) this.y, NukkitMath.floorDouble(this.z + dz)));
        Block block = that.getSide(this.getHorizontalFacing());
        Block down = block.down();
        if (!down.isSolid() && !block.isSolid() && !down.down().isSolid()) {
            this.stayTime = 10;
        } else if (!block.canPassThrough() && block.up().canPassThrough() && that.up(2).canPassThrough()) {
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

    public Vector3 updateMove(int tickDiff) {
        if (this.isSitting()) {
            return this.target;
        }

        if (this.followTarget != null && !this.followTarget.closed && this.followTarget.isAlive()) {
            double x = this.followTarget.x - this.x;
            double z = this.followTarget.z - this.z;

            double diff = Math.abs(x) + Math.abs(z);
            if (this.stayTime > 0 || (!findingPlayer && this.distance(this.followTarget) <= (this.getWidth() + 0.0d) / 2 + 0.05)) {
                this.motionX = 0;
                this.motionZ = 0;
            } else {
                if (this.isInsideOfWater()) {
                    this.motionX = 0.06 * (x / diff);
                    this.motionZ = 0.06 * (z / diff);
                } else {
                    this.motionX = 0.12 * (x / diff);
                    this.motionZ = 0.12 * (z / diff);
                }
            }
            this.yaw = Math.toDegrees(-Math.atan2(x / diff, z / diff));
            return this.followTarget;
        }

        Vector3 before = this.target;
        this.checkTarget();
        if (this.target instanceof EntityCreature || before != this.target) {
            double x = this.target.x - this.x;
            double z = this.target.z - this.z;

            double diff = Math.abs(x) + Math.abs(z);
            if (this.stayTime > 0 || (!findingPlayer && this.distance(this.target) <= (this.getWidth() + 0.0d) / 2 + 0.05)) {
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
            this.yaw = Math.toDegrees(-Math.atan2(x / diff, z / diff));
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
                if (!(this.level.getBlock(new Vector3(NukkitMath.floorDouble(this.x), (int) (this.y + 0.8),
                        NukkitMath.floorDouble(this.z))) instanceof BlockLiquid)) {
                    this.motionY -= 0.08;
                }
            } else {
                this.motionY -= 0.08 * tickDiff;
            }
        }

        this.updateMovement();

        return this.target;
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

        Vector3 target = this.updateMove(tickDiff);

        if (target instanceof Player) {
            if (this.distanceSquared(target) <= 20) {
                this.x = this.lastX;
                this.y = this.lastY;
                this.z = this.lastZ;
            }
        }

        return true;
    }

    @Override
    public boolean entityBaseTick(int tickDiff) {
        if (this.moveTime > 0) {
            this.moveTime -= tickDiff;
        }

        if (inLoveTicks > 0) {
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
}
