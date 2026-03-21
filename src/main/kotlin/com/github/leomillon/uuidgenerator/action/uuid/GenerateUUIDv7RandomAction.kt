package com.github.leomillon.uuidgenerator.action.uuid

import com.github.leomillon.uuidgenerator.UUIDGenerator
import com.github.leomillon.uuidgenerator.action.GenerateRandomAction

/**
 * A random UUIDv7 generator action.
 *
 * @author Jan-Hendrik Diederich
 */
class GenerateUUIDv7RandomAction : GenerateRandomAction() {
    override fun generateId(): String = UUIDGenerator.generateUUIDv7()
}
