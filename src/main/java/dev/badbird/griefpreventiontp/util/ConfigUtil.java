package dev.badbird.griefpreventiontp.util;

import net.badbird5907.blib.objects.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConfigUtil {
    public static List<Pair<String, Integer>> parseGroups(Map<String, Object> map) {
        List<Pair<String, Integer>> list = new ArrayList<>();
        for (Map.Entry<String, Object> stringObjectEntry : map.entrySet()) {
            String k = stringObjectEntry.getKey();
            Object v = stringObjectEntry.getValue();
            int i = -1;
            if (v instanceof String) {
                i = Integer.parseInt((String) v);
            } else if (v instanceof Integer) {
                i = (Integer) v;
            }
            list.add(new Pair<>(k, i));
        }
        return list;
    }
}
