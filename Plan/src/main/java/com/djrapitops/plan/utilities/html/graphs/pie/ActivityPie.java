/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.utilities.html.graphs.pie;

import com.djrapitops.plan.data.store.mutators.ActivityIndex;

import java.util.*;

/**
 * Pie about different Activity Groups defined by ActivityIndex.
 *
 * @author Rsl1122
 * @see ActivityIndex
 * @since 4.2.0
 */
public class ActivityPie extends Pie {

    ActivityPie(Map<String, Set<UUID>> activityData, String[] colors) {
        super(turnToSlices(activityData, colors));
    }

    private static List<PieSlice> turnToSlices(Map<String, Set<UUID>> activityData, String[] colors) {
        int maxCol = colors.length;

        List<PieSlice> slices = new ArrayList<>();
        int i = 0;
        for (String group : ActivityIndex.getGroups()) {
            Set<UUID> players = activityData.getOrDefault(group, new HashSet<>());
            int num = players.size();

            slices.add(new PieSlice(group, num, colors[i % maxCol], false));
            i++;
        }

        return slices;
    }
}
