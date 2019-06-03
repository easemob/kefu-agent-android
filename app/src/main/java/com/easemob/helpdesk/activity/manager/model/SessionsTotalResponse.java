package com.easemob.helpdesk.activity.manager.model;

import java.util.List;

/**
 * Created by liyuzhao on 03/04/2018.
 */

public class SessionsTotalResponse {
	/**
	 * status : OK
	 * entities : [{"cnt_csc":2,"se_1":0,"cnt_sc":2,"se_0":0,"key":"total"}]
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
		 * cnt_csc : 2.0
		 * se_1 : 0
		 * cnt_sc : 2.0
		 * se_0 : 0
		 * key : total
		 */

		private double cnt_csc;
		private double se_1;
		private double cnt_sc;
		private double se_0;
		private String key;

		public double getCnt_csc() {
			return cnt_csc;
		}

		public void setCnt_csc(double cnt_csc) {
			this.cnt_csc = cnt_csc;
		}

		public double getSe_1() {
			return se_1;
		}

		public void setSe_1(double se_1) {
			this.se_1 = se_1;
		}

		public double getCnt_sc() {
			return cnt_sc;
		}

		public void setCnt_sc(double cnt_sc) {
			this.cnt_sc = cnt_sc;
		}

		public double getSe_0() {
			return se_0;
		}

		public void setSe_0(double se_0) {
			this.se_0 = se_0;
		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}
	}
}
