package com.speehive.speehiveaihub.models

data class ValidationError(
    val row: Int,
    val title: String?,
    val reason: String
)

data class BulkValidationResponse(
    val isValid: Boolean,
    val totalRows: Int,
    val validRows: Int,
    val invalidRows: Int,
    val message: String?,
    val errors: List<ValidationError>?
)

data class BulkImportResponse(
    val success: Boolean?,
    val totalRows: Int?,
    val imported: Int?,
    val message: String?,
    val isValid: Boolean?,
    val validRows: Int?,
    val invalidRows: Int?,
    val errors: List<ValidationError>?
)
