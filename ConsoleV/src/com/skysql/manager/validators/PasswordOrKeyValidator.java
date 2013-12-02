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

import com.vaadin.data.Validator;
import com.vaadin.ui.PasswordField;

public class PasswordOrKeyValidator implements Validator {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	private PasswordField passwordField;

	public PasswordOrKeyValidator(PasswordField passwordField) {
		super();
		this.passwordField = passwordField;
	}

	public boolean isValid(Object value) {
		if (value == null || !(value instanceof String)) {
			return false;
		} else {
			boolean isKey = !String.valueOf(value).isEmpty();
			boolean isPassword = !String.valueOf(passwordField.getValue()).isEmpty();
			return (isKey && !isPassword) || (!isKey && isPassword);
		}
	}

	// Upon failure, the validate() method throws an exception
	public void validate(Object value) throws InvalidValueException {
		if (!isValid(value)) {
			throw new InvalidValueException("Either Root Password or SSH Key must be provided.");
		}
	}
}
