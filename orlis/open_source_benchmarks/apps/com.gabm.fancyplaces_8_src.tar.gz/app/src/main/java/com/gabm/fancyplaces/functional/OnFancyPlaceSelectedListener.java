/*
 * Copyright (C) 2015 Matthias Gabriel
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

package com.gabm.fancyplaces.functional;

/**
 * Created by gabm on 15/05/15.
 */


public interface OnFancyPlaceSelectedListener {
    int INTENT_VIEW = 0;
    int INTENT_EDIT = 1;
    int INTENT_DELETE = 2;
    int INTENT_SHARE = 3;
    int INTENT_CREATE_NEW = 4;
    int INTENT_EXPORT_TO_GPX = 5;

    void onFancyPlaceSelected(int id, int intent);
}
