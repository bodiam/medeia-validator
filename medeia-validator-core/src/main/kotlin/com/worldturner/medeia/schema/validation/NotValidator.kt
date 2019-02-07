package com.worldturner.medeia.schema.validation

import com.worldturner.medeia.parser.JsonTokenData
import com.worldturner.medeia.parser.JsonTokenLocation
import com.worldturner.medeia.schema.validation.stream.SchemaValidatorInstance

class NotValidator(
    val validator: SchemaValidator
) : SchemaValidator {
    override fun createInstance(startLevel: Int): SchemaValidatorInstance {
        return NotSchemaValidatorInstance(startLevel, validator)
    }

    companion object {
        fun create(validation: SchemaValidator?) =
            if (validation != null) NotValidator(validation) else null
    }
}

class NotSchemaValidatorInstance(
    val startLevel: Int,
    validation: SchemaValidator
) : SchemaValidatorInstance {

    val instance: SchemaValidatorInstance = validation.let { it.createInstance(startLevel) }
    var result: ValidationResult? = null

    override fun validate(token: JsonTokenData, location: JsonTokenLocation): ValidationResult? {
        if (result == null) {
            result = instance.validate(token, location)
        }
        if (location.level == startLevel && token.type.lastToken) {
            return result?.let { result ->
                if (result.valid)
                    FailedValidationResult(
                        location = location,
                        failedRule = "not",
                        message = "Subschema was successful"
                    )
                else
                    OkValidationResult
            } ?: throw NullPointerException("Illegal state")
        } else {
            return null
        }
    }
}