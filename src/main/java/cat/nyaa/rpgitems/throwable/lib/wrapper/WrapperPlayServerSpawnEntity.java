/**
 * PacketWrapper - ProtocolLib wrappers for Minecraft packets
 * Copyright (C) dmulloy2 <http://dmulloy2.net>
 * Copyright (C) Kristian S. Strangeland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cat.nyaa.rpgitems.throwable.lib.wrapper;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.PacketConstructor;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

import java.util.UUID;

public class WrapperPlayServerSpawnEntity extends AbstractPacket {
	public static final PacketType TYPE = PacketType.Play.Server.SPAWN_ENTITY;

	private static PacketConstructor entityConstructor;

	// Cached velocity for when packet fields are not directly accessible
	private Vector cachedVelocity = new Vector(0, 0, 0);

	/**
	 * Represents the different object types.
	 * Note: These are legacy values, modern Minecraft uses EntityType registry.
	 *
	 * @author Kristian
	 */
	public static final class ObjectTypes {
		public static final int BOAT = 1;
		public static final int ITEM_STACK = 2;
		public static final int AREA_EFFECT_CLOUD = 3;
		public static final int MINECART = 10;
		public static final int ACTIVATED_TNT = 50;
		public static final int ENDER_CRYSTAL = 51;
		public static final int TIPPED_ARROW_PROJECTILE = 60;
		public static final int SNOWBALL_PROJECTILE = 61;
		public static final int EGG_PROJECTILE = 62;
		public static final int GHAST_FIREBALL = 63;
		public static final int BLAZE_FIREBALL = 64;
		public static final int THROWN_ENDERPEARL = 65;
		public static final int WITHER_SKULL_PROJECTILE = 66;
		public static final int SHULKER_BULLET = 67;
		public static final int FALLING_BLOCK = 70;
		public static final int ITEM_FRAME = 71;
		public static final int EYE_OF_ENDER = 72;
		public static final int THROWN_POTION = 73;
		public static final int THROWN_EXP_BOTTLE = 75;
		public static final int FIREWORK_ROCKET = 76;
		public static final int LEASH_KNOT = 77;
		public static final int ARMORSTAND = 78;
		public static final int FISHING_FLOAT = 90;
		public static final int SPECTRAL_ARROW = 91;
		public static final int DRAGON_FIREBALL = 93;

		private ObjectTypes() {}
	}

	public WrapperPlayServerSpawnEntity() {
		super(new PacketContainer(TYPE), TYPE);
		handle.getModifier().writeDefaults();
	}

	public WrapperPlayServerSpawnEntity(PacketContainer packet) {
		super(packet, TYPE);
	}

	public WrapperPlayServerSpawnEntity(Entity entity, int type, int objectData) {
		super(fromEntity(entity, type, objectData), TYPE);
	}

	// Useful constructor
	private static PacketContainer fromEntity(Entity entity, int type,
			int objectData) {
		if (entityConstructor == null)
			entityConstructor =
					ProtocolLibrary.getProtocolManager()
							.createPacketConstructor(TYPE, entity, type,
									objectData);
		return entityConstructor.createPacket(entity, type, objectData);
	}

	/**
	 * Retrieve entity ID of the Object.
	 *
	 * @return The current EID
	 */
	public int getEntityID() {
		return handle.getIntegers().read(0);
	}

	/**
	 * Retrieve the entity that will be spawned.
	 *
	 * @param world - the current world of the entity.
	 * @return The spawned entity.
	 */
	public Entity getEntity(World world) {
		return handle.getEntityModifier(world).read(0);
	}

	/**
	 * Retrieve the entity that will be spawned.
	 *
	 * @param event - the packet event.
	 * @return The spawned entity.
	 */
	public Entity getEntity(PacketEvent event) {
		return getEntity(event.getPlayer().getWorld());
	}

	/**
	 * Set entity ID of the Object.
	 *
	 * @param value - new value.
	 */
	public void setEntityID(int value) {
		handle.getIntegers().write(0, value);
	}

	public UUID getUniqueId() {
		return handle.getUUIDs().read(0);
	}

	public void setUniqueId(UUID value) {
		handle.getUUIDs().write(0, value);
	}

	/**
	 * Retrieve the x position of the object.
	 *
	 * @return The current X
	 */
	public double getX() {
		return handle.getDoubles().read(0);
	}

	/**
	 * Set the x position of the object.
	 *
	 * @param value - new value.
	 */
	public void setX(double value) {
		handle.getDoubles().write(0, value);
	}

	/**
	 * Retrieve the y position of the object.
	 *
	 * @return The current y
	 */
	public double getY() {
		return handle.getDoubles().read(1);
	}

	/**
	 * Set the y position of the object.
	 *
	 * @param value - new value.
	 */
	public void setY(double value) {
		handle.getDoubles().write(1, value);
	}

	/**
	 * Retrieve the z position of the object.
	 *
	 * @return The current z
	 */
	public double getZ() {
		return handle.getDoubles().read(2);
	}

	/**
	 * Set the z position of the object.
	 *
	 * @param value - new value.
	 */
	public void setZ(double value) {
		handle.getDoubles().write(2, value);
	}

	/**
	 * Retrieve the optional speed x.
	 *
	 * @return The optional speed x.
	 */
	public double getOptionalSpeedX() {
		try {
			if (handle.getShorts().size() > 0) {
				return handle.getShorts().read(0) / 8000.0D;
			}
		} catch (Exception ignored) {}
		try {
			// Legacy: integers at index 1
			if (handle.getIntegers().size() > 1) {
				return handle.getIntegers().read(1) / 8000.0D;
			}
		} catch (Exception ignored) {}
		return cachedVelocity.getX();
	}

	/**
	 * Set the optional speed x.
	 *
	 * @param value - new value.
	 */
	public void setOptionalSpeedX(double value) {
		cachedVelocity.setX(value);
		try {
			if (handle.getShorts().size() > 0) {
				handle.getShorts().write(0, (short) (value * 8000.0D));
				return;
			}
		} catch (Exception ignored) {}
		try {
			// Legacy: integers at index 1
			if (handle.getIntegers().size() > 1) {
				handle.getIntegers().write(1, (int) (value * 8000.0D));
			}
		} catch (Exception ignored) {}
	}

	/**
	 * Retrieve the optional speed y.
	 *
	 * @return The optional speed y.
	 */
	public double getOptionalSpeedY() {
		try {
			if (handle.getShorts().size() > 1) {
				return handle.getShorts().read(1) / 8000.0D;
			}
		} catch (Exception ignored) {}
		try {
			// Legacy: integers at index 2
			if (handle.getIntegers().size() > 2) {
				return handle.getIntegers().read(2) / 8000.0D;
			}
		} catch (Exception ignored) {}
		return cachedVelocity.getY();
	}

	/**
	 * Set the optional speed y.
	 *
	 * @param value - new value.
	 */
	public void setOptionalSpeedY(double value) {
		cachedVelocity.setY(value);
		try {
			if (handle.getShorts().size() > 1) {
				handle.getShorts().write(1, (short) (value * 8000.0D));
				return;
			}
		} catch (Exception ignored) {}
		try {
			// Legacy: integers at index 2
			if (handle.getIntegers().size() > 2) {
				handle.getIntegers().write(2, (int) (value * 8000.0D));
			}
		} catch (Exception ignored) {}
	}

	/**
	 * Retrieve the optional speed z.
	 *
	 * @return The optional speed z.
	 */
	public double getOptionalSpeedZ() {
		try {
			if (handle.getShorts().size() > 2) {
				return handle.getShorts().read(2) / 8000.0D;
			}
		} catch (Exception ignored) {}
		try {
			// Legacy: integers at index 3
			if (handle.getIntegers().size() > 3) {
				return handle.getIntegers().read(3) / 8000.0D;
			}
		} catch (Exception ignored) {}
		return cachedVelocity.getZ();
	}

	/**
	 * Set the optional speed z.
	 *
	 * @param value - new value.
	 */
	public void setOptionalSpeedZ(double value) {
		cachedVelocity.setZ(value);
		try {
			if (handle.getShorts().size() > 2) {
				handle.getShorts().write(2, (short) (value * 8000.0D));
				return;
			}
		} catch (Exception ignored) {}
		try {
			// Legacy: integers at index 3
			if (handle.getIntegers().size() > 3) {
				handle.getIntegers().write(3, (int) (value * 8000.0D));
			}
		} catch (Exception ignored) {}
	}

	/**
	 * Retrieve the pitch.
	 *
	 * @return The current pitch.
	 */
	public float getPitch() {
		try {
			if (handle.getBytes().size() > 0) {
				return (handle.getBytes().read(0) * 360.F) / 256.0F;
			}
		} catch (Exception ignored) {}
		try {
			// Legacy: integers at index 4
			if (handle.getIntegers().size() > 4) {
				return (handle.getIntegers().read(4) * 360.F) / 256.0F;
			}
		} catch (Exception ignored) {}
		return 0;
	}

	/**
	 * Set the pitch.
	 *
	 * @param value - new pitch.
	 */
	public void setPitch(float value) {
		try {
			if (handle.getBytes().size() > 0) {
				handle.getBytes().write(0, (byte) (value * 256.0F / 360.0F));
				return;
			}
		} catch (Exception ignored) {}
		try {
			// Legacy: integers at index 4
			if (handle.getIntegers().size() > 4) {
				handle.getIntegers().write(4, (int) (value * 256.0F / 360.0F));
			}
		} catch (Exception ignored) {}
	}

	/**
	 * Retrieve the yaw.
	 *
	 * @return The current Yaw
	 */
	public float getYaw() {
		try {
			if (handle.getBytes().size() > 1) {
				return (handle.getBytes().read(1) * 360.F) / 256.0F;
			}
		} catch (Exception ignored) {}
		try {
			// Legacy: integers at index 5
			if (handle.getIntegers().size() > 5) {
				return (handle.getIntegers().read(5) * 360.F) / 256.0F;
			}
		} catch (Exception ignored) {}
		return 0;
	}

	/**
	 * Set the yaw of the object spawned.
	 *
	 * @param value - new yaw.
	 */
	public void setYaw(float value) {
		try {
			if (handle.getBytes().size() > 1) {
				handle.getBytes().write(1, (byte) (value * 256.0F / 360.0F));
				return;
			}
		} catch (Exception ignored) {}
		try {
			// Legacy: integers at index 5
			if (handle.getIntegers().size() > 5) {
				handle.getIntegers().write(5, (int) (value * 256.0F / 360.0F));
			}
		} catch (Exception ignored) {}
	}

	/**
	 * Retrieve the type of object.
	 *
	 * @return The current EntityType
	 */
	public EntityType getType() {
		try {
			return handle.getEntityTypeModifier().read(0);
		} catch (Exception ignored) {}
		return EntityType.ITEM;
	}

	/**
	 * Set the type of object using Bukkit EntityType.
	 *
	 * @param type - entity type name (e.g., "ITEM", "ARROW")
	 */
	public void setType(String type) {
		try {
			EntityType entityType = EntityType.valueOf(type);
			handle.getEntityTypeModifier().write(0, entityType);
		} catch (Exception ignored) {}
	}

	/**
	 * Retrieve object data.
	 *
	 * @return The current object Data
	 */
	public int getObjectData() {
		try {
			if (handle.getIntegers().size() > 1) {
				return handle.getIntegers().read(1);
			}
		} catch (Exception ignored) {}
		return 0;
	}

	/**
	 * Set object Data.
	 *
	 * @param value - new object data.
	 */
	public void setObjectData(int value) {
		try {
			if (handle.getIntegers().size() > 1) {
				handle.getIntegers().write(1, value);
			}
		} catch (Exception ignored) {}
	}
}
