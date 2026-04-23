package com.github.leomillon.uuidgenerator.debugger

import com.github.f4b6a3.uuid.util.UuidUtil
import com.github.leomillon.uuidgenerator.annotator.uuid.formatInstant
import com.github.leomillon.uuidgenerator.parser.UUID_WITHOUT_DASH_LENGTH
import com.github.leomillon.uuidgenerator.popup.uuid.examiner.items.examineString
import com.intellij.debugger.engine.evaluation.EvaluationContext
import com.intellij.debugger.ui.impl.watch.FieldDescriptorImpl
import com.intellij.debugger.ui.tree.DebuggerTreeNode
import com.intellij.debugger.ui.tree.NodeDescriptor
import com.intellij.debugger.ui.tree.ValueDescriptor
import com.intellij.debugger.ui.tree.render.ChildrenBuilder
import com.intellij.debugger.ui.tree.render.DescriptorLabelListener
import com.intellij.debugger.ui.tree.render.NodeRendererImpl
import com.intellij.openapi.project.Project
import com.sun.jdi.Field
import com.sun.jdi.LongValue
import com.sun.jdi.ObjectReference
import com.sun.jdi.StringReference
import com.sun.jdi.Value
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.reflect.jvm.jvmName

class UuidNodeRenderer : NodeRendererImpl() {
    init {
        isEnabled = true
        setIsApplicableChecker { type ->
            CompletableFuture.completedFuture(
                type?.name() in setOf(
                    String::class.jvmName, UUID::class.jvmName
                )
            )
        }
    }

    override fun getUniqueId(): String = "UUIDNodeRenderer"

    override fun getName(): String {
        return getUniqueId()
    }

    // hasOverhead means: Needs time (for string parsing), evaluate lazy, time delayed.
    override fun hasOverhead(): Boolean {
        return true
    }

    override fun calcLabel(
        descriptor: ValueDescriptor, evaluationContext: EvaluationContext, listener: DescriptorLabelListener
    ): String {
        val value = descriptor.value

        // Handle java.lang.String
        if (value is StringReference) {
            val str = value.value().trim()
            if (str.length < UUID_WITHOUT_DASH_LENGTH) {
                return str
            }
            val examined = examineString(str)
            if (examined != null) {
                val timestamp = if (examined.timestamp != null) {
                    formatInstant(examined.timestamp)
                } else {
                    ""
                }
                return "\"$str\" → UUIDv${examined.version} $timestamp"
            }
            return str
        }

        // Handle java.util.UUID
        if ((value is ObjectReference) && (value.referenceType().name() == UUID::class.jvmName)) {
            val refType = value.referenceType()
            val mostSigBits = (value.getValue(refType.fieldByName("mostSigBits")) as? LongValue)?.value()
            val leastSigBits = (value.getValue(refType.fieldByName("leastSigBits")) as? LongValue)?.value()
            if (mostSigBits != null && leastSigBits != null) {
                val uuidValue = UUID(mostSigBits, leastSigBits)
                val version = uuidValue.version()
                val timestamp = if (version == 1 || version == 7) {
                    val instant = UuidUtil.getInstant(uuidValue)
                    formatInstant(instant)
                } else {
                    ""
                }
                return "$uuidValue → UUIDv$version $timestamp"
            }
            return "{UUID: unreadable bits}"
        }

        return descriptor.value?.toString() ?: "null"
    }

    override fun buildChildren(value: Value, builder: ChildrenBuilder, evaluationContext: EvaluationContext) {
        val obj = value as? ObjectReference ?: return

        // Re-Insert mostSigBits and leastSigBits nodes for fields of UUID classes.
        if (obj.referenceType().name() == UUID::class.jvmName) {
            val refType = obj.referenceType()
            val mostField = refType.fieldByName("mostSigBits")
            val leastField = refType.fieldByName("leastSigBits")

            val nodeManager = builder.nodeManager
            val children = mutableListOf<DebuggerTreeNode>()
            if (mostField != null) {
                val fieldDescriptor = HexFieldDescriptorImpl(evaluationContext.project, obj, mostField)
                children.add(nodeManager.createNode(fieldDescriptor, evaluationContext))
            }
            if (leastField != null) {
                val fieldDescriptor = HexFieldDescriptorImpl(evaluationContext.project, obj, leastField)
                children.add(nodeManager.createNode(fieldDescriptor, evaluationContext))
            }
            builder.setChildren(children)
        }
    }

    override fun isExpandableAsync(
        value: Value, context: EvaluationContext, parentDescriptor: NodeDescriptor
    ): CompletableFuture<Boolean> {
        val isUuidClass = (value is ObjectReference) && (value.referenceType().name() == UUID::class.jvmName)
        return CompletableFuture.completedFuture(isUuidClass)
    }
}

private class HexFieldDescriptorImpl(
    project: Project, objRef: ObjectReference, field: Field
) : FieldDescriptorImpl(project, objRef, field) {
    override fun setValueLabel(label: String) {
        val longVal = (value as? LongValue)?.value()
        if (longVal != null) {
            super.setValueLabel("0x${java.lang.Long.toHexString(longVal)}")
        } else {
            super.setValueLabel(label)
        }
    }
}