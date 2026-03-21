package com.github.leomillon.uuidgenerator.action.uuid

import com.github.leomillon.uuidgenerator.UUIDGenerator
import com.github.leomillon.uuidgenerator.action.GenerateRandomAction

/**
 * A random UUIDv4 generator action.
 *
 * @author Léo Millon
 */
class GenerateUUIDv4RandomAction : GenerateRandomAction() {
    override fun generateId(): String = UUIDGenerator.generateUUIDv4()
}
