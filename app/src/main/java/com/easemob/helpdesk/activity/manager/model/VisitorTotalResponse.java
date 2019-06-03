package com.easemob.helpdesk.activity.manager.model;

import java.util.List;

/**
 * Created by liyuzhao on 03/04/2018.
 */

public class VisitorTotalResponse {
	/**
	 * status : OK
	 * entities : [{"app":1,"weixin":0,"webim":0,"weibo":0,"phone":0}]
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
		 * app : 1
		 * weixin : 0
		 * webim : 0
		 * weibo : 0
		 * phone : 0
		 */

		private int app;
		private int weixin;
		private int webim;
		private int weibo;
		private int phone;

		public int getApp() {
			return app;
		}

		public void setApp(int app) {
			this.app = app;
		}

		public int getWeixin() {
			return weixin;
		}

		public void setWeixin(int weixin) {
			this.weixin = weixin;
		}

		public int getWebim() {
			return webim;
		}

		public void setWebim(int webim) {
			this.webim = webim;
		}

		public int getWeibo() {
			return weibo;
		}

		public void setWeibo(int weibo) {
			this.weibo = weibo;
		}

		public int getPhone() {
			return phone;
		}

		public void setPhone(int phone) {
			this.phone = phone;
		}
	}
}
