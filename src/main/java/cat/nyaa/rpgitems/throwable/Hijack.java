package cat.nyaa.rpgitems.throwable;

import cat.nyaa.rpgitems.throwable.lib.wrapper.WrapperPlayServerSpawnEntity;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.comphenix.protocol.PacketType.Play.Client.*;
import static com.comphenix.protocol.PacketType.Play.Server.*;

public final class Hijack {
    public Hijack() {
        protocolManager = ProtocolLibrary.getProtocolManager();
        registPacketListener();
    }

    public static Set<PacketType> hijacked = new HashSet<>();

    public static void hijack(PacketType packetType) {
        hijacked.add(packetType);
    }

    public static void stopHijack(PacketType packetType) {
        hijacked.remove(packetType);
    }

    public static ProtocolManager protocolManager;

    public static Set<Integer> hiddenEntities = new HashSet<>();

    public static Map<Integer, Consumer<PacketEvent>> entitySpawnHandler = new HashMap<>();

    public static Map<Integer, Consumer<PacketEvent>> entityMetadataHandler = new HashMap<>();

    public static Cache<Integer, PacketContainer> entitySpawnCache = CacheBuilder.newBuilder()
            .concurrencyLevel(2)
            .expireAfterWrite(1, TimeUnit.SECONDS)
            .build();

    private PacketAdapter entityPacketAdapter;

    private void registPacketListener() {
        entityPacketAdapter = new PacketAdapter(ThrowableExtensionPlugin.plugin, ListenerPriority.NORMAL, ENTITY_PACKETS) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (isHijacked(SPAWN_ENTITY) && event.getPacketType() == SPAWN_ENTITY) {
                    int entityID = event.getPacket().getIntegers().read(0);
                    if(entitySpawnCache.getIfPresent(entityID) == null){
                        WrapperPlayServerSpawnEntity spawnEntity = new WrapperPlayServerSpawnEntity(event.getPacket());
                        entitySpawnCache.put(entityID, spawnEntity.getHandle().deepClone());
                        event.setCancelled(true);
                        return;
                    }
                }
                try {
                    int entityID = event.getPacket().getIntegers().read(0);
                    if (hiddenEntities.contains(entityID) || entitySpawnCache.getIfPresent(entityID) != null) {
                        event.setCancelled(true);
                    }
                    if (entitySpawnHandler.containsKey(entityID) && event.getPacketType() == SPAWN_ENTITY) {
                        entitySpawnHandler.get(entityID).accept(event);
                    }
                    if (entityMetadataHandler.containsKey(entityID) && event.getPacketType() == ENTITY_METADATA) {
                        entityMetadataHandler.get(entityID).accept(event);
                    }
                }catch (FieldAccessException e){
                    String message = e.getMessage();
                }
            }
        };

        protocolManager.addPacketListener(entityPacketAdapter);
    }

        private static final PacketType[] ENTITY_PACKETS = {
            ENTITY_EQUIPMENT, BED, ANIMATION, NAMED_ENTITY_SPAWN,
            COLLECT, SPAWN_ENTITY, SPAWN_ENTITY_LIVING, SPAWN_ENTITY_PAINTING, SPAWN_ENTITY_EXPERIENCE_ORB,
            ENTITY_VELOCITY, REL_ENTITY_MOVE, ENTITY_LOOK, ENTITY_TELEPORT, ENTITY_HEAD_ROTATION, ENTITY_STATUS,
            ATTACH_ENTITY, ENTITY_METADATA, ENTITY_EFFECT, REMOVE_ENTITY_EFFECT, BLOCK_BREAK_ANIMATION
    };

    private static final PacketType[] PLAYER_ACTION_PACKETS = {
            USE_ENTITY, FLYING, PacketType.Play.Client.POSITION, POSITION_LOOK, BOAT_MOVE,
            PacketType.Play.Client.ABILITIES, BLOCK_DIG, ENTITY_ACTION, STEER_VEHICLE, PacketType.Play.Client.HELD_ITEM_SLOT,
            ARM_ANIMATION, USE_ITEM, BLOCK_PLACE,
    };

    private static final PacketType[] PLAYER_CLICK_PACKETS = {
            BLOCK_DIG, ARM_ANIMATION, BLOCK_PLACE, USE_ITEM, USE_ENTITY
    };

    private static boolean isHijacked(PacketType packetType) {
        return hijacked.contains(packetType);
    }
}
