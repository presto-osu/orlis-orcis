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


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DepartureTimeList extends HashMap<Integer, List<DepartureTime>> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5872170171460904029L;
	String from;
	Bank bank;
	
	public DepartureTimeList(String from, Bank rive) {
		this.from = from;
		this.bank = rive;
		for (int i = 1 ; i <= 8 ; i++) // days from monday to friday + holiday
			this.put(i, new ArrayList<DepartureTime>());
	}
	
	public String toString() {
		return from+"\t"+bank+"\t"+super.toString();
	}

	public void addAll(int day, String[] asList) {
		if (asList == null ) {
			//TODO: NextBac.show("HoraireList.addAll(day, list): error null list");
			return;
		}
		DepartureTime[] listToAdd = new DepartureTime[asList.length];
		for (int i = 0 ; i < asList.length ; i++) {
			String str = asList[i];
			listToAdd[i] = new DepartureTime(str);
		}
		this.put(day, Arrays.asList(listToAdd));
	}
	
	public void addAll(String[] asList) {
		if (asList == null ) {
			//TODO: NextBac.show("HoraireList.addAll(list): error null list");
			return;
		}
		for (int day = 1 ; day <=7 ; day++) {
			addAll(day, asList);
		}
	}
}