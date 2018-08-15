package com.luorrak.ouroboros.util;

import java.util.ArrayList;

/**
 * Ouroboros - An 8chan browser
 * Copyright (C) 2015  Luorrak
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

//adapted from Clover/Exodus
public class Reply{
    public String name = "";
    public String email = "";
    public boolean sage = false;
    public String subject = "";
    public String comment = "";
    public String board = "";
    public String resto = "0";
    public ArrayList<String> filePath;
    public ArrayList<String> fileName;
    public String password = "";
    public boolean spoilerImage = false;
    public String captchaText = "";
    public String captchaCookie = "";
}