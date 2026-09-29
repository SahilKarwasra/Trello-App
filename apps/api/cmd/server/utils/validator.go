package utils

import (
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"reflect"
	"strings"
	"unicode"

	"github.com/gin-gonic/gin/binding"
	"github.com/go-playground/validator/v10"
)

func init() {
	if v, ok := binding.Validator.Engine().(*validator.Validate); ok {
		v.RegisterTagNameFunc(func(fld reflect.StructField) string {
			name := strings.SplitN(fld.Tag.Get("json"), ",", 2)[0]
			if name == "-" {
				return ""
			}
			return name
		})
	}
}

// FormatValidationError converts raw binding and validation errors into clean, human-readable messages.
func FormatValidationError(err error) string {
	if err == nil {
		return ""
	}

	// Handle empty body
	if errors.Is(err, io.EOF) {
		return "request body cannot be empty"
	}

	// Handle JSON unmarshal type error
	var unmarshalTypeError *json.UnmarshalTypeError
	if errors.As(err, &unmarshalTypeError) {
		return fmt.Sprintf("invalid type for field '%s', expected %s", unmarshalTypeError.Field, unmarshalTypeError.Type.String())
	}

	// Handle JSON syntax error or truncated JSON
	var syntaxError *json.SyntaxError
	if errors.As(err, &syntaxError) || errors.Is(err, io.ErrUnexpectedEOF) {
		return "malformed JSON payload"
	}

	// Handle go-playground validation errors
	var valErrors validator.ValidationErrors
	if errors.As(err, &valErrors) {
		var errorMessages []string
		for _, e := range valErrors {
			fieldName := toLowerCamel(e.Field())
			switch e.Tag() {
			case "required":
				errorMessages = append(errorMessages, fmt.Sprintf("%s is required", fieldName))
			case "email":
				errorMessages = append(errorMessages, fmt.Sprintf("%s must be a valid email address", fieldName))
			case "min":
				errorMessages = append(errorMessages, fmt.Sprintf("%s must be at least %s characters long", fieldName, e.Param()))
			case "max":
				errorMessages = append(errorMessages, fmt.Sprintf("%s must not exceed %s characters", fieldName, e.Param()))
			case "len":
				errorMessages = append(errorMessages, fmt.Sprintf("%s must be exactly %s characters long", fieldName, e.Param()))
			case "e164":
				errorMessages = append(errorMessages, fmt.Sprintf("%s must be a valid phone number with country code (e.g. +1234567890)", fieldName))
			case "numeric":
				errorMessages = append(errorMessages, fmt.Sprintf("%s must contain only digits", fieldName))
			case "uuid":
				errorMessages = append(errorMessages, fmt.Sprintf("%s must be a valid UUID", fieldName))
			case "oneof":
				errorMessages = append(errorMessages, fmt.Sprintf("%s must be one of: %s", fieldName, e.Param()))
			default:
				errorMessages = append(errorMessages, fmt.Sprintf("%s failed validation on '%s'", fieldName, e.Tag()))
			}
		}
		if len(errorMessages) > 0 {
			return strings.Join(errorMessages, ", ")
		}
	}

	return err.Error()
}

func toLowerCamel(s string) string {
	if len(s) == 0 {
		return s
	}
	r := []rune(s)
	r[0] = unicode.ToLower(r[0])
	return string(r)
}
