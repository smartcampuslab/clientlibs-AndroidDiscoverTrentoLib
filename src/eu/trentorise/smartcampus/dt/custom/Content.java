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
package eu.trentorise.smartcampus.dt.custom;

import java.util.List;

public class Content {
	private List<String> imagesLinks;
	private String title;
	private List<String> tags;
	private String date;

	public Content(List<String> imagesLinks, String title, List<String> tags, String date) {
		setImagesLinks(imagesLinks);
		setTitle(title);
		setTags(tags);
		setDate(date);
	}

	public List<String> getImagesLinks() {
		return imagesLinks;
	}

	public void setImagesLinks(List<String> imagesLinks) {
		this.imagesLinks = imagesLinks;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}
}
