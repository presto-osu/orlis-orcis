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

public final class TrainingPlanBean implements Parcelable {

	private String name;
	private Integer getReadyTimeInSeconds;

	private TrainingPlanBean(Parcel in) {
		name = in.readString();
		getReadyTimeInSeconds = (Integer) in.readSerializable();
	}

	public TrainingPlanBean() {

	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(name);
		out.writeSerializable(getReadyTimeInSeconds);

	}

	public static final Parcelable.Creator<TrainingPlanBean> CREATOR = new Parcelable.Creator<TrainingPlanBean>() {
		public TrainingPlanBean createFromParcel(Parcel in) {
			return new TrainingPlanBean(in);
		}

		public TrainingPlanBean[] newArray(int size) {
			return new TrainingPlanBean[size];
		}
	};

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getGetReadyTimeInSeconds() {
		return getReadyTimeInSeconds;
	}

	public void setGetReadyTimeInSeconds(Integer getReadyTimeInSeconds) {
		this.getReadyTimeInSeconds = getReadyTimeInSeconds;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getReadyTimeInSeconds == null) ? 0 : getReadyTimeInSeconds.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		TrainingPlanBean other = (TrainingPlanBean) obj;
		if (getReadyTimeInSeconds == null) {
			if (other.getReadyTimeInSeconds != null)
				return false;
		} else if (!getReadyTimeInSeconds.equals(other.getReadyTimeInSeconds))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
