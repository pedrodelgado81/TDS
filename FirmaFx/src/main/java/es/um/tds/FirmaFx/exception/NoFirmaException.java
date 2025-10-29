package es.um.tds.FirmaFx.exception;

/**
 * Excepcion que se lanza cuando falta la firma
 */
public class NoFirmaException extends Exception{
	
	public NoFirmaException(String mensaje) {
		super(mensaje);
	}

}
