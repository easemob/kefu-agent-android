package com.easemob.helpdesk.activity.manager.model;

import java.util.List;

/**
 * Created by liyuzhao on 03/04/2018.
 */

public class QualityTotalResponse {
	/**
	 * status : OK
	 * entities : [{"avg_vm":0,"avg_ar":9,"avg_fr":9}]
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
		 * avg_vm : 0
		 * avg_ar : 9
		 * avg_fr : 9
		 */

		private double avg_vm;
		private double avg_ar;
		private double avg_fr;

		public double getAvg_vm() {
			return avg_vm;
		}

		public void setAvg_vm(double avg_vm) {
			this.avg_vm = avg_vm;
		}

		public double getAvg_ar() {
			return avg_ar;
		}

		public void setAvg_ar(double avg_ar) {
			this.avg_ar = avg_ar;
		}

		public double getAvg_fr() {
			return avg_fr;
		}

		public void setAvg_fr(double avg_fr) {
			this.avg_fr = avg_fr;
		}
	}
}
