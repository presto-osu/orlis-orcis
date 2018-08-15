//This file is part of RiverFerry.
//
//RiverFerry is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//RiverFerry is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with RiverFerry.  If not, see <http://www.gnu.org/licenses/>.
//
//Author: Matthieu Decorde 
//Contact: mdecorde.riverferry@gmail.com

package fr.hnit.riverferry;
import java.io.Serializable;


/**
 * Departure Time
 * 
 * @author mdecorde
 *
 */
public class DepartureTime implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3846302829257086450L;
	
	String str;
	int time=0;
	int h=0,m=0;
	/**
	 * Initialize a DepartureTime with a String.
	 * String format is hh:mm
	 * 
	 * @param str
	 */
	public DepartureTime(String str) {
		this.str = str;
		String[] split = str.split(":", 2);
		//if (split == null || split.length != 2) split = str.split("h", 2);
		if (split == null) {
			//TODO: NextBac.show("error creating horaire "+str);
			return;
		}
		try {
			this.h = (60 * Integer.parseInt(split[0]));
			this.m = Integer.parseInt(split[1]);
		} catch(Exception e) {
			//TODO: NextBac.show("Integer error: '"+str+"'");
		}
		this.time = h + m;
	}
		
	public int compareTo(DepartureTime h) {
		return this.time - h.time;
	}
	
	public int hashcode() {
		return time;
	}
	
	public String toString() {
		return str;
	}
}
