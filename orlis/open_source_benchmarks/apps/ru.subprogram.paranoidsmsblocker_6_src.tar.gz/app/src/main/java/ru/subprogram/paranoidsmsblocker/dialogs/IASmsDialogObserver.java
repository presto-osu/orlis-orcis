package ru.subprogram.paranoidsmsblocker.dialogs;

import ru.subprogram.paranoidsmsblocker.database.entities.CAContact;

public interface IASmsDialogObserver {

	void smsDialogMoveToWhiteListButtonClick(CAContact contact);

}
