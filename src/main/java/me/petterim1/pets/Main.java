package me.petterim1.pets;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntitySpawnEvent;
import cn.nukkit.event.player.PlayerTeleportEvent;
import cn.nukkit.level.Level;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import me.petterim1.pets.entities.*;

import java.util.Map;

/*

PPPPPPPPPPPPPPPPP                              tttt                            !!!
P::::::::::::::::P                          ttt:::t                           !!:!!
P::::::PPPPPP:::::P                         t:::::t                           !:::!
PP:::::P     P:::::P                        t:::::t                           !:::!
P::::P     P:::::P  eeeeeeeeeeee    ttttttt:::::ttttttt        ssssssssss     !:::!
P::::P     P:::::Pee::::::::::::ee  t:::::::::::::::::t      ss::::::::::s    !:::!
P::::PPPPPP:::::Pe::::::eeeee:::::eet:::::::::::::::::t    ss:::::::::::::s   !:::!
P:::::::::::::PPe::::::e     e:::::etttttt:::::::tttttt    s::::::ssss:::::s  !:::!
P::::PPPPPPPPP  e:::::::eeeee::::::e      t:::::t           s:::::s  ssssss   !:::!
P::::P          e:::::::::::::::::e       t:::::t             s::::::s        !:::!
P::::P          e::::::eeeeeeeeeee        t:::::t                s::::::s     !!:!!
P::::P          e:::::::e                 t:::::t      ttttt       sss:::::s   !!! 
PP::::::PP        e::::::::e                t::::::tttt:::::ts:::::ssss::::::s
P::::::::P         e::::::::eeeeeeee        tt::::::::::::::ts::::::::::::::s  !!!
P::::::::P          ee:::::::::::::e          tt:::::::::::tt s:::::::::::ss  !!:!!
PPPPPPPPPP            eeeeeeeeeeeeee            ttttttttttt    sssssssssss     !!!

*----------------------*
| Pets for Nukkit      |
| Created by PetteriM1 |
*----------------------*

*/
public class Main extends PluginBase implements Listener {

    private static final int configVersion = 3;
    private Config config;
    private static Main instance;
    private String nameTagColor;
    private boolean teleportPets;
    private int feedXp;

    public static Main getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.config = this.getConfig();

        if (config.getInt("configVersion") != configVersion) {
            switch (config.getInt("configVersion")) {
                case 1:
                    config.set("teleportPets", true);
                    config.set("configVersion", configVersion);
                    config.save();
                    this.config = this.getConfig();
                    this.getServer().getLogger().info("Pets plugin config file updated.");
                    break;
                case 2:
                    config.set("callPetSwitchWorld", true);
                    config.set("configVersion", configVersion);
                    config.save();
                    this.config = this.getConfig();
                    this.getServer().getLogger().info("Pets plugin config file updated.");
                    break;
                default:
                    this.getServer().getLogger().warning("Pets plugin config file version is unknown. Unable to update.");
            }
        }

        this.nameTagColor = config.getString("nameTagColor");
        this.teleportPets = config.getBoolean("teleportPets");
        this.feedXp = config.getInt("feedXp");

