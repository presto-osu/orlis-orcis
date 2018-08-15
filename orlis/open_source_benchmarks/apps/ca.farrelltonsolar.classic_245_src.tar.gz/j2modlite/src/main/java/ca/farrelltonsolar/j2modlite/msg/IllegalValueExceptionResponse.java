/**
 * 
 */
package ca.farrelltonsolar.j2modlite.msg;

import ca.farrelltonsolar.j2modlite.Modbus;

/**
 * @author Julie
 *
 * @version @version@ (@date@)
 */
public class IllegalValueExceptionResponse extends ExceptionResponse {

	/**
	 * 
	 */
	public void setFunctionCode(int fc) {
		super.setFunctionCode(fc | Modbus.EXCEPTION_OFFSET);
	}
	
	/**
	 * 
	 */
	public IllegalValueExceptionResponse() {
		super(0, Modbus.ILLEGAL_VALUE_EXCEPTION);		
	}
	
	public IllegalValueExceptionResponse(int function) {
		super(function, Modbus.ILLEGAL_VALUE_EXCEPTION);
	}
}
