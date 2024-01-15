package me.petterim1.pets;

import cn.nukkit.level.Level;
import cn.nukkit.level.format.FullChunk;

import java.util.SplittableRandom;

public class Utils {

    private static final SplittableRandom random = new SplittableRandom(System.currentTimeMillis());

    public static int rand(int min, int max) {
        if (min == max) {
            return max;
        }
        return min + random.nextInt(max - min);
    }

    public static double rand(double min, double max) {
        if (min == max) {
            return max;
        }
        return min + Math.random() * (max-min);
    }

    public static boolean rand() {
        return random.nextBoolean();
    }

    public static int getBlockId(Level level, FullChunk chunk, int x, int y, int z) {
        if (y < 0 || y > 255) {
            return -1;
        }
        int cx = x >> 4;
        int cz = z >> 4;
        if (chunk == null || cx != chunk.getX() || cz != chunk.getZ()) {
            chunk = level.getChunk(cx, cz, true);
        }
        return chunk.getBlockId(x & 0x0f, y, z & 0x0f);
    }
}
