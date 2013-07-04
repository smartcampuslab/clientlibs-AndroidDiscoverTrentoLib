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

import java.util.List;

public interface SocialContainer {

	public boolean isAllUsers();
	public void setAllUsers(boolean allUsers);
	
	public  List<MinimalProfile> getUsers();

	public  void setUsers(List<MinimalProfile> users);

	public  List<Group> getGroups();

	public  void setGroups(List<Group> groups);

	public  List<Community> getCommunities();

	public  void setCommunities(List<Community> communities);

	public boolean isAllKnownUsers();
	public void setAllKnownUsers(boolean allKnownUsers);

	public boolean isAllKnownCommunities();
	public void setAllKnownCommunities(boolean allKnownCommunities);

	public boolean isAllCommunities();
	public void setAllCommunities(boolean allCommunities);
}
