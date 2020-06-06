package com.foxxite.multicharacter.worldspacemenu;

import com.foxxite.multicharacter.MultiCharacter;
import com.foxxite.multicharacter.character.Character;
import com.foxxite.multicharacter.config.Language;
import com.foxxite.multicharacter.misc.Common;
import com.foxxite.multicharacter.sql.SQLHandler;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.SoundCategory;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class WorldSpaceMenu implements Listener {

    private final MultiCharacter plugin;
    private final MinecraftServer server;
    private final CraftPlayer cPlayer;
    private final WorldServer world;
    private final GameProfile profile;
    private final PlayerInteractManager interactManager;
    private final PlayerConnection connection;
    private final SQLHandler sqlHandler;
    private final Language language;
    private final FileConfiguration config;
    private final NamespacedKey namespacedKey;
    private Location lastArmorStandPos;
    private ArrayList<ArmorStand> charInfoStands = new ArrayList<>();
    private int lastSelectedStand = -1;
    private int selectedStand = 0;
    private Player player;
    private EntityPlayer fakeEntityPlayer;
    private ArmorStand charStand1;
    private ArmorStand charStand2;
    private ArmorStand charStand3;
    private ArmorStand staffModeStand;

    public WorldSpaceMenu(MultiCharacter plugin, Player player) {
        this.plugin = plugin;
        this.player = player;

        // Reset Player Rotation
        Location playerLoc = player.getLocation();
        playerLoc.setYaw(180);
        playerLoc.setPitch(0);
        player.teleport(playerLoc);

        sqlHandler = plugin.getSqlHandler();
        language = plugin.getLanguage();
        config = plugin.getConfiguration();

        namespacedKey = new NamespacedKey(plugin, "character-uuid");

        connection = ((CraftPlayer) player).getHandle().playerConnection;

        lastArmorStandPos = player.getLocation().clone();

        server = MinecraftServer.getServer();
        cPlayer = (CraftPlayer) player;
        world = ((CraftWorld) player.getWorld()).getHandle();
        profile = new GameProfile(UUID.randomUUID(), "");
        interactManager = new PlayerInteractManager(world);

        spawnNPC();

        spawnArmorStands();

        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        new BukkitRunnable() {
            @Override
            public void run() {
                updateMenu();
            }
        }.runTaskLater(plugin, 10L);

    }

    public void closeMenu() {

        for (int i = 0; i < 6; i++) {
            int entityId = 0;

            switch (i) {
                case 0:
                    entityId = charStand1.getEntityId();
                    break;
                case 1:
                    entityId = charStand2.getEntityId();
                    break;
                case 2:
                    entityId = charStand3.getEntityId();
                    break;
                case 3:
                    while (!charInfoStands.isEmpty()) {
                        entityId = charInfoStands.get(0).getEntityId();
                        PacketPlayOutEntityDestroy packetPlayOutEntityDestroy = new PacketPlayOutEntityDestroy(entityId);
                        connection.sendPacket(packetPlayOutEntityDestroy);

                        charInfoStands.get(0).remove();
                        charInfoStands.remove(0);
                    }
                    entityId = -1;
                    break;
                case 4:
                    entityId = staffModeStand.getEntityId();
                    break;
                case 5:
                    entityId = fakeEntityPlayer.getId();
                    break;
            }

            if (entityId != -1) {
                PacketPlayOutEntityDestroy packetPlayOutEntityDestroy = new PacketPlayOutEntityDestroy(entityId);
                connection.sendPacket(packetPlayOutEntityDestroy);
            }

        }

        charStand1.remove();
        charStand2.remove();
        charStand3.remove();
        staffModeStand.remove();

        charInfoStands = null;
        fakeEntityPlayer = null;
        player = null;
    }

    private void spawnNPC() {

        PropertyMap pm = profile.getProperties();

        final String textureValue = "ewogICJ0aW1lc3RhbXAiIDogMTU5MTM3MjMyODQwOCwKICAicHJvZmlsZUlkIiA6ICJiZWNkZGIyOGEyYzg0OWI0YTliMDkyMmE1ODA1MTQyMCIsCiAgInByb2ZpbGVOYW1lIiA6ICJTdFR2IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzlhODY3OTM1OWJiN2NiODUzYzUyYmFhMDNhYWZlZTU3MmM4ZTE0NGRjNWI4NTlmNzY1YjI2NTNiMTkxNGM0ODIiCiAgICB9CiAgfQp9";

        final String textureSignature = "F3zJ4gOCyWCsCjP3JyiY0/Il+GqmJDyc1OGNiBzRny4RhKn+8JGoAyGfcp77UL3x9JzTcn/V37b3qIWcyPKOAxP391QKtHgKmGthi6vc+rIdRZNMlBU0rSVZWizb9nG9Qmjhjl1APCJa26T2g7Wt7yePCLQV5feBlBkv8GRt+GKSmPtiuTFRXzY/fkFemHwxlyGJqoVMcoyW/xmXeV2pq2ZDLSaLH8UwiucPQcc5uv87fSmrRvScdm7auzteXQgcPJBx7zost5/Q0IK+g0q033pzwbA5uU4Qp3tfPesrMKIC2PtbeuKyu+IXaj0SVOZjO5KSZKRiSvtVdiyoc8jN3YxZl5u0Dln0LEuHHBUvIxOjz69fq7syHJTQ++8fHf+7Gfn2GEKQRELIDbQ6MrPwq/N+Y0RbO+nlzWSt0TMVSR27/bqu2VaMgHLnpc0pFN8aPHb5sGotYhzzyRDG4joRCDntYc3ZfaoUi8DkPQy5c5zXqakmb/riPCsKYT3rxKpzPdAKS6fU3ulg6WIJVSaeKlIjIcXjam7NhP9l+ze0W71MFuQJTJYeqYimrCO8zGqN9M3/Yr9Ua20GCfJ2IcYd0NvB/FF7+jW7qZkkj190M0ZmkfURyXZc2UyUNcrstCaK5Ykg1SP/ZIkaiq+lcS+lNxuEowmSfUlNnib0xzGHHzU=";

        pm.put("textures", new Property("textures", textureValue, textureSignature));

        Location NPCLocation = player.getLocation().clone();
        NPCLocation.add(2, 0, -3);

        Location lookATTarget = lookAtTarget(player.getLocation(), NPCLocation);

        fakeEntityPlayer = new EntityPlayer(server, world, profile, interactManager);
        fakeEntityPlayer.setLocation(NPCLocation.getX(), NPCLocation.getY(), NPCLocation.getZ(), NPCLocation.getYaw(), NPCLocation.getPitch());

        PacketPlayOutNamedEntitySpawn packetPlayOutNamedEntitySpawn = new PacketPlayOutNamedEntitySpawn(fakeEntityPlayer);
        PacketPlayOutPlayerInfo packetPlayOutPlayerInfo = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, fakeEntityPlayer);

        //Make NPC face the right way
        PacketPlayOutEntity.PacketPlayOutEntityLook packetPlayOutEntityLook = new PacketPlayOutEntity.PacketPlayOutEntityLook(fakeEntityPlayer.getId(), Common.toPackedByte(lookATTarget.getYaw()), Common.toPackedByte(lookATTarget.getPitch()), true);

        PacketPlayOutEntityHeadRotation packetPlayOutEntityHeadRotation = new PacketPlayOutEntityHeadRotation(fakeEntityPlayer, Common.toPackedByte(lookATTarget.getYaw()));

        //Send packets
        connection.sendPacket(packetPlayOutPlayerInfo);
        connection.sendPacket(packetPlayOutNamedEntitySpawn);
        connection.sendPacket(packetPlayOutEntityLook);
        connection.sendPacket(packetPlayOutEntityHeadRotation);

    }

    private void spawnArmorStands() {

        ArmorStand localArmorStand;

        for (int i = 0; i < 4; i++) {

            //localArmorStand = new EntityArmorStand(EntityTypes.ARMOR_STAND, world);
            localArmorStand = (ArmorStand) Common.createLivingEntity(EntityType.ARMOR_STAND, player.getLocation());
            localArmorStand.setCustomNameVisible(true);
            localArmorStand.setSmall(true);
            localArmorStand.setGravity(false);
            localArmorStand.setVisible(false);

            localArmorStand.setCustomName(language.getMessage("character-selection.new-character.name"));

            switch (i) {
                case 0:
                    charStand1 = localArmorStand;
                    lastArmorStandPos.add(-2.25, 1, -3);
                    break;
                case 1:
                    charStand2 = localArmorStand;
                    lastArmorStandPos.subtract(0, 0.4, 0);
                    break;
                case 2:
                    charStand3 = localArmorStand;
                    lastArmorStandPos.subtract(0, 0.4, 0);
                    break;
                case 3:
                    // Don't show admin mode is no perms
                    if (player.hasPermission("multicharacter.admin")) {
                        staffModeStand = localArmorStand;
                        localArmorStand.setCustomName(language.getMessage("character-selection.staff-mode.name"));
                    } else {
                        staffModeStand = localArmorStand;
                        localArmorStand.setCustomName(ChatColor.BLACK + "");
                    }
                    lastArmorStandPos.subtract(0, 0.4, 0);
                    break;
            }

            localArmorStand.teleport(lastArmorStandPos);

            EntityArmorStand entityArmorStand = ((CraftArmorStand) localArmorStand).getHandle();

            PacketPlayOutSpawnEntity packetPlayOutSpawnEntity = new PacketPlayOutSpawnEntity(entityArmorStand);
            PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(entityArmorStand.getId(), entityArmorStand.getDataWatcher(), true);

            connection.sendPacket(packetPlayOutSpawnEntity);
            connection.sendPacket(metadata);

        }

        // Spawn stands for the info hologram
        spawnInfoStands();

        HashMap<String, String> tableLayout = new HashMap<>();
        tableLayout.put("UUID", "string");
        tableLayout.put("OwnerUUID", "string");

        String selectQuery = "SELECT UUID, OwnerUUID FROM Characters WHERE Deleted = 0 AND OwnerUUID = '" + player.getUniqueId().toString() + "'";

        HashMap<Integer, HashMap<String, Object>> queryResult = sqlHandler.executeQuery(selectQuery, tableLayout);

        //Overwrite new character with excising characters
        if (queryResult != null && queryResult.size() > 0) {

            for (int i = 0; i < queryResult.size(); i++) {

                HashMap<String, Object> row = queryResult.get(i);

                Character character = getCharacterData(UUID.fromString((String) queryResult.get(i).get("UUID")));

                ArmorStand localStand;

                switch (i) {
                    case 0:
                        localStand = charStand1;
                        charStand1.setCustomName(character.getName());
                        charStand1.getPersistentDataContainer().set(namespacedKey, PersistentDataType.STRING, character.getCharacterID().toString());
                        break;
                    case 1:
                        localStand = charStand2;
                        charStand2.setCustomName(character.getName());
                        charStand2.getPersistentDataContainer().set(namespacedKey, PersistentDataType.STRING, character.getCharacterID().toString());
                        break;
                    case 2:
                        localStand = charStand3;
                        charStand3.setCustomName(character.getName());
                        charStand3.getPersistentDataContainer().set(namespacedKey, PersistentDataType.STRING, character.getCharacterID().toString());
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + i);
                }

                EntityArmorStand entityArmorStand = ((CraftArmorStand) localStand).getHandle();

                PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(entityArmorStand.getId(), entityArmorStand.getDataWatcher(), true);
                connection.sendPacket(metadata);
            }
        }

    }

    private void spawnInfoStands() {

        lastArmorStandPos = player.getLocation().clone();

        lastArmorStandPos.add(0, 1.8, -3.5);

        ArmorStand localArmorStand;

        for (int j = 0; j < language.getMultiLineMessage("character-selection.character.lore").size(); j++) {

            localArmorStand = (ArmorStand) Common.createLivingEntity(EntityType.ARMOR_STAND, player.getLocation());
            localArmorStand.setCustomNameVisible(true);
            localArmorStand.setSmall(true);
            localArmorStand.setGravity(false);
            localArmorStand.setVisible(false);

            charInfoStands.add(localArmorStand);
            localArmorStand.setCustomName("Char Info");
            lastArmorStandPos.subtract(0, 0.25, 0);

            localArmorStand.teleport(lastArmorStandPos);

            EntityArmorStand entityArmorStand = ((CraftArmorStand) localArmorStand).getHandle();

            PacketPlayOutSpawnEntity packetPlayOutSpawnEntity = new PacketPlayOutSpawnEntity(entityArmorStand);
            PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(entityArmorStand.getId(), entityArmorStand.getDataWatcher(), true);

            connection.sendPacket(packetPlayOutSpawnEntity);
            connection.sendPacket(metadata);
        }
    }

    /*
        Source = What to look at
        Target = What should look at source
     */
    private Location lookAtTarget(Location source, Location target) {

        Vector direction = getVector(target).subtract(getVector(source)).normalize();
        double x = direction.getX();
        double y = direction.getY();
        double z = direction.getZ();

        // Now change the angle
        Location changed = target.clone();
        changed.setYaw(180 - toDegree(Math.atan2(x, z)));
        changed.setPitch(90 - toDegree(Math.acos(y)));

        return changed;
    }

    private float toDegree(double angle) {
        return (float) Math.toDegrees(angle);
    }

    private Vector getVector(Location source) {
        return source.toVector();
    }

    private Character getCharacterData(UUID uuid) {
        return new Character(plugin, uuid);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getPlayer() == player) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPlayerMove(PlayerMoveEvent event) {
        boolean jumped = false;
        if (event.getPlayer() == player) {
            event.setCancelled(true);

            // Check if player jumped
            if (event.getFrom().getY() < event.getTo().getY()) {
                jumped = true;

                if (selectedStand == 0) {
                    selectedStand = 2;
                    if (player.hasPermission("multicharacter.admin")) {
                        selectedStand = 3;
                    }
                } else {
                    selectedStand -= 1;
                }
            }

            updateMenu();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPlayerSneak(PlayerToggleSneakEvent event) {
        boolean sneaked = event.isSneaking();
        if (event.getPlayer() == player) {
            event.setCancelled(true);

            if (sneaked) {
                if (selectedStand >= 2) {
                    if (selectedStand == 2 && player.hasPermission("multicharacter.admin")) {
                        selectedStand = 3;
                    } else {
                        selectedStand = 0;
                    }
                } else {
                    selectedStand += 1;
                }
            }

            updateMenu();
        }
    }

    public void updateMenu() {

        // Don't update if we don't have to
        if (selectedStand == lastSelectedStand) {
            return;
        }

        lastSelectedStand = selectedStand;

        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, SoundCategory.MASTER, 0.5f, 1f);

        ArmorStand localStand;
        Character character;

        String pointerLeft = language.getMessage("character-selection.pointers.left");
        String pointerRight = language.getMessage("character-selection.pointers.right");

        // Reset Char Info
        for (ArmorStand charInfoStand : charInfoStands) {
            charInfoStand.setCustomName(ChatColor.BLACK + "");

            EntityArmorStand entityArmorStand = ((CraftArmorStand) charInfoStand).getHandle();
            PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(entityArmorStand.getId(), entityArmorStand.getDataWatcher(), true);
            connection.sendPacket(metadata);
        }

        // Reset names of all armor stands
        for (int i = 0; i < 4; i++) {
            switch (i) {
                case 0:
                    localStand = charStand1;
                    character = getCharacterData(UUID.fromString(localStand.getPersistentDataContainer().get(namespacedKey, PersistentDataType.STRING)));
                    localStand.setCustomName(character.getName());
                    break;
                case 1:
                    localStand = charStand2;
                    character = getCharacterData(UUID.fromString(localStand.getPersistentDataContainer().get(namespacedKey, PersistentDataType.STRING)));
                    localStand.setCustomName(character.getName());
                    break;
                case 2:
                    localStand = charStand3;
                    character = getCharacterData(UUID.fromString(localStand.getPersistentDataContainer().get(namespacedKey, PersistentDataType.STRING)));
                    localStand.setCustomName(character.getName());
                    break;
                //Staff Mode - Don't show if no perms
                case 3:
                    if (player.hasPermission("multicharacter.admin")) {
                        localStand = staffModeStand;
                        localStand.setCustomName(language.getMessage("character-selection.staff-mode.name"));
                    } else {
                        localStand = staffModeStand;
                        localStand.setCustomName(ChatColor.BLACK + "");
                    }
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + i);
            }

            EntityArmorStand entityArmorStand = ((CraftArmorStand) localStand).getHandle();

            PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(entityArmorStand.getId(), entityArmorStand.getDataWatcher(), true);
            connection.sendPacket(metadata);
        }


        // Set pointers for selected option
        switch (selectedStand) {
            case 0:
                localStand = charStand1;
                character = getCharacterData(UUID.fromString(localStand.getPersistentDataContainer().get(namespacedKey, PersistentDataType.STRING)));
                localStand.setCustomName(pointerLeft + character.getName() + pointerRight);
                break;
            case 1:
                localStand = charStand2;
                character = getCharacterData(UUID.fromString(localStand.getPersistentDataContainer().get(namespacedKey, PersistentDataType.STRING)));
                localStand.setCustomName(pointerLeft + character.getName() + pointerRight);
                break;
            case 2:
                localStand = charStand3;
                character = getCharacterData(UUID.fromString(localStand.getPersistentDataContainer().get(namespacedKey, PersistentDataType.STRING)));
                localStand.setCustomName(pointerLeft + character.getName() + pointerRight);
                break;
            //Staff Mode
            case 3:
                localStand = staffModeStand;
                localStand.setCustomName(pointerLeft + language.getMessage("character-selection.staff-mode.name") + pointerRight);
                character = null;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + selectedStand);
        }

        EntityArmorStand entityArmorStand = ((CraftArmorStand) localStand).getHandle();
        PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(entityArmorStand.getId(), entityArmorStand.getDataWatcher(), true);
        connection.sendPacket(metadata);


        // Update Char Info
        ArrayList<String> info = new ArrayList<>();
        int count = 0;

        switch (selectedStand) {
            case 0:
            case 1:
            case 2:
                HashMap<String, String> placeholdersLore = new HashMap<>();
                placeholdersLore.put("{birthday}", character.getBirthday());
                placeholdersLore.put("{nationality}", character.getNationality());
                placeholdersLore.put("{sex}", character.getSex());
                placeholdersLore.put("{balance}", String.valueOf(character.getVaultBalance()));
                placeholdersLore.put("{group}", character.getVaultGroup());

                info = language.getMultiLineMessageCustom("character-selection.character.lore", placeholdersLore);

                for (String line : info) {
                    charInfoStands.get(count).setCustomName(line);
                    count++;
                }

                break;

            // Staff Mode
            case 3:
                info = language.getMultiLineMessage("character-selection.staff-mode.lore");

                for (String line : info) {
                    charInfoStands.get(count).setCustomName(line);
                    count++;
                }
                break;
        }

        for (ArmorStand charInfoStand : charInfoStands) {
            entityArmorStand = ((CraftArmorStand) charInfoStand).getHandle();
            metadata = new PacketPlayOutEntityMetadata(entityArmorStand.getId(), entityArmorStand.getDataWatcher(), true);
            connection.sendPacket(metadata);
        }

        // Update NPC skin
        if (character != null) {
            PropertyMap pm = profile.getProperties();

            Property property = pm.get("textures").iterator().next();
            pm.remove("textures", property);
            pm.put("textures", new Property("textures", character.getSkinTexture(), character.getSkinSignature()));

            PacketPlayOutPlayerInfo infoRemove = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, fakeEntityPlayer);
            PacketPlayOutPlayerInfo infoAdd = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, fakeEntityPlayer);
            PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(fakeEntityPlayer.getId());
            PacketPlayOutNamedEntitySpawn spawn = new PacketPlayOutNamedEntitySpawn(fakeEntityPlayer);
            PacketPlayOutEntityMetadata meta = new PacketPlayOutEntityMetadata(fakeEntityPlayer.getId(), ((CraftPlayer) player).getHandle().getDataWatcher(), true);

            connection.sendPacket(destroy);
            connection.sendPacket(infoAdd);
            connection.sendPacket(spawn);
            connection.sendPacket(meta);

            new BukkitRunnable() {
                @Override
                public void run() {
                    connection.sendPacket(infoRemove);
                }
            }.runTaskLaterAsynchronously(plugin, 3L);

            // Make NPC face the right way
            Location NPCLocation = player.getLocation().clone();
            NPCLocation.add(2, 0, -3);

            Location lookATTarget = lookAtTarget(player.getLocation(), NPCLocation);

            fakeEntityPlayer.setLocation(NPCLocation.getX(), NPCLocation.getY(), NPCLocation.getZ(), NPCLocation.getYaw(), NPCLocation.getPitch());

            PacketPlayOutEntity.PacketPlayOutEntityLook packetPlayOutEntityLook = new PacketPlayOutEntity.PacketPlayOutEntityLook(fakeEntityPlayer.getId(), Common.toPackedByte(lookATTarget.getYaw()), Common.toPackedByte(lookATTarget.getPitch()), true);

            PacketPlayOutEntityHeadRotation packetPlayOutEntityHeadRotation = new PacketPlayOutEntityHeadRotation(fakeEntityPlayer, Common.toPackedByte(lookATTarget.getYaw()));

            connection.sendPacket(packetPlayOutEntityLook);
            connection.sendPacket(packetPlayOutEntityHeadRotation);
        }
    }
}
