/*
 * This file is distributed as part of the MariaDB Manager.  It is free
 * software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation,
 * version 2.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Copyright 2012-2014 SkySQL Ab
 */

package com.skysql.manager.validators;

import com.skysql.manager.api.UserInfo;
import com.vaadin.data.Validator;

public class UserNameValidator implements Validator {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	private UserInfo userInfo;

	public UserNameValidator(UserInfo userInfo) {
		super();
		this.userInfo = userInfo;
	}

	public boolean isValid(Object value) {
		if (value == null || !(value instanceof String)) {
			return false;
		}
		return (true);
	}

	// Upon failure, the validate() method throws an exception
	public void validate(Object value) throws InvalidValueException {
		if (!isValid(value)) {
			throw new InvalidValueException("Username invalid");
		} else {
			String name = (String) value;
			if (name.contains(" ")) {
				throw new InvalidValueException("Username contains illegal characters");
			} else if (userInfo != null && userInfo.findRecordByID((String) value) != null) {
				throw new InvalidValueException("Username already exists");
			}
		}
	}
}
