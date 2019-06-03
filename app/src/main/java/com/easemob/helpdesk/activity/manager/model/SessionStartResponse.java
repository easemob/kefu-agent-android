package com.easemob.helpdesk.activity.manager.model;


import java.util.List;

/**
 * Created by liyuzhao on 03/04/2018.
 */

public class SessionStartResponse {
	/**
	 * status : OK
	 * entities : [{"cnt_ssc":2,"name":"大堂经理","index":1,"key":"42b6e936-df96-42e4-bd19-0342d356008d"},{"cnt_ssc":1,"name":"小猫","index":2,"key":"6795757e-eb6d-408a-874d-0fd3317f490f"}]
	 * totalElements : 2
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
		 * cnt_ssc : 2.0
		 * name : 大堂经理
		 * index : 1
		 * key : 42b6e936-df96-42e4-bd19-0342d356008d
		 */

		private double cnt_ssc;
		private String name;
		private int index;
		private String key;

		public double getCnt_ssc() {
			return cnt_ssc;
		}

		public void setCnt_ssc(double cnt_ssc) {
			this.cnt_ssc = cnt_ssc;
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
