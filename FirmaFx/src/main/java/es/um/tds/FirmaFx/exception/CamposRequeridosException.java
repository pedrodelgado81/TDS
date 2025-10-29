package es.um.tds.FirmaFx.exception;

/**
 * Excepcion personalizada para validar campos
 */
public class CamposRequeridosException extends Exception {

	public CamposRequeridosException(StringBuffer mensaje) {
		super(mensaje.toString());
	}

}
