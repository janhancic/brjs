/**
 * @module br/presenter/validator/ValidationResultListener
 */

var Errors = require('br/Errors');

/**
 * @class
 * @interface
 * @alias module:br/presenter/validator/ValidationResultListener
 * 
 * @classdesc
 * A ValidationResultListener is notified when a validator completes.
 */
function ValidationResultListener() {
}

/**
 * Callback to notify this class of a completed validation result.
 * 
 * @param {module:br/presenter/validator/ValidationResult} oValidationResult the result for the validation.  Will not be null.
 */
ValidationResultListener.prototype.onValidationResultReceived = function(oValidationResult) {
	throw new Errors.UnimplementedInterfaceError("ValidationResultListener.onValidationResultReceived() has not been implemented.");
};

br.presenter.validator.ValidationResultListener = ValidationResultListener;
