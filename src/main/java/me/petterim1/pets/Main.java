package me.petterim1.pets;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
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
                    config.set("enablePetCall", true);
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

        this.registerPets();
        this.getServer().getPluginManager().registerEvents(this, this);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("pet")) {
            if (args.length == 0) {
                sender.sendMessage("\u00A7d* \u00A7aPets \u00A7d*");
                sender.sendMessage("\u00A76/pet add <player> <pet>");
                sender.sendMessage("\u00A76/pet remove <player>");
                sender.sendMessage("\u00A76/pet list");
                sender.sendMessage("\u00A76/callpet");
                return true;
            }

            switch (args[0].toLowerCase()) {
                case "add":
                    if (args.length != 3) {
                        sender.sendMessage("\u00A76Usage: /pet add <player> <pet>");
                        return true;
                    }

                    Player pl = this.getServer().getPlayerExact(args[1]);
                    if (pl == null) {
                        sender.sendMessage("\u00A7d>> \u00A7cUnknown player");
                        return true;
                    }

                    String a1 = args[1].toLowerCase();
                    if (config.getString("players." + a1).contains("Cat") || config.getString("players." + a1).contains("Dog") || config.getString("players." + a1).contains("Chicken") || config.getString("players." + a1).contains("Pig") || config.getString("players." + a1).contains("Fox")) {
                        sender.sendMessage("\u00A7d>> \u00A7cThis player already have a pet");
                        return true;
                    }

                    Entity ent = Entity.createEntity("Pet" + args[2], pl);

                    if (ent != null) {
                        if (!(ent instanceof EntityPet)) {
                            ent.close();
                            sendPetsList(sender);
                            return true;
                        }

                        ((EntityPet) ent).setOwner(pl.getName());
                        ((EntityPet) ent).setRandomType();
                        ent.spawnToAll();

                        config.set("players." + a1, args[2]);
                        config.save();

                        sender.sendMessage("\u00A7d>> \u00A7aPet added");
                        return true;
                    }

                    sender.sendMessage("\u00A7d>> \u00A7cUnable to add pet");
                    return true;
                case "remove":
                    if (args.length != 2) {
                        sender.sendMessage("\u00A76Usage: /pet remove <player>");
                        return true;
                    }

                    Player pla = this.getServer().getPlayerExact(args[1]);
                    if (pla == null) {
                        sender.sendMessage("\u00A7d>> \u00A7cUnknown player");
                        return true;
                    }

                    String se = args[1].toLowerCase();
                    if (!config.getString("players." + se).contains("Cat") && !config.getString("players." + se).contains("Dog") && !config.getString("players." + se).contains("Chicken") && !config.getString("players." + se).contains("Pig") && !config.getString("players." + se).contains("Fox")) {
                        sender.sendMessage("\u00A7d>> \u00A7cThis player does not have a pet");
                        return true;
                    }

                    ((Map) config.get("players")).remove(se);
                    config.save();

                    for (Level level : this.getServer().getLevels().values()) {
                        for (Entity entity : level.getEntities()) {
                            if (entity instanceof EntityPet) {
                                if (((EntityPet) entity).getOwner() == pla) {
                                    entity.close();
                                    sender.sendMessage("\u00A7d>> \u00A7aPet removed");
                                    return true;
                                }
                            }
                        }
                    }

                    sender.sendMessage("\u00A7d>> \u00A7cPet not found");
                    return true;
                case "list":
                    sendPetsList(sender);
                    return true;
                default:
                    sender.sendMessage("\u00A7d* \u00A7aPets \u00A7d*");
                    sender.sendMessage("\u00A76/pet add <player> <pet>");
                    sender.sendMessage("\u00A76/pet remove <player>");
                    sender.sendMessage("\u00A76/pet list");
                    sender.sendMessage("\u00A76/callpet");
            }
        } else if (cmd.getName().equalsIgnoreCase("callpet")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("\u00A7d>> \u00A7cThis command only works in game");
                return true;
            }

            String se = sender.getName().toLowerCase();
            if (!config.getString("players." + se).contains("Cat") && !config.getString("players." + se).contains("Chicken") && !config.getString("players." + se).contains("Cow") && !config.getString("players." + se).contains("Dog") && !config.getString("players." + se).contains("Fox") && !config.getString("players." + se).contains("Pig") && !config.getString("players." + se).contains("PolarBear") && !config.getString("players." + se).contains("Sheep")) {
                sender.sendMessage("\u00A7d>> \u00A7cYou don't have a pet");
                return true;
            }

            for (Level level : this.getServer().getLevels().values()) {
                for (Entity entity : level.getEntities()) {
                    if (entity instanceof EntityPet) {
                        if (((EntityPet) entity).getOwner() == sender) {
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
                                ((EntityPet) entity).followTarget = (Player) sender;
                                ((EntityPet) entity).stayTime = 0;
                                ((EntityPet) entity).findingPlayer = true;*/
                            }

                            sender.sendMessage("\u00A7d>> \u00A7aPet called");
                            return true;
                        }
                    }
                }
            }

            sender.sendMessage("\u00A7d>> \u00A7cPet not found");
            return false;
        }

        return true;
    }

    private static void sendPetsList(CommandSender sender) {
        sender.sendMessage("\u00A7d>> \u00A7aAvailable pets: \u00A76Cat, Chicken, Cow, Dog, Fox, Pig, PolarBear, Sheep");
    }

    public Config getPluginConfig() {
        return this.config;
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

    String getNameTagColor() {
        return config.getString("nameTagColor");
    }

    @EventHandler(ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent e) {
        if (!config.getBoolean("teleportPets")) return;
        Player p = e.getPlayer();
        for (Level level : this.getServer().getLevels().values()) {
            for (Entity entity : level.getEntities()) {
                if (entity instanceof EntityPet) {
                    if (((EntityPet) entity).getOwner() == p) {
                        entity.setLevel(e.getTo().getLevel());
                        entity.teleport(e.getTo());
                    }
                }
            }
        }
    }
}
