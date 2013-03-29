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
package eu.trentorise.smartcampus.dt.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StoryObject extends BaseDTObject {

	private static final long serialVersionUID = -5123355788738143639L;
	private List<StepObject> steps = new ArrayList<StepObject>();
	private List<String> attending = new ArrayList<String>();
	private Integer attendees = 0;
	
	
	public StoryObject() {
		super();
	}
	public StoryObject(List<StepObject> steps, List<String> partecipating,Integer participants, String description, String title, String source, Long entityId, String creatorId, String creatorName, int com_averageRating, String notes, String type, boolean typeUserDefined) {
		super();
		super.setDescription(description);
		super.setTitle(title);
		super.setSource(source);
		super.setEntityId(entityId);
		super.setCreatorId(creatorId);
		super.setCreatorName(creatorName);
		CommunityData com = new CommunityData();
		com.setAverageRating(com_averageRating);
		com.setNotes(notes);
		com.setRatings(Arrays.asList(new Rating("a", 5),new Rating("a", 3)));
		super.setCommunityData(com);
		super.setType(type);
		super.setTypeUserDefined(typeUserDefined);

		
		
		this.steps=new ArrayList<StepObject>(steps);
		this.attending=partecipating;
		this.attendees=participants;
	}
	public List<StepObject> getSteps() {
		return steps;
	}


	public void setSteps(List<StepObject> steps) {
		this.steps = steps;
	}

	public List<String> getAttending() {
		return attending;
	}

	public void setAttending(List<String> attending) {
		this.attending = attending;
	}

	public Integer getAttendees() {
		return attendees;
	}

	public void setAttendees(Integer attendees) {
		this.attendees = attendees;
	}
	
}
