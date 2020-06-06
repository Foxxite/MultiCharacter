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
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.UUID;

public class WorldSpaceMenu {

    private final MultiCharacter plugin;
    private final Player player;
    private final MinecraftServer server;
    private final CraftPlayer cPlayer;
    private final WorldServer world;
    private final GameProfile profile;
    private final PlayerInteractManager interactManager;
    private final Location lastArmorStandPos;
    private final PlayerConnection connection;
    private final SQLHandler sqlHandler;
    private final Language language;
    private final FileConfiguration config;
    private EntityPlayer fakeEntityPlayer;
    private ArmorStand charStand1;
    private ArmorStand charStand2;
    private ArmorStand charStand3;
    private ArmorStand charInfoStand;
    private ArmorStand staffModeStand;

    public WorldSpaceMenu(MultiCharacter plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        sqlHandler = plugin.getSqlHandler();
        language = plugin.getLanguage();
        config = plugin.getConfiguration();

        connection = ((CraftPlayer) player).getHandle().playerConnection;

        lastArmorStandPos = player.getLocation().clone();

        server = MinecraftServer.getServer();
        cPlayer = (CraftPlayer) player;
        world = ((CraftWorld) player.getWorld()).getHandle();
        profile = new GameProfile(UUID.randomUUID(), "");
        interactManager = new PlayerInteractManager(world);

        spawnNPC();

        spawnArmorStands();

    }


    void spawnNPC() {

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

    void spawnArmorStands() {

        ArmorStand localArmorStand;

        for (int i = 0; i < 5; i++) {

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
                    lastArmorStandPos.add(-2, 1.25, -3);
                    break;
                case 1:
                    charStand2 = localArmorStand;
                    lastArmorStandPos.subtract(0, 0.5, 0);
                    break;
                case 2:
                    charStand3 = localArmorStand;
                    lastArmorStandPos.subtract(0, 0.5, 0);
                    break;
                case 3:
                    staffModeStand = localArmorStand;
                    localArmorStand.setCustomName(language.getMessage("character-selection.staff-mode.name"));
                    lastArmorStandPos.subtract(0, 0.5, 0);
                    break;
                case 4:
                    charInfoStand = localArmorStand;
                    localArmorStand.setCustomName("Char Info");
                    lastArmorStandPos.add(2, 1.8, -0.75);
                    break;
            }

            localArmorStand.teleport(lastArmorStandPos);

            EntityArmorStand entityArmorStand = ((CraftArmorStand) localArmorStand).getHandle();

            PacketPlayOutSpawnEntity packetPlayOutSpawnEntity = new PacketPlayOutSpawnEntity(entityArmorStand);
            PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(entityArmorStand.getId(), entityArmorStand.getDataWatcher(), true);

            connection.sendPacket(packetPlayOutSpawnEntity);
            connection.sendPacket(metadata);
        }


        HashMap<String, String> tableLayout = new HashMap<>();
        tableLayout.put("UUID", "string");
        tableLayout.put("OwnerUUID", "string");

        String selectQuery = "SELECT UUID, OwnerUUID FROM Characters WHERE Deleted = 0 AND OwnerUUID = '" + player.getUniqueId().toString() + "'";

        HashMap<Integer, HashMap<String, Object>> queryResult = sqlHandler.executeQuery(selectQuery, tableLayout);

        //Overwrite new character with excising characters
        if (queryResult != null && queryResult.size() > 0) {

            for (int i = 0; i < queryResult.size(); i++) {

                HashMap<String, Object> row = queryResult.get(i);

                System.out.println(row);

                Character character = getCharacterData(UUID.fromString((String) queryResult.get(i).get("UUID")));

                ArmorStand localStand;

                switch (i) {
                    case 0:
                        localStand = charStand1;
                        charStand1.setCustomName(character.getName());
                        break;
                    case 1:
                        localStand = charStand2;
                        charStand2.setCustomName(character.getName());
                        break;
                    case 2:
                        localStand = charStand3;
                        charStand3.setCustomName(character.getName());
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

}
