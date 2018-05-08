package omtteam.openmodularturrets.api.lists;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import omtteam.omlib.util.EntityUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Keridos on 16/11/17.
 * This Class
 */
public class MobBlacklist {
    private static final List<String> list = new ArrayList<>();

    public static boolean contains(String mobIn) {
        for (String mob : list) {
            if (mob.equalsIgnoreCase(mobIn)) {
                return true;
            }
        }
        return false;
    }

    public static boolean contains(EntityLivingBase mobIn) {
        return contains(EntityList.getEntityString(mobIn));
    }

    public static void add(String mobIn) {
        if (contains(mobIn) || EntityUtil.findClassById(mobIn) == null) {
        } else {
            list.add(mobIn);
        }
    }

    public static boolean remove(String mobIn) {
        for (String mob : list) {
            if (mob.equalsIgnoreCase(mobIn)) {
                list.remove(mobIn);
                return true;
            }
        }
        return false;
    }
}