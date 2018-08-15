/*
 * 
 * HIIT Timer - A simple timer for high intensity trainings
 Copyright (C) 2015 Lorenzo Chiovini

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package org.hiittimer.beans;

import android.os.Parcel;
import android.os.Parcelable;

public class RoundBean implements Parcelable {

	private Integer number;
	private Integer workInSeconds;
	private Integer restInSeconds;

	private RoundBean(Parcel in) {
		number = in.readInt();
		workInSeconds = in.readInt();
		restInSeconds = in.readInt();
	}

	public RoundBean() {

	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(number);
		out.writeInt(workInSeconds);
		out.writeInt(restInSeconds);
	}

	public static final Parcelable.Creator<RoundBean> CREATOR = new Parcelable.Creator<RoundBean>() {
		public RoundBean createFromParcel(Parcel in) {
			return new RoundBean(in);
		}

		public RoundBean[] newArray(int size) {
			return new RoundBean[size];
		}
	};

	public Integer getNumber() {
		return number;
	}

	public void setNumber(Integer number) {
		this.number = number;
	}

	public Integer getWorkInSeconds() {
		return workInSeconds;
	}

	public void setWorkInSeconds(Integer workInSeconds) {
		this.workInSeconds = workInSeconds;
	}

	public Integer getRestInSeconds() {
		return restInSeconds;
	}

	public void setRestInSeconds(Integer restInSeconds) {
		this.restInSeconds = restInSeconds;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((number == null) ? 0 : number.hashCode());
		result = prime * result + ((restInSeconds == null) ? 0 : restInSeconds.hashCode());
		result = prime * result + ((workInSeconds == null) ? 0 : workInSeconds.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RoundBean other = (RoundBean) obj;
		if (number == null) {
			if (other.number != null)
				return false;
		} else if (!number.equals(other.number))
			return false;
		if (restInSeconds == null) {
			if (other.restInSeconds != null)
				return false;
		} else if (!restInSeconds.equals(other.restInSeconds))
			return false;
		if (workInSeconds == null) {
			if (other.workInSeconds != null)
				return false;
		} else if (!workInSeconds.equals(other.workInSeconds))
			return false;
		return true;
	}

}
