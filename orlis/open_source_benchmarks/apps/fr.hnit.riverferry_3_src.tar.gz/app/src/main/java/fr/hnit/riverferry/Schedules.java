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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.HashMap;

public class Schedules extends HashMap<String, DepartureTimeList> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1531102114201278916L;
	
	public ArrayList<String> rights = new ArrayList<String>();
	public ArrayList<String> lefts = new ArrayList<String>();
	
	public void add(DepartureTimeList hl) {
		this.put(hl.from, hl);
		if (hl.bank.equals(Bank.LEFT)) {
			lefts.add(hl.from);
		} else {
			rights.add(hl.from);
		}
	}
	
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("RIGHTS: \n");
		for (String right : rights) {
			buffer.append(" "+this.get(right)+"\n");
		}
		buffer.append("\n");
		buffer.append("LEFTS: \n");
		for (String left : lefts) {
			buffer.append(" "+this.get(left)+"\n");
		}
		
		return buffer.toString();
	}

	
	public static void write(File binFile, Serializable p) throws IOException {
		FileOutputStream fos = new FileOutputStream(binFile);
		ObjectOutputStream oos= new ObjectOutputStream(fos);
		try {
			oos.writeObject(p); 
			oos.flush();
		} finally {
			try {
				oos.close();
			} finally {
				fos.close();
			}
		}
	}

	public static Schedules read(File binFile) throws StreamCorruptedException, IOException, ClassNotFoundException {
		Schedules schedules = null;
		FileInputStream fis = new FileInputStream(binFile);
		ObjectInputStream ois= new ObjectInputStream(fis);
		try {	
			Object p = ois.readObject(); 
			if (p instanceof Schedules)
				schedules = (Schedules)p;
			
		} finally {
			try {
				ois.close();
			} finally {
				fis.close();
			}
		}
		return schedules;
	}
}
