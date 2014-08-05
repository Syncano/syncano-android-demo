package com.syncano.android.lib.objects;

import java.io.Serializable;

import com.google.gson.annotations.Expose;

/**
 * Represents Subscription object from Syncano Api
 */
public class Subscription implements Serializable {
	private static final long serialVersionUID = 1264710454087635139L;
	/** type of subscription */
	@Expose
	private String type;
	/** subscription id */
	@Expose
	private Integer id;

	/**
	 * @return subscription type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets subscription type
	 * 
	 * @param type
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return subscription id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * Sets subscription id
	 * 
	 * @param id
	 */
	public void setId(Integer id) {
		this.id = id;
	}

}