package com.easemob.helpdesk.activity.manager.model;

import java.util.List;
import java.util.Map;

/**
 * Created by liyuzhao on 03/04/2018.
 */

public class WaitCountResponse {
	/**
	 * status : OK
	 * entities : [{"max":0,"value":[{"1489463520000":0},{"1489463580000":0},{"1489463640000":0},{"1489463700000":0},{"1489463760000":0},{"1489463820000":0},{"1489463880000":0},{"1489463940000":0},{"1489464000000":0},{"1489464060000":0},{"1489464120000":0},{"1489464180000":0},{"1489464240000":0},{"1489464300000":0},{"1489464360000":0},{"1489464420000":0},{"1489464480000":0},{"1489464540000":0},{"1489464600000":0},{"1489464660000":0},{"1489464720000":0},{"1489464780000":0},{"1489464840000":0},{"1489464900000":0},{"1489464960000":0},{"1489465020000":0},{"1489465080000":0},{"1489465140000":0},{"1489465200000":0},{"1489465260000":0}],"key":"wait"}]
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
		 * max : 0
		 * value : [{"1489463520000":0},{"1489463580000":0},{"1489463640000":0},{"1489463700000":0},{"1489463760000":0},{"1489463820000":0},{"1489463880000":0},{"1489463940000":0},{"1489464000000":0},{"1489464060000":0},{"1489464120000":0},{"1489464180000":0},{"1489464240000":0},{"1489464300000":0},{"1489464360000":0},{"1489464420000":0},{"1489464480000":0},{"1489464540000":0},{"1489464600000":0},{"1489464660000":0},{"1489464720000":0},{"1489464780000":0},{"1489464840000":0},{"1489464900000":0},{"1489464960000":0},{"1489465020000":0},{"1489465080000":0},{"1489465140000":0},{"1489465200000":0},{"1489465260000":0}]
		 * key : wait
		 */

		private int max;
		private String key;
		private List<Map<String, Integer>> value;

		public int getMax() {
			return max;
		}

		public void setMax(int max) {
			this.max = max;
		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public List<Map<String, Integer>> getValue() {
			return value;
		}

		public void setValue(List<Map<String, Integer>> value) {
			this.value = value;
		}

	}

}
