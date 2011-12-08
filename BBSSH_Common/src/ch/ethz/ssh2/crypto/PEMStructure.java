
package ch.ethz.ssh2.crypto;
import net.rim.device.api.util.Persistable;

/**
 * Parsed PEM structure.
 * 
 * @author Christian Plattner, plattner@inf.ethz.ch
 * @version $Id: PEMStructure.java,v 1.1 2005/08/11 12:47:31 cplattne Exp $
 */

public class PEMStructure implements Persistable
{
	int pemType;
	String dekInfo[];
	String procType[];
	byte[] data;
}