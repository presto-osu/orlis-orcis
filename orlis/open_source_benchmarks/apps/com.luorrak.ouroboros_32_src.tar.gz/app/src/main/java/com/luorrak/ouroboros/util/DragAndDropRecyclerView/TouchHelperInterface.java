package com.luorrak.ouroboros.util.DragAndDropRecyclerView;

/**
 * Ouroboros - An 8chan browser
 * Copyright (C) 2015  Luorrak
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public interface TouchHelperInterface {
    void onItemMove(int fromPosition, int toPosition);

    void onItemDismiss(int position);
}
