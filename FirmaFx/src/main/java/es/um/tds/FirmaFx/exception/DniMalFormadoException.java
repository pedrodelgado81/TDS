package es.um.tds.FirmaFx.exception;

/**
 * Excepcion que se lanza cuando el DNI no está bien formado
 */
public class DniMalFormadoException extends Exception{

	public DniMalFormadoException(String mensaje) {
		super(mensaje);
	}

}
