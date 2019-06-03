package com.easemob.helpdesk.activity.manager.model;

import java.util.List;

/**
 * Created by liyuzhao on 03/04/2018.
 */

public class ResponseTimeResponse {
	/**
	 * status : OK
	 * entities : [{"name":"mb","index":1,"avg_rt":7,"key":"46894"}]
	 * totalElements : 1
	 */

	private String status;
	private int totalElements;
	private List<EntitiesBean> entities;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getTotalElements() {
		return totalElements;
	}

	public void setTotalElements(int totalElements) {
		this.totalElements = totalElements;
	}

	public List<EntitiesBean> getEntities() {
		return entities;
	}

	public void setEntities(List<EntitiesBean> entities) {
		this.entities = entities;
	}

	public static class EntitiesBean {
		/**
		 * name : mb
		 * index : 1
		 * avg_rt : 7
		 * key : 46894
		 */

		private String name;
		private int index;
		private double avg_rt;
		private String key;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getIndex() {
			return index;
		}

		public void setIndex(int index) {
			this.index = index;
		}

		public double getAvg_rt() {
			return avg_rt;
		}

		public void setAvg_rt(double avg_rt) {
			this.avg_rt = avg_rt;
		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}
	}
}
