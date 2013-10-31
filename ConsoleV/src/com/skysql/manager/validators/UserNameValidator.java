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