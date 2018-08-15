/*
 * LibraryApi.java is a part of DailybRead
 *     Copyright (C) 2015  John Krause, Eleven-K Software
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package tk.elevenk.olapi;

import org.apache.http.cookie.Cookie;
import org.json.JSONObject;

import java.util.Map;

/**
 * Interface for the Library API
 *
 * Created by John Krause on 12/25/14.
 */
public interface LibraryApi {
    /**
     * Executes a Book API call 
     * @param data Map of query values
     * @return JSON data from API
     */
    public JSONObject books(Map data);

    /**
     * Executes a Covers API call 
     * @param data Map of the query values
     * @return JSON data from API
     */
    public JSONObject covers(Map data);

    /**
     * Executes a Lists API call 
     * @param data Map of query values
     * @return JSON data from api
     */
    public JSONObject lists(Map data);

    /**
     * Executes a Read API call 
     * @param data Map of query values
     * @return JSON data from API
     */
    public JSONObject read(Map data);

    /**
     * Executes a Recent Changes API call 
     * @param data Map of query values
     * @return JSON data from API
     */
    public JSONObject recentChanges(Map data);

    /**
     * Executes a Search API call 
     * @param data Map of query values
     * @return JSON data from server
     */
    public JSONObject search(Map data);

    /**
     * Executes a SearchInside API call 
     * @param data Map of query values
     * @return JSON data from API
     */
    public JSONObject searchInside(Map data);

    /**
     * Executes a Subject API call 
     * @param data Map of query values
     * @return JSON data from API
     */
    public JSONObject subjects(Map data);

    /**
     * Executes a Login API call 
     * @param data Map of query values
     * @return Cookie containing session data
     */
    public Cookie login(Map data);

    /**
     * Cancels any HTTP requests the API is making 
     */
    public void cancelRequest();

    /**
     * Gets the base URL of the API
     * @return Base URL as a String
     */
    public String getBaseUrl();
}
