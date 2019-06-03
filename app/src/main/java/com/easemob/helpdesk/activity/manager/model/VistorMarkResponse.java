package com.easemob.helpdesk.activity.manager.model;

import java.util.List;

/**
 * Created by liyuzhao on 03/04/2018.
 */

public class VistorMarkResponse {
	/**
	 * status : OK
	 * entities : [{"avg_vm":5,"name":"小猫","index":1,"key":"6795757e-eb6d-408a-874d-0fd3317f490f"}]
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
		 * avg_vm : 5.0
		 * name : 小猫
		 * index : 1
		 * key : 6795757e-eb6d-408a-874d-0fd3317f490f
		 */

		private double avg_vm;
		private String name;
		private int index;
		private String key;

		public double getAvg_vm() {
			return avg_vm;
		}

		public void setAvg_vm(double avg_vm) {
			this.avg_vm = avg_vm;
		}

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

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}
	}
}
