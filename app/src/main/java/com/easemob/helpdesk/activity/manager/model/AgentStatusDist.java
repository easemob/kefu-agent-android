package com.easemob.helpdesk.activity.manager.model;

import java.util.List;

/**
 * Created by liyuzhao on 03/04/2018.
 */

public class AgentStatusDist {
	/**
	 * status : OK
	 * entities : [{"offline":4,"hidden":0,"leave":0,"busy":0,"online":2}]
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
		 * offline : 4
		 * hidden : 0
		 * leave : 0
		 * busy : 0
		 * online : 2
		 */

		private int offline;
		private int hidden;
		private int leave;
		private int busy;
		private int online;

		public int getOffline() {
			return offline;
		}

		public void setOffline(int offline) {
			this.offline = offline;
		}

		public int getHidden() {
			return hidden;
		}

		public void setHidden(int hidden) {
			this.hidden = hidden;
		}

		public int getLeave() {
			return leave;
		}

		public void setLeave(int leave) {
			this.leave = leave;
		}

		public int getBusy() {
			return busy;
		}

		public void setBusy(int busy) {
			this.busy = busy;
		}

		public int getOnline() {
			return online;
		}

		public void setOnline(int online) {
			this.online = online;
		}
	}

}
