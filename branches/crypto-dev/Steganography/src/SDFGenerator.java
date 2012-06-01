

import org.spongycastle.crypto.DataLengthException;
import org.spongycastle.crypto.DerivationFunction;
import org.spongycastle.crypto.DerivationParameters;
import org.spongycastle.crypto.Digest;

/**
 * Seed Derivative Function (SDF) generator, derives a seed value to be used
 * with a PRNG given the SDF parameters, Digest, and the number of iterations
 * that the digest is applied
 *
 */
public class SDFGenerator implements DerivationFunction
{
	private Digest  digest;
	private static final int DEFAULT_ITERATIONS = 12;	// Default number of Digest iterations
	private int iterations;
	private byte[]  S1;
	private byte[]  S2;
	
	/**
	 * Define the digest to use for the SDF generator as well as the number
	 * of iterations that the digest is applied. If the number of iterations
	 * is not specified the default will be used
	 * 
	 * @param digest The hash digest to use, SHA256 is recommended
	 * @param iterations The number of iterations that the digest is applied
	 * 
	 * @throws IllegalArgumentException If the number of iterations specified is less than 1
	 */
	public SDFGenerator(Digest digest, int iterations)
	{
		if (iterations < 1)
		{
			 throw new IllegalArgumentException("The number of iterations must be greater than zero!");
		}
		
		this.digest = digest;
		this.iterations = iterations;
	}
	
	/**
	 * Define the digest to use for the SDF generator as well as the number
	 * of iterations that the digest is applied. If the number of iterations
	 * is not specified the default will be used
	 * 
	 * @param digest The hash digest to use, SHA256 is recommended
	 */
	public SDFGenerator(Digest digest)
	{	
		this.digest = digest;
		this.iterations = DEFAULT_ITERATIONS;
	}
	

	/* 
	 * Initialize the SDF generator given the SDF parameters, the derivation
	 * parameters must be an instance of SDFParameters
	 * 
	 * @param param The derivation parameters for the SDF
	 * 
	 * @throws IllegalArgumentException If derivation parameters are not SDFParameters
	 */
	public void init(DerivationParameters param)
	{
		if (param instanceof SDFParameters)
		{
			SDFParameters p = (SDFParameters) param;
			this.S1 = p.getS1();
			this.S2 = p.getS2();
		}
		else
		{
			throw new IllegalArgumentException("Derivation parameters MUST be an instance of SDFParameters!");
		}
	}

	
    /**
     * Fill the length bytes specified of the output buffer with bytes generated from
     * the seed derivation function (SDF). If no length is specified the size of the
     * digest will be used as the length of output
     *
     * @param out The output buffer to fill with the bytes generated for the seed
     * @param outOff The offset in the output buffer to populate
     * @param len The length of the output, if no length specified size of digest is used
     * 
     * @throws IllegalArgumentException if the size of the request will cause an overflow.
     * @throws DataLengthException if the out buffer is too small.
     */
    public int generateBytes(
        byte[]  out,
        int     outOff,
        int     len)
        throws DataLengthException, IllegalArgumentException
    {
    	/*
    	 * Exception if the requested output seed length is greater than the output size provided
    	 * by the digest (ie. maximum seed length output for SHA256 is 32 bytes) 
    	 */
        if (digest.getDigestSize() < len)
        {
        	throw new IllegalArgumentException("Length of output specified greater than the size of digest output!");
        }
    	
        if ((out.length - len) < outOff)
        {
            throw new DataLengthException("Output buffer too small for length specified!");
        }
        else if ((out.length - digest.getDigestSize()) < outOff)
        {
        	throw new DataLengthException("Output buffer too small for digest, " + digest.getAlgorithmName() + ", specified."
        			+ " Digest requires " + digest.getDigestSize() + " bytes!");
        }
        
    	// No length specified, use the default digest length
        if (len == 0)
        {
        	len = digest.getDigestSize();
        }

        long    oBytes = len;

        //
        // this is at odds with the standard implementation, the
        // maximum value should be hBits * (2^32 - 1) where hBits
        // is the digest output size in bits. We can't have an
        // array with a long index at the moment...
        //
        if (oBytes > ((2L << 32) - 1))
        {
            throw new IllegalArgumentException("Output length too large!");
        }

        // Digest input is the initial key and the generated seed
        byte[] digestInput = new byte[S1.length + S2.length];
        byte[] seed = new byte[digest.getDigestSize()];

        // The shared information S1 & S2 concatenated is the initial key
    	System.arraycopy(S1, 0, digestInput, 0, S1.length);
    	System.arraycopy(S2, 0, digestInput, S1.length, S2.length);
        
    	// Generate the initial seed
        digest.update(digestInput, 0, digestInput.length);
        digest.doFinal(seed, 0);
    	
        // Perform additional iterations of the digest to generate the final seed
        for (int i = 0; i < this.iterations; i++)
        {
            digest.update(seed, 0, seed.length);
            digest.doFinal(seed, 0);
        }
    
        System.arraycopy(seed, 0, out, outOff, len);
        digest.reset();

        return len;
    }

    
	/*
	 * Returns the digest being used by the SDF
	 * 
	 * @return Digest being used
	 */
	public Digest getDigest()
	{
		return this.digest;
	}
    
}
