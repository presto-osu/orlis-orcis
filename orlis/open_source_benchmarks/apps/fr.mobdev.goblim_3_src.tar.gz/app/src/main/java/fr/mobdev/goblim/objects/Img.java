/*
 * Copyright (C) 2015  Anthony Chomienne, anthony@mob-dev.fr
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package fr.mobdev.goblim.objects;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;

public class Img {

    private Long id;
    private String url;
    private Calendar date;
    private int storageDuration;
    private byte[] thumb;
    private Bitmap image;
    private String shortHash;
    private String realShortHash;
    private String token;
    private int val = 0;

    public Img(long id, String url, String shortHash, String realShortHash, Calendar date, int storageDuration, byte[] thumbData, String token) {
        this.url = url;
        this.shortHash = shortHash;
        this.id = id;
        this.realShortHash = realShortHash;
        this.date = date;
        this.storageDuration = storageDuration;
        this.token = token;
        thumb = thumbData;
        if(thumb != null) {
            image = BitmapFactory.decodeByteArray(thumb, 0, thumb.length);
        }
    }

    public Long getId()
    {
        return id;
    }

    public String getUrl()
    {
        return url;
    }

    public Calendar getDate()
    {
        return date;
    }

    public Bitmap getThumb()
    {
        return image;
    }

    public String getShortHash()
    {
        return shortHash;
    }

    public String getRealShortHash()
    {
        return realShortHash;
    }

    public int getStorageDuration()
    {
        return storageDuration;
    }

    public byte[] getThumbData()
    {
        return thumb;
    }

    public void setThumbData(byte[] thumbData){
        thumb = thumbData;
    }

    public String getToken(){
        return token;
    }


}
