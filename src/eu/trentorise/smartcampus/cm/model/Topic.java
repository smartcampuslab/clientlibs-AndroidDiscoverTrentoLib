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

import java.util.ArrayList;
import java.util.List;

import eu.trentorise.smartcampus.android.common.tagging.SemanticSuggestion;
import eu.trentorise.smartcampus.android.common.tagging.SemanticSuggestion.TYPE;
import eu.trentorise.smartcampus.storage.BasicObject;

public class Topic extends BasicObject implements SocialContainer {
	private static final long serialVersionUID = -6406159100675282522L;

	private long socialId;
	private String name;
	private int status;
	private List<Concept> concepts;
	private List<String> keywords;
	private List<MinimalProfile> users;
	private List<Group> groups;
	private List<Community> communities;
	private List<String> contentTypes;
	private List<Concept> entities;

	private boolean allKnownUsers;
	private boolean allKnownCommunities;
	private boolean allUsers;
	private boolean allCommunities;

	public Topic() {
		super();
	}

	public Topic(String name, List<String> keywords, List<Community> communities) {
		super();
		this.name = name;
		this.keywords = keywords;
		this.communities = communities;
	}



	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Concept> getConcepts() {
		return concepts;
	}

	public void setConcepts(List<Concept> concepts) {
		this.concepts = concepts;
	}

	/* (non-Javadoc)
	 * @see eu.trentorise.smartcampus.cm.model.SocialContainer#getUsers()
	 */
	@Override
	public List<MinimalProfile> getUsers() {
		return users;
	}

	/* (non-Javadoc)
	 * @see eu.trentorise.smartcampus.cm.model.SocialContainer#setUsers(java.util.List)
	 */
	@Override
	public void setUsers(List<MinimalProfile> users) {
		this.users = users;
	}

	/* (non-Javadoc)
	 * @see eu.trentorise.smartcampus.cm.model.SocialContainer#getGroups()
	 */
	@Override
	public List<Group> getGroups() {
		return groups;
	}

	/* (non-Javadoc)
	 * @see eu.trentorise.smartcampus.cm.model.SocialContainer#setGroups(java.util.List)
	 */
	@Override
	public void setGroups(List<Group> groups) {
		this.groups = groups;
	}

	/* (non-Javadoc)
	 * @see eu.trentorise.smartcampus.cm.model.SocialContainer#getCommunities()
	 */
	@Override
	public List<Community> getCommunities() {
		return communities;
	}

	/* (non-Javadoc)
	 * @see eu.trentorise.smartcampus.cm.model.SocialContainer#setCommunities(java.util.List)
	 */
	@Override
	public void setCommunities(List<Community> communities) {
		this.communities = communities;
	}

	public List<String> getContentTypes() {
		return contentTypes;
	}

	public void setContentTypes(List<String> contentTypes) {
		this.contentTypes = contentTypes;
	}

	public List<String> getKeywords() {
		return keywords;
	}

	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
	}

	public long getSocialId() {
		return socialId;
	}

	public void setSocialId(long socialId) {
		this.socialId = socialId;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String description() {
		String res = "";
		if (getKeywords() != null) {
			for (int i = 0; i < getKeywords().size(); i++) {
				if (res.length() > 0) res += ", ";
				res += getKeywords().get(i);
			}
		}
		if (getConcepts() != null) {
			for (Concept c : getConcepts()) {
				if (res.length() > 0) res += ", ";
				res += c.getName();
			}
		}
		if (getEntities() != null) {
			for (Concept c : getEntities()) {
				if (res.length() > 0) res += ", ";
				res += c.getName();
			}
		}
		return res;
	}
	
	public String sources() {
		String res = "";
		if (isAllUsers()) {
			res += "Public";
		}
		if (getCommunities() != null) {
			for (Community c : getCommunities()) {
				if (res.length() > 0) res += ", ";
				res += c.getName();
			}
		}
		if (getGroups() != null) {
			for (Group c : getGroups()) {
				if (res.length() > 0) res += ", ";
				res += c.getName();
			}
		}
		if (getUsers() != null) {
			for (MinimalProfile c : getUsers()) {
				if (res.length() > 0) res += ", ";
				res += c.getName() + " " + c.getSurname();
			}
		}

		return res;
	}

	public ArrayList<SemanticSuggestion> tags() {
		if (getConcepts() == null && getKeywords() == null && getEntities() == null) return new ArrayList<SemanticSuggestion>();
		ArrayList<SemanticSuggestion> list = new ArrayList<SemanticSuggestion>();
		if (getConcepts() != null) {
			for (Concept c : getConcepts()) {
				SemanticSuggestion ss = new SemanticSuggestion();
				ss.setId(c.getId());
				ss.setType(TYPE.SEMANTIC);
				ss.setName(c.getName());
				ss.setDescription(c.getDescription());
				ss.setSummary(c.getSummary());
				list.add(ss);
			}
		}
		if (getKeywords() != null) {
			for (String keyword : getKeywords()) {
				SemanticSuggestion ss = new SemanticSuggestion();
				ss.setType(TYPE.KEYWORD);
				ss.setName(keyword);
				list.add(ss);
			}
		}
		if (getEntities() != null) {
			for (Concept c : getEntities()) {
				SemanticSuggestion ss = new SemanticSuggestion();
				ss.setId(c.getId());
				ss.setType(TYPE.ENTITY);
				ss.setName(c.getName());
				ss.setDescription(c.getDescription());
				ss.setSummary(c.getSummary());
				list.add(ss);
			}
		}
		return list;
	}

	public boolean isAllUsers() {
		return allUsers;
	}

	public void setAllUsers(boolean allUsers) {
		this.allUsers = allUsers;
	}

	public List<Concept> getEntities() {
		return entities;
	}

	public void setEntities(List<Concept> entities) {
		this.entities = entities;
	}

	public boolean isAllKnownUsers() {
		return allKnownUsers;
	}

	public void setAllKnownUsers(boolean allKnownUsers) {
		this.allKnownUsers = allKnownUsers;
	}

	public boolean isAllKnownCommunities() {
		return allKnownCommunities;
	}

	public void setAllKnownCommunities(boolean allKnownCommunities) {
		this.allKnownCommunities = allKnownCommunities;
	}

	public boolean isAllCommunities() {
		return allCommunities;
	}

	public void setAllCommunities(boolean allCommunities) {
		this.allCommunities = allCommunities;
	}
	
}
