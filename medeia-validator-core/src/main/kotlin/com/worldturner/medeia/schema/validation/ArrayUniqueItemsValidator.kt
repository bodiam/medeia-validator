package com.worldturner.medeia.schema.validation

import com.worldturner.medeia.parser.JsonTokenData
import com.worldturner.medeia.parser.JsonTokenLocation
import com.worldturner.medeia.parser.JsonTokenType.END_ARRAY
import com.worldturner.medeia.parser.JsonTokenType.START_ARRAY
import com.worldturner.medeia.parser.NodeData
import com.worldturner.medeia.parser.SimpleTreeBuilder
import com.worldturner.medeia.schema.model.UniqueItemsValidationMethod
import com.worldturner.medeia.schema.validation.stream.SchemaValidatorInstance

class ArrayUniqueItemsValidator : SchemaValidator {
    override fun createInstance(startLevel: Int): SchemaValidatorInstance =
        ArrayUniqueItemsValidatorInstance(startLevel)

    companion object {
        fun create(
            uniqueItems: Boolean?,
            method: UniqueItemsValidationMethod
        ): SchemaValidator? =
            if (uniqueItems == true) {
                if (method.digest)
                    ArrayUniqueItemsDigestValidator(digestAlgorithm = method.algorithm)
                else
                    ArrayUniqueItemsValidator()
            } else {
                null
            }
    }
}

class ArrayUniqueItemsValidatorInstance(val startLevel: Int) :
    SchemaValidatorInstance {
    private val uniqueItems: MutableSet<NodeData> = mutableSetOf()
    private val treeBuilder: SimpleTreeBuilder = SimpleTreeBuilder(startLevel + 1)

    override fun validate(token: JsonTokenData, location: JsonTokenLocation): ValidationResult? {
        if (location.level == startLevel && token.type != START_ARRAY) {
            return OkValidationResult
        }
        if (location.level > startLevel) {
            treeBuilder.consume(token, location)
            treeBuilder.takeResult()?.let { tree ->
                if (tree in uniqueItems) {
                    return FailedValidationResult(
                        location = location,
                        failedRule = "uniqueItems",
                        message = "Duplicate item"
                    )
                } else {
                    uniqueItems.add(tree)
                }
            }
        }
        if (token.type == END_ARRAY && location.level == startLevel)
            return OkValidationResult
        else
            return null
    }
}