        this.registerPets();
        this.getServer().getPluginManager().registerEvents(this, this);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equals("pet")) {
            if (!sender.hasPermission("pets.command") && !sender.hasPermission("pets.manage")) {
                return false;
            }

            if (args.length == 0) {
                sendPetsList(sender);
                return true;
            }

            switch (args[0].toLowerCase()) {
                case "add":
                    Player pl;
                    if (args.length == 2 && sender instanceof Player) {
                        pl = (Player) sender;
                    } else if (args.length != 3) {
                        sender.sendMessage("\u00A76Usage: /pet add [player] <pet>");
                        return true;
                    } else {
                        pl = this.getServer().getPlayerExact(args[1]);
                        if (pl == null) {
                            sender.sendMessage("\u00A7d>> \u00A7cUnknown player");
                            return true;
                        }
                    }

                    if (sender instanceof Player && sender != pl && !sender.hasPermission("pets.manage")) {
                        return false;
                    }

                    if (hasPet(pl.getName())) {
                        sender.sendMessage("\u00A7d>> \u00A7cThis player already has a pet");
                        return true;
                    }

                    Entity ent = Entity.createEntity("Pet" + args[args.length - 1], pl);
                    if (ent != null) {
                        if (!(ent instanceof EntityPet)) {
                            ent.close();
                            sendPetsList(sender);
                            return true;
                        }

                        ((EntityPet) ent).setOwner(pl.getName());
                        ((EntityPet) ent).setRandomType();
                        ent.spawnToAll();

                        config.set("players." + pl.getName().toLowerCase(), args[args.length - 1]);
                        config.save();

                        sender.sendMessage("\u00A7d>> \u00A7aPet added");
                        getLogger().info("Pet added for " + pl.getName());
                        return true;
                    }

                    sender.sendMessage("\u00A7d>> \u00A7cUnknown pet: " + args[args.length - 1]);
                    return true;
                case "remove":
                    if (args.length == 1 && sender instanceof Player) {
                        pl = (Player) sender;
                    } else if (args.length != 2) {
                        sender.sendMessage("\u00A76Usage: /pet remove [player]");
                        return true;
                    } else {
                        pl = this.getServer().getPlayerExact(args[1]);
                        if (pl == null) {
                            sender.sendMessage("\u00A7d>> \u00A7cUnknown player");
                            return true;
                        }
                    }

                    if (sender instanceof Player && sender != pl && !sender.hasPermission("pets.manage")) {
                        return false;
                    }

                    String se = pl.getName().toLowerCase();
                    if (!hasPet(se)) {
                        sender.sendMessage("\u00A7d>> \u00A7cThis player does not have a pet");
                        return true;
                    }

                    ((Map) config.get("players")).remove(se);
                    config.save();

                    for (Level level : this.getServer().getLevels().values()) {
                        for (Entity entity : level.getEntities()) {
                            if (entity instanceof EntityPet) {
                                if (((EntityPet) entity).isOwner(pl)) {
                                    entity.close();
                                    sender.sendMessage("\u00A7d>> \u00A7aPet removed");
                                    getLogger().info("Pet removed from " + pl.getName());
                                    return true;
                                }
                            }
                        }
                    }

                    sender.sendMessage("\u00A7d>> \u00A7eThe pet will be removed next time its chunk is loaded");
                    return true;
                case "list":
                    sendPetsList(sender);
                    return true;
                default:
                    sendCommandList(sender);
            }
        } else if (cmd.getName().equals("callpet")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("\u00A7d>> \u00A7cThis command only works in game");
                return true;
            }

            if (!sender.hasPermission("pets.call")) {
                return false;
            }

            if (!hasPet(sender.getName())) {
                sender.sendMessage("\u00A7d>> \u00A7cYou don't have a pet");
                return true;
            }

            for (Level level : this.getServer().getLevels().values()) {
                for (Entity entity : level.getEntities()) {
                    if (entity instanceof EntityPet) {
                        if (((EntityPet) entity).isOwner((Player) sender)) {
                            if (/*((Player) sender).distance(entity) > 50 ||*/ !((Player) sender).getLevel().equals(entity.getLevel())) {
                                if (!config.getBoolean("callPetSwitchWorld")) {
                                    sender.sendMessage("\u00A7d>> \u00A7cYou cannot teleport your pet to this world");
                                    return true;
                                }

                                entity.setLevel(((Player) sender).getLevel());
                                entity.teleport((Player) sender);
                            } else {
                                entity.teleport((Player) sender);
                                /*((EntityPet) entity).target = (Player) sender;
                                ((EntityPet) entity).stayTime = 0;
                                ((EntityPet) entity).findingPlayer = true;*/
                            }

                            entity.onGround = false;
                            if (((EntityPet) entity).isSitting()) {
                                ((EntityPet) entity).setSitting();
                            }
                            entity.scheduleUpdate();

                            sender.sendMessage("\u00A7d>> \u00A7aPet called");
                            return true;
                        }
                    }
                }
            }

            sender.sendMessage("\u00A7d>> \u00A7cPet not found. It might be on an unloaded chunk");
            return true;
        }

        return true;
    }

    private static void sendPetsList(CommandSender sender) {
        sender.sendMessage("\u00A7d>> \u00A7aAvailable pets: \u00A76Cat, Chicken, Cow, Dog, Fox, Pig, PolarBear, Sheep");
    }

    private static void sendCommandList(CommandSender sender) {
        sender.sendMessage("\u00A7d* \u00A7aPets \u00A7d*");
        sender.sendMessage("\u00A76/pet add [player] <pet>");
        sender.sendMessage("\u00A76/pet remove [player]");
        sender.sendMessage("\u00A76/pet list");
        sender.sendMessage("\u00A76/callpet");
    }

    public boolean hasPet(String player) {
        String cfgSE = config.getString("players." + player.toLowerCase());
        return cfgSE.equals("Cat") || cfgSE.equals("Chicken") || cfgSE.equals("Cow") || cfgSE.equals("Dog") || cfgSE.equals("Fox") || cfgSE.equals("Pig") || cfgSE.equals("PolarBear") || cfgSE.equals("Sheep");
    }

    public boolean hasPet(String player, String pet) {
        return config.getString("players." + player.toLowerCase()).equals(pet);
    }

    private void registerPets() {
        Entity.registerEntity("PetCat", PetCat.class);
        Entity.registerEntity("PetChicken", PetChicken.class);
        Entity.registerEntity("PetCow", PetCow.class);
        Entity.registerEntity("PetDog", PetDog.class);
        Entity.registerEntity("PetFox", PetFox.class);
        Entity.registerEntity("PetPig", PetPig.class);
        Entity.registerEntity("PetPolarBear", PetPolarBear.class);
        Entity.registerEntity("PetSheep", PetSheep.class);
    }

    public String getNameTagColor() {
        return this.nameTagColor;
    }

    public boolean canTeleportPet() {
        return this.teleportPets;
    }

    public int getFeedXp() {
        return this.feedXp;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onTeleport(PlayerTeleportEvent e) {
        Player pl = e.getPlayer();
        if (pl.isSpectator() || !canTeleportPet()) return;
        for (Level level : this.getServer().getLevels().values()) {
            for (Entity entity : level.getEntities()) {
                if (entity instanceof EntityPet) {
                    if (((EntityPet) entity).isOwner(pl) && !((EntityPet) entity).isSitting()) {
                        entity.setLevel(e.getTo().getLevel());
                        entity.teleport(e.getTo());
                        entity.onGround = false;
                        ((EntityPet) entity).setSitting();
                        entity.scheduleUpdate();
                        return;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onSpawn(EntitySpawnEvent e) {
        if (e.getEntity() instanceof EntityPet) {
            String owner = e.getEntity().namedTag.getString("Owner");
            if (owner != null && !owner.isEmpty()) {
                if (!hasPet(owner, ((EntityPet) e.getEntity()).getSaveName())) {
                    e.getEntity().kill();
                    getLogger().info("Pet removed from " + owner);
                }
            }
        }
    }
}
