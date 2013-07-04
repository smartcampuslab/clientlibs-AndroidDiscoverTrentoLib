/*******************************************************************************
 * Copyright 2012-2013 Trento RISE
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either   express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package eu.trentorise.smartcampus.cm.model;

/**
 * 
 * Minimal information for a user visualization
 * 
 * @author Mirko Perillo
 * 
 */
public class MinimalProfile extends SCUser {

	private static final long serialVersionUID = -5207522961747636696L;
	private String name;
	private String surname;
	private UserInformation userInformation;
	private String pictureUrl;
	private boolean known;

	public MinimalProfile() {
		super();
	}

	public MinimalProfile(String name, String surname, String pictureUrl, boolean known) {
		super();
		this.name = name;
		this.surname = surname;
		this.pictureUrl = pictureUrl;
		this.known = known;
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public UserInformation getUserInformation() {
		return userInformation;
	}

	public void setUserInformation(UserInformation userInformation) {
		this.userInformation = userInformation;
	}

	public String getPictureUrl() {
		return pictureUrl;
	}

	public void setPictureUrl(String pictureUrl) {
		this.pictureUrl = pictureUrl;
	}

	public boolean isKnown() {
		return known;
	}

	public void setKnown(boolean known) {
		this.known = known;
	}

	public String fullName() {
		return name + " "+surname;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Long.valueOf(getSocialId()).hashCode();
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
		MinimalProfile other = (MinimalProfile) obj;
		if (getSocialId() <= 0) {
			if (other.getSocialId() > 0) return false;
		} else if (getSocialId() != other.getSocialId())
			return false;
		return true;
	}

	
}
