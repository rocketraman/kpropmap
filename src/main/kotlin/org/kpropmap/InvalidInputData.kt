package org.kpropmap

enum class InvalidInputData(val error: String, val template: String) {
  FIELD_REQUIRED("field_required", "Field(s) [%s] required for this operation."),
  FIELD_NOT_ALLOWED("field_not_allowed", "Field(s) [%s] not allowed for this operation."),
  FIELD_CONTENT_INVALID("field_content_invalid", "Field [%s] is invalid: %s."),
  FIELD_TYPE_INVALID("field_type_invalid", "Field [%s] has an invalid data type, expected %s but received %s."),
  TYPE_INVALID("type_invalid", "Invalid data type, expected %s but received %s.")
}
