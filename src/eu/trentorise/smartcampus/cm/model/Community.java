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

import eu.trentorise.smartcampus.storage.BasicObject;

public class Community extends BasicObject {

	private static final long serialVersionUID = 2223942724440001844L;
	private String name;
	private String description;
	private String[] tags;
	private long socialId;

	
	public Community() {
		super();
	}

	public Community(String id, String name) {
		super();
		this.name = name;
		setId(id);
	}

	public long getSocialId() {
		return socialId;
	}

	public void setSocialId(long socialId) {
		this.socialId = socialId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String[] getTags() {
		return tags;
	}

	public void setTags(String[] tags) {
		this.tags = tags;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
