/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the LGNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  LGNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.utilities.comparators;

import com.djrapitops.plan.data.store.objects.DateHolder;

import java.util.Comparator;

/**
 * Compares DateHolder objects so that most recent is first.
 *
 * @author Rsl1122
 */
public class DateHolderRecentComparator implements Comparator<DateHolder> {

    @Override
    public int compare(DateHolder one, DateHolder two) {
        return Long.compare(two.getDate(), one.getDate());
    }
}
