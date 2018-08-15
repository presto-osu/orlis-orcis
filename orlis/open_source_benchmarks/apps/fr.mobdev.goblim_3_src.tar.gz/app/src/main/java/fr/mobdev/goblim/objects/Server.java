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

public class Server {

    private long id;
    private String url;
    private boolean defaultServer;

    public Server(long id, String url, boolean defaultServer)
    {
        this.id = id;
        this.url = url;
        this.defaultServer = defaultServer;
    }

    public String getUrl()
    {
        return url;
    }

    public long getId()
    {
        return id;
    }

    public boolean isDefaultServer()
    {
        return defaultServer;
    }

}
