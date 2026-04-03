package com.github.leomillon.uuidgenerator.action.uuid

import com.github.leomillon.uuidgenerator.action.GeneratePopupAction
import com.github.leomillon.uuidgenerator.popup.GeneratePopup
import com.github.leomillon.uuidgenerator.popup.uuid.examiner.ExamineUUIDPopup

/**
 * Open the UUID examiner popup.
 *
 * @author Léo Millon
 */
class ExamineUUIDPopupAction : GeneratePopupAction() {
    override fun createPopup(): GeneratePopup =
        ExamineUUIDPopup()
}
