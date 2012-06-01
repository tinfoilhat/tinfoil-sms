import org.spongycastle.crypto.DataLengthException;
import org.spongycastle.crypto.DerivationParameters;

 /**
 * Seed Derivative Function (SDF) parameter, takes the shared information 
 * S1 & S2 as constructor arguments which are used as parameters for 
 * deriving the shared seed
 */
public class SDFParameters implements DerivationParameters
{

    byte[]  S1;
    byte[]  S2;
	
	/**
	 * Defines the two parameters for the SDF which are the shared
	 * information S1 & S2, the values cannot be empty
	 * 
	 * @param S1 Shared information S1
	 * @param S2 Shared information S2
	 * 
	 * @throws DataLengthException if shared information is empty
	 */
	public SDFParameters(String S1, String S2)
			throws DataLengthException
	{
		if (S1.length() == 0 || S2.length() == 0)
		{
		    throw new DataLengthException("You must specify a value for the shared information!");
		}
		
		this.S1 = S1.getBytes();
		this.S2 = S2.getBytes();
	}


	/**
	 * Gets the shared information S1
	 * @return shared information, S1
	 */
    public byte[] getS1()
    {
        return S1;
    }
    
	/**
	 * Gets the shared information S2
	 * @return shared information, S2
	 */
    public byte[] getS2()
    {
        return S2;
    }
}
