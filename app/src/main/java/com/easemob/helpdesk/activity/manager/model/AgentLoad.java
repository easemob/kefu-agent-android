package com.easemob.helpdesk.activity.manager.model;

/**
 * Created by liyuzhao on 03/04/2018.
 */

public class AgentLoad {


	/**
	 * status : OK
	 * entity : {"processingSessionCount":0,"tenantId":35,"totalMaxServiceSessionCount":50}
	 */

	private String status;
	private EntityBean entity;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public EntityBean getEntity() {
		return entity;
	}

	public void setEntity(EntityBean entity) {
		this.entity = entity;
	}

	public static class EntityBean {
		/**
		 * processingSessionCount : 0
		 * tenantId : 35
		 * totalMaxServiceSessionCount : 50
		 */

		private int processingSessionCount;
		private int tenantId;
		private int totalMaxServiceSessionCount;

		public int getProcessingSessionCount() {
			return processingSessionCount;
		}

		public void setProcessingSessionCount(int processingSessionCount) {
			this.processingSessionCount = processingSessionCount;
		}

		public int getTenantId() {
			return tenantId;
		}

		public void setTenantId(int tenantId) {
			this.tenantId = tenantId;
		}

		public int getTotalMaxServiceSessionCount() {
			return totalMaxServiceSessionCount;
		}

		public void setTotalMaxServiceSessionCount(int totalMaxServiceSessionCount) {
			this.totalMaxServiceSessionCount = totalMaxServiceSessionCount;
		}
	}

}
