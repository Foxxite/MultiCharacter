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
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.time.LocalDate;
import java.time.Month;
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
    private final Location playerLoginLocation;
    private String textureValue;
    private String textureSignature;
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

        skinEasterEgg();

        this.plugin = plugin;
        this.player = player;

        // Hide player for everyone
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.hidePlayer(player);
        }

        playerLoginLocation = player.getLocation().clone();

        sqlHandler = plugin.getSqlHandler();
        language = plugin.getLanguage();
        config = plugin.getConfiguration();

        // Reset Player Rotation
        double x = config.getDouble("menu-location.x");
        double y = config.getDouble("menu-location.y");
        double z = config.getDouble("menu-location.z");
        final float yaw = 180;
        final float pitch = 0;

        String confWorld = config.getString("menu-location.world");
        Location menuLocation = new Location(Bukkit.getWorld(confWorld), x, y, z, yaw, pitch);

        player.teleport(menuLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.setGameMode(GameMode.ADVENTURE);

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

    private void skinEasterEgg() {

        LocalDate currentDate = LocalDate.now();

        if (currentDate.getMonth() == Month.JUNE) {
            // LGBT Steve
            textureValue = "ewogICJ0aW1lc3RhbXAiIDogMTU5MTQ2MzY3NzExNSwKICAicHJvZmlsZUlkIiA6ICI3MmNiMDYyMWU1MTA0MDdjOWRlMDA1OTRmNjAxNTIyZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJNb3M5OTAiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzA2OWZjMDczOWEwNTFmMzZiZmM5YmM5ZDk1ZGI2NDZiMGE5NWZjZGEzZDViN2UyNmNkY2FhODg1NjAxOGYxOSIKICAgIH0KICB9Cn0=";
            textureSignature = "ouKYkQjA83oV7A5tPi9G6uCN5X5cC9cxgtAXnZ+OJnPovg7S0FY4OPhJoQEziIX7s5QigMCk1ccTujBODMof83PKWofaS0SjmTQpHN2fVzd30qj/sWOvMYhub43VqGK+o9hZlXAZ4rg8KOIlJLdFkRRyzng4LTd4QdsmNsSUQl+CTYsJ+8riA0iZ88kMzD5bGKOSQZWvgbr3ScKwYhKkD77cvODmyw+KmGT+ssJjSxOke8AnooLLupJ55lczFHmpxGElWTdxvRmHfh9P4NN04hYHTTucVxztbgM4xq3BDBIxOn/0PaVOZnNNectqVTwe89xLCoKAwkJ4QACOpBycD4mCAGVIXEkr7ZXytj5PaeodLdkU1hQ3Loyui4WZx7vbnl8nQG7o4ZB8iq44R2CkyaLNUL/jn7mvY2Dxz6yrJ5oRR1FKM3ucNTWKWpe8xaFe+zUBWh3HLt80pVxH6wt96wPAcD0TiWjz0CHyUbI8P6bJr4fKIZMrH7SubjVy4g/h0gjnr4OnRshWzpI1/Sw6LaqwUmJIZdPXEVij1rI0Odd7VB+i29jLEpFbUki7jnBOxG1lgSv8bloeKizug4JYuUhMifZ4q7Wr+hlWttBxlOMewMzHwOPSg3LJxsMJfJ1jcTczpkVAbJXIoxnUVqXqlsNyc20Bfad20A5Oe0qZ7nI=";
        } else {
            // Green Steve Data
            textureValue = "ewogICJ0aW1lc3RhbXAiIDogMTU5MTQ2MzQ2MTEzMSwKICAicHJvZmlsZUlkIiA6ICI3MzgyZGRmYmU0ODU0NTVjODI1ZjkwMGY4OGZkMzJmOCIsCiAgInByb2ZpbGVOYW1lIiA6ICJ4cWwiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzUyN2QwZDE5NGE4NDNlZmE2NTkyNDI2NmE5ODAzM2RiYjM0MDYzNjAzMGExN2EyM2JiNTljNzNjNjkyNGVmOCIKICAgIH0KICB9Cn0=";
            textureSignature = "EcK2+2xLJLCAfu8qvTqtpfQieFh/r43xkDm2a/c2ZIVxvutgxUIZcFp+cjBEttPESDIoc9FeFAgwGvdPi8cgearoqQE9FPzQo/aZdSvwJ1U6hPzdUgt0puZDMFxEWpdUl5rFuL/NMmdrAOOFzANlVlxWZKjnc32xCfL/NdHt2RUku9jRb+v1WISO1rmUng8WsUfte7JqCphVrl6YSySI3669NS0bbqMsSQMCYbzopV4wanYPnN3S3mtOt76GioysME//+TJlER9xuSTuV2+vfxL8L8UbcHiRavOLU6Wd1hdTFaJX2c6NHatffOXZtxIyxkVycyTFDsjV/ssx3jQFUqgPk+z2zvio5UlnWSQtAV2t8+E87uTJEestoslEVniGLfHGF22o4kH1f66dXUG94qMZk7xZkHQqDfK/IC1CNh4w/JV8yKsqRGEmg7f8dNXsMSEcqvsJKXg971loTfO32EDDvHXzjSKIktFnNv6WVH44VcoNQDB8tzetviBxXihQKXdW6GC9bmFoXjqGPrizIYtOL4QhsYqDNSY0wHMsi5JpxNBZXH58fQ7W7RPQg4RupuUw2oxmuRjYQGSX7kpHFY39ZCgTgGWmtg3Ddo94mSNsNvdQXo00eN/YyhRLswO0DlF0NayeJebh2kG7BLwez+O/9JzufjPOziuukZhocSY=";
        }
    }

    public void closeMenu(boolean showPlayer) {

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

        if (showPlayer) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.showPlayer(player);
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
        if (event.getPlayer() == player) {
            event.setCancelled(true);

            Player eventPlayer = event.getPlayer();

            //Reset flight if flight lost
            if (!eventPlayer.isFlying()) {
                eventPlayer.setAllowFlight(true);
                eventPlayer.setFlying(true);
            }

            // Check if player jumped
            if (event.getFrom().getY() < event.getTo().getY()) {
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

        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, SoundCategory.MASTER, 1f, 1f);

        ArmorStand localStand;
        Character character = null;

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
                    break;
                case 1:
                    localStand = charStand2;
                    break;
                case 2:
                    localStand = charStand3;
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

            if (localStand.getPersistentDataContainer().has(namespacedKey, PersistentDataType.STRING)) {
                character = getCharacterData(UUID.fromString(localStand.getPersistentDataContainer().get(namespacedKey, PersistentDataType.STRING)));
                localStand.setCustomName(character.getName());
            } else if (i != 3) {
                localStand.setCustomName(language.getMessage("character-selection.new-character.name"));
            }

            EntityArmorStand entityArmorStand = ((CraftArmorStand) localStand).getHandle();

            PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(entityArmorStand.getId(), entityArmorStand.getDataWatcher(), true);
            connection.sendPacket(metadata);
        }


        // Set pointers for selected option
        switch (selectedStand) {
            case 0:
                localStand = charStand1;
                break;
            case 1:
                localStand = charStand2;
                break;
            case 2:
                localStand = charStand3;
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

        if (localStand.getPersistentDataContainer().has(namespacedKey, PersistentDataType.STRING)) {
            character = getCharacterData(UUID.fromString(localStand.getPersistentDataContainer().get(namespacedKey, PersistentDataType.STRING)));
            localStand.setCustomName(pointerLeft + character.getName() + pointerRight);
        } else if (selectedStand != 3) {
            character = null;
            localStand.setCustomName(pointerLeft + language.getMessage("character-selection.new-character.name") + pointerRight);
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
                if (character != null) {
                    HashMap<String, String> placeholdersLore = new HashMap<>();
                    placeholdersLore.put("{birthday}", character.getBirthday());
                    placeholdersLore.put("{nationality}", character.getNationality());
                    placeholdersLore.put("{sex}", character.getSex());
                    placeholdersLore.put("{balance}", String.valueOf(character.getVaultBalance()));
                    placeholdersLore.put("{group}", character.getVaultGroup());

                    info = language.getMultiLineMessageCustom("character-selection.character.lore", placeholdersLore);
                } else {
                    info = language.getMultiLineMessage("character-selection.new-character.lore");
                }

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
        PropertyMap pm = profile.getProperties();

        Property property = pm.get("textures").iterator().next();
        pm.remove("textures", property);

        if (character != null) {
            pm.put("textures", new Property("textures", character.getSkinTexture(), character.getSkinSignature()));
        } else {
            pm.put("textures", new Property("textures", textureValue, textureSignature));
        }

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
