package com.github.leomillon.uuidgenerator.popup.uuid.examiner

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.util.xmlb.XmlSerializerUtil
import org.jetbrains.annotations.Nullable

@State(name = "UUIDExaminerPopupSettings", storages = [(Storage("uuid_examiner_popup.xml"))])
class UUIDExaminerPopupSettings : PersistentStateComponent<UUIDExaminerPopupSettings> {

    companion object {
        val instance: UUIDExaminerPopupSettings
            get() = service()
    }

    var inputText: String? = null
    var separatingChars: String = ","
    var trimWhitespace: Boolean = true
    var hasHeader: Boolean = true
    var hasHeaderSeparator: Boolean = false
    var summarizeCheck: Boolean = true

    @Nullable
    override fun getState() = this

    override fun loadState(state: UUIDExaminerPopupSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }
}
