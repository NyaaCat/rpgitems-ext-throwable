package cat.nyaa.rpgitems.throwable.utils;

import cat.nyaa.nyaacore.Pair;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.udojava.evalex.Expression;
import com.udojava.evalex.LazyFunction;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.scoreboard.Objective;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import think.rpgitems.RPGItems;
import think.rpgitems.power.Getter;
import think.rpgitems.power.Power;
import think.rpgitems.power.PropertyHolder;
import think.rpgitems.power.Serializer;
import think.rpgitems.power.marker.Selector;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Utils {

    private static LoadingCache<String, List<String>> permissionCache = CacheBuilder
                                                                                .newBuilder()
                                                                                .concurrencyLevel(1)
                                                                                .maximumSize(1000)
                                                                                .build(CacheLoader.from(Utils::parsePermission));

    private static List<String> parsePermission(String str) {
        return Arrays.asList(str.split(";"));
    }

    public static List<Entity> getNearbyEntities(Power power, Location l, Player player, double radius, double dx, double dy, double dz) {
        List<Entity> entities = new ArrayList<>();
        Collection<Entity> nearbyEntities = l.getWorld().getNearbyEntities(l, dx, dy, dz);
        if (!nearbyEntities.isEmpty()) {
            for (Entity e : nearbyEntities) {
                if (!Utils.isUtilArmorStand(e) && l.distance(e.getLocation()) <= radius) {
                    entities.add(e);
                }
            }
        }
        power.getItem().getMarkers().stream().filter(pow -> pow instanceof Selector).forEach(
                selector -> {
                    if (power.getSelectors().contains(((Selector) selector).id())) {
                        ((Selector) selector).inPlaceFilter(player, entities);
                    }
                }
        );
        return entities;
    }

    /**
     * Get nearby entities.
     *
     * @param power  power
     * @param l      location
     * @param player player
     * @param radius radius
     * @return nearby entities
     */
    public static List<Entity> getNearbyEntities(Power power, Location l, Player player, double radius) {
        return getNearbyEntities(power, l, player, radius, radius, radius, radius);
    }

    /**
     * Get nearby living entities ordered by distance.
     *
     * @param power  power
     * @param l      location
     * @param player player
     * @param radius radius
     * @param min    min radius
     * @return nearby living entities ordered by distance
     */
    public static List<LivingEntity> getNearestLivingEntities(Power power, Location l, Player player, double radius, double min) {
        final List<Map.Entry<LivingEntity, Double>> entities = new ArrayList<>();
        for (Entity e : getNearbyEntities(power, l, player, radius)) {
            if (e instanceof LivingEntity && !player.equals(e) && !Utils.isUtilArmorStand(e)) {
                double d = l.distance(e.getLocation());
                if (d <= radius && d >= min) {
                    entities.add(new AbstractMap.SimpleImmutableEntry<>((LivingEntity) e, d));
                }
            }
        }
        List<LivingEntity> entity = new ArrayList<>();
        entities.sort(Comparator.comparing(Map.Entry::getValue));
        entities.forEach((k) -> entity.add(k.getKey()));
        return entity;
    }

    /**
     * Gets entities in cone.
     *
     * @param entities  List of nearby entities
     * @param startPos  starting position
     * @param degrees   angle of cone
     * @param direction direction of the cone
     * @return All entities inside the cone
     */
    public static List<LivingEntity> getLivingEntitiesInCone(List<LivingEntity> entities, Vector startPos, double degrees, Vector direction) {
        List<LivingEntity> newEntities = new LinkedList<>();
        float relativeAngle = 0;
        float minAngle = 180;
        for (LivingEntity e : entities) {
            if (Utils.isUtilArmorStand(e)) continue;
            Vector relativePosition = e.getEyeLocation().toVector();
            relativePosition.subtract(startPos);
            relativeAngle = getAngleBetweenVectors(direction, relativePosition);
            if (relativeAngle > degrees) continue;
            if (relativeAngle < minAngle) {
                minAngle = relativeAngle;
                newEntities.add(0, e);
            } else {
                newEntities.add(e);
            }
        }
        return newEntities;
    }

    public static List<LivingEntity> getLivingEntitiesInConeSorted(List<LivingEntity> entities, Vector startPos, double degrees, Vector direction) {
        Set<AngledEntity> newEntities = new TreeSet<>();
        float relativeAngle = 0;
        for (LivingEntity e : entities) {
            if (isUtilArmorStand(e))continue;
            Vector relativePosition = e.getEyeLocation().toVector();
            relativePosition.subtract(startPos);
            relativeAngle = getAngleBetweenVectors(direction, relativePosition);
            AngledEntity angledEntity = new AngledEntity(relativeAngle, e);
            if (relativeAngle > degrees) continue;
            newEntities.add(angledEntity);
        }
        return newEntities.stream().map(AngledEntity::getEntity).collect(Collectors.toList());
    }

    private static class AngledEntity implements Comparable<AngledEntity>{
        double angle;
        LivingEntity entity;

        public AngledEntity(double angle, LivingEntity entity){
            this.angle = angle;
            this.entity = entity;
        }

        public LivingEntity getEntity() {
            return entity;
        }

        public double getAngle() {
            return angle;
        }

        @Override
        public int compareTo(AngledEntity o) {
            return Double.compare(angle, o.angle);
        }
    }


    /**
     * Gets angle between vectors.
     *
     * @param v1 the v 1
     * @param v2 the v 2
     * @return the angle between vectors
     */
    public static float getAngleBetweenVectors(Vector v1, Vector v2) {
        return Math.abs((float) Math.toDegrees(v1.angle(v2)));
    }

    public static void attachPermission(Player player, String permissions) {
        if (permissions.length() != 0 && !permissions.equals("*")) {
            List<String> permissionList = permissionCache.getUnchecked(permissions);
            for (String permission : permissionList) {
                if (player.hasPermission(permission)) {
                    return;
                }
                PermissionAttachment attachment = player.addAttachment(RPGItems.plugin, 1);
                String[] perms = permission.split("\\.");
                StringBuilder p = new StringBuilder();
                for (String perm : perms) {
                    p.append(perm);
                    attachment.setPermission(p.toString(), true);
                    p.append('.');
                }
            }
        }
    }

    // TODO
    @SuppressWarnings("unchecked")
    public static void saveProperty(PropertyHolder p, ConfigurationSection section, String property, Field field) throws IllegalAccessException {
        Serializer getter = field.getAnnotation(Serializer.class);
        field.setAccessible(true);
        Object val = field.get(p);
        if (val == null) return;
        if (getter != null) {
            section.set(property, Getter.from(p, getter.value()).get(val));
        } else {
            if (Collection.class.isAssignableFrom(field.getType())) {
                Collection c = (Collection) val;
                if (c.isEmpty()) return;
                if (Set.class.isAssignableFrom(field.getType())) {
                    section.set(property, c.stream().map(Object::toString).sorted().collect(Collectors.joining(",")));
                } else {
                    section.set(property, c.stream().map(Object::toString).collect(Collectors.joining(",")));
                }
            } else if (field.getType() == Enchantment.class) {
                section.set(property, ((Enchantment) val).getKey().toString());
            } else {
                val = field.getType().isEnum() ? ((Enum<?>) val).name() : val;
                section.set(property, val);
            }
        }
    }

    public static String getProperty(PropertyHolder p, String property, Field field) {
        YamlConfiguration configuration = new YamlConfiguration();
        try {
            saveProperty(p, configuration, property, field);
        } catch (IllegalAccessException e) {
            RPGItems.plugin.getLogger().log(Level.WARNING, "Error getting property " + property + " from " + field + " in " + p, e);
            return null;
        }
        return configuration.getString(property);
    }

    public static Vector distance(BoundingBox bb, Vector vec) {
        double x = Math.max(0, Math.max(bb.getMinX() - vec.getX(), vec.getX() - bb.getMaxX()));
        double y = Math.max(0, Math.max(bb.getMinY() - vec.getY(), vec.getY() - bb.getMaxY()));
        double z = Math.max(0, Math.max(bb.getMinZ() - vec.getZ(), vec.getZ() - bb.getMaxZ()));
        return new Vector(x, y, z);
    }

    public static Vector hitPoint(BoundingBox bb, Vector hitNormal) {
        if (hitNormal.getX() > 0) {
            return new Vector(bb.getMinX(), bb.getCenterY(), bb.getCenterZ());
        }
        if (hitNormal.getX() < 0) {
            return new Vector(bb.getMaxX(), bb.getCenterY(), bb.getCenterZ());
        }
        if (hitNormal.getY() > 0) {
            return new Vector(bb.getCenterX(), bb.getMinY(), bb.getCenterZ());
        }
        if (hitNormal.getY() < 0) {
            return new Vector(bb.getCenterX(), bb.getMaxY(), bb.getCenterZ());
        }
        if (hitNormal.getZ() > 0) {
            return new Vector(bb.getCenterX(), bb.getCenterY(), bb.getMinZ());
        }
        if (hitNormal.getZ() < 0) {
            return new Vector(bb.getCenterX(), bb.getCenterY(), bb.getMaxZ());
        }
        throw new IllegalArgumentException("hitNormal: " + hitNormal.toString());
    }

    // Sweep a in the direction of v against b, returns non null & info if there was a hit
    // ===================================================================
    public static Pair<Vector, Vector> sweep(BoundingBox a, BoundingBox b, Vector vel) {
        double outTime = 1.0;
        // Return early if a & b are already overlapping
        if (a.overlaps(b)) return Pair.of(new Vector(), null);

        // Treat b as stationary, so invert v to get relative velocity
        Vector v = vel.clone().multiply(-1);

        double hitTime = 0.0;
        Vector overlapTime = new Vector();

        // X axis overlap
        if (v.getX() < 0) {
            if (b.getMax().getX() < a.getMin().getX()) return null;
            if (b.getMax().getX() > a.getMin().getX())
                outTime = Math.min((a.getMin().getX() - b.getMax().getX()) / v.getX(), outTime);

            if (a.getMax().getX() < b.getMin().getX()) {
                overlapTime.setX((a.getMax().getX() - b.getMin().getX()) / v.getX());
                hitTime = Math.max(overlapTime.getX(), hitTime);
            }
        } else if (v.getX() > 0) {
            if (b.getMin().getX() > a.getMax().getX()) return null;
            if (a.getMax().getX() > b.getMin().getX())
                outTime = Math.min((a.getMax().getX() - b.getMin().getX()) / v.getX(), outTime);

            if (b.getMax().getX() < a.getMin().getX()) {
                overlapTime.setX((a.getMin().getX() - b.getMax().getX()) / v.getX());
                hitTime = Math.max(overlapTime.getX(), hitTime);
            }
        }

        if (hitTime > outTime) return null;

        //=================================

        // Y axis overlap
        if (v.getY() < 0) {
            if (b.getMax().getY() < a.getMin().getY()) return null;
            if (b.getMax().getY() > a.getMin().getY())
                outTime = Math.min((a.getMin().getY() - b.getMax().getY()) / v.getY(), outTime);

            if (a.getMax().getY() < b.getMin().getY()) {
                overlapTime.setY((a.getMax().getY() - b.getMin().getY()) / v.getY());
                hitTime = Math.max(overlapTime.getY(), hitTime);
            }
        } else if (v.getY() > 0) {
            if (b.getMin().getY() > a.getMax().getY()) return null;
            if (a.getMax().getY() > b.getMin().getY())
                outTime = Math.min((a.getMax().getY() - b.getMin().getY()) / v.getY(), outTime);

            if (b.getMax().getY() < a.getMin().getY()) {
                overlapTime.setY((a.getMin().getY() - b.getMax().getY()) / v.getY());
                hitTime = Math.max(overlapTime.getY(), hitTime);
            }
        }

        if (hitTime > outTime) return null;

        //=================================

        // Z axis overlap
        if (v.getZ() < 0) {
            if (b.getMax().getZ() < a.getMin().getZ()) return null;
            if (b.getMax().getZ() > a.getMin().getZ())
                outTime = Math.min((a.getMin().getZ() - b.getMax().getZ()) / v.getZ(), outTime);

            if (a.getMax().getZ() < b.getMin().getZ()) {
                overlapTime.setZ((a.getMax().getZ() - b.getMin().getZ()) / v.getZ());
                hitTime = Math.max(overlapTime.getZ(), hitTime);
            }
        } else if (v.getZ() > 0) {
            if (b.getMin().getZ() > a.getMax().getZ()) return null;
            if (a.getMax().getZ() > b.getMin().getZ())
                outTime = Math.min((a.getMax().getZ() - b.getMin().getZ()) / v.getZ(), outTime);

            if (b.getMax().getZ() < a.getMin().getZ()) {
                overlapTime.setZ((a.getMin().getZ() - b.getMax().getZ()) / v.getZ());
                hitTime = Math.max(overlapTime.getZ(), hitTime);
            }
        }

        if (hitTime > outTime) return null;
        Vector hitNormal;
        // Scale resulting velocity by normalized hit time
        Vector outVel = vel.clone().multiply(hitTime);

        // Hit normal is along axis with the highest overlap time
        if (overlapTime.getX() > overlapTime.getY()) {
            if (overlapTime.getZ() > overlapTime.getX()) {
                hitNormal = new Vector(0, 0, v.getZ());
            } else {
                hitNormal = new Vector(v.getX(), 0, 0);
            }
        } else {
            if (overlapTime.getZ() > overlapTime.getY()) {
                hitNormal = new Vector(0, 0, v.getZ());
            } else {
                hitNormal = new Vector(0, v.getY(), 0);
            }
        }

        return Pair.of(outVel, hitNormal);
    }

    private static final Pattern VALID_KEY = Pattern.compile("[a-z0-9/._-]+");

    public static byte[] decodeUUID(UUID complex) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(complex.getMostSignificantBits());
        bb.putLong(complex.getLeastSignificantBits());
        return bb.array();
    }

    public static UUID encodeUUID(byte[] primitive) {
        ByteBuffer bb = ByteBuffer.wrap(primitive);
        long firstLong = bb.getLong();
        long secondLong = bb.getLong();
        return new UUID(firstLong, secondLong);
    }

    public static void rethrow(Exception e) {
        if (e instanceof RuntimeException) {
            throw (RuntimeException) e;
        } else {
            throw new RuntimeException(e);
        }
    }

    public static Float maxWithCancel(Float a, Float b) {
        if (a == null) {
            return b;
        }
        if (a == -1 || b == -1) return -1.0f;
        return Math.max(a, b);
    }

    public static Double maxWithCancel(Double a, Double b) {
        if (a == null) {
            return b;
        }
        if (a == -1 || b == -1) return -1.0;
        return Math.max(a, b);
    }

    public static Double minWithCancel(Double a, Double b) {
        if (a == null) {
            return b;
        }
        if (a == -1 || b == -1) return -1.0;
        return Math.min(a, b);
    }

    public static Expression.LazyNumber lazyNumber(Supplier<Double> f) {
        return new Expression.LazyNumber() {
            @Override
            public BigDecimal eval() {
                return BigDecimal.valueOf(f.get());
            }

            @Override
            public String getString() {
                return null;
            }
        };
    }

    public static LazyFunction scoreBoard(Player player) {
        return new LazyFunction() {
            @Override
            public String getName() {
                return "playerScoreBoard";
            }

            @Override
            public int getNumParams() {
                return 2;
            }

            @Override
            public boolean numParamsVaries() {
                return false;
            }

            @Override
            public boolean isBooleanFunction() {
                return false;
            }

            @Override
            public Expression.LazyNumber lazyEval(List<Expression.LazyNumber> lazyParams) {
                Objective objective = player.getScoreboard().getObjective(lazyParams.get(0).getString());
                if (objective == null) {
                    return lazyParams.get(1);
                }
                return lazyNumber(() -> (double) objective.getScore(player.getName()).getScore());
            }
        };
    }

    public static LazyFunction now() {
        return new LazyFunction() {
            @Override
            public String getName() {
                return "now";
            }

            @Override
            public int getNumParams() {
                return 0;
            }

            @Override
            public boolean numParamsVaries() {
                return false;
            }

            @Override
            public boolean isBooleanFunction() {
                return false;
            }

            @Override
            public Expression.LazyNumber lazyEval(List<Expression.LazyNumber> lazyParams) {
                return new Expression.LazyNumber() {
                    @Override
                    public BigDecimal eval() {
                        return BigDecimal.valueOf(System.currentTimeMillis());
                    }

                    @Override
                    public String getString() {
                        return null;
                    }
                };
            }
        };
    }

    public static <T extends Weightable> T weightedRandomPick(Collection<T> collection) {
        int sum = collection.stream().mapToInt(Weightable::getWeight)
                .sum();
        if (sum == 0) {
            return collection.stream().findAny().orElse(null);
        }
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int selected = random.nextInt(sum);
        Iterator<Pair<T, Integer>> iterator = collection.stream().map(t -> new Pair<>(t, t.getWeight())).iterator();
        int count = 0;
        while (iterator.hasNext()) {
            Pair<T, Integer> next = iterator.next();
            Integer i = next.getValue();
            int nextCount = count + i;
            if (count <= selected && nextCount > selected) {
                return next.getKey();
            }
            count = nextCount;
        }
        return collection.stream().findAny().orElse(null);
    }

    public static <T> T randomPick(List<T> list) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return list.isEmpty() ? null : list.get(random.nextInt(list.size()));
    }

    public static boolean isUtilArmorStand(Entity livingEntity) {
        if (livingEntity instanceof ArmorStand) {
            ArmorStand arm = (ArmorStand) livingEntity;
            return arm.isMarker() && !arm.isVisible();
        }
        return false;
    }
}
