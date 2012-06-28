/**
 * The following is a demonstration to test the compression algorithm where a recipient
 * receives a single byte Z which is the compressed message that was originally 2 bytes
 * (X and Y) and was compressed to a single value Z. Both the sender and recipient know
 * the constant C, the recipient must be able to recover the original 2 bytes (X and Y)
 * in the exact same order given the single byte, Z received and known constant C.
 */
#include <stdio.h>
#include <stdbool.h>

#define BYTETOBINARYPATTERN "%d%d%d%d%d%d%d%d"

#define BYTETOBINARY(byte)  \
  (byte & 0x80 ? 1 : 0), \
  (byte & 0x40 ? 1 : 0), \
  (byte & 0x20 ? 1 : 0), \
  (byte & 0x10 ? 1 : 0), \
  (byte & 0x08 ? 1 : 0), \
  (byte & 0x04 ? 1 : 0), \
  (byte & 0x02 ? 1 : 0), \
  (byte & 0x01 ? 1 : 0) 

typedef unsigned char BYTE;

BYTE calcQ(BYTE *X, BYTE *Y, const BYTE *C)
{
	// (X XNOR Y) XOR C
	return (~(*X ^ *Y)) ^ *C;
}

BYTE calcR(BYTE *X, BYTE *Y, const BYTE *D)
{
	// (X XNOR Y) XOR D
	return (~(*X ^ *Y)) ^ *D;
}

/**
 * Calculate Z, R XOR C and Q XOR D are equivalent
 * 
 * Z = R XOR C = Q XOR D
 * 
**/
BYTE calcZ(BYTE *R, const BYTE *C)
{
	// R XOR C
	return  *R ^ *C;
}


/**
 * Calculate Z', R XNOR C and Q XNOR D are equivalent
 * 
 * Z' = R XNOR C = Q XNOR D
 * 
 * NOTE: An interesting fact about Z' is that it is equal to ~Z, this is the
 * unique result that makes it possible to recover the variables X and Y!
 * 
**/
BYTE calcZprime(BYTE *Q, const BYTE *D)
{
	// Q XNOR D
	return  ~(*Q ^ *D);
}


/**
 * Calculate X from the value Z and known constant C
 * 
 * X = Z XNOR C = Z' XOR C
 * 
**/
BYTE calcX(BYTE *Z, const BYTE *C)
{
	// Z XNOR C
	return  ~(*Z ^ *C);
}


/**
 * Calculate Y from the value Zprime and calculated value Q
 * 
 * Y = Z' XNOR Q = Z XOR Q
 * 
**/
BYTE calcY(BYTE *Zprime, BYTE *Q)
{
	// Z' XNOR Q
	return  ~(*Zprime ^ *Q);
}


int main(void)
{
    // C MUST BE PRIME to ensure that it is co-prime with other values
	const BYTE C = 0x07;
    
    // D MUST be assigned the same value as Y
	BYTE D = 0x00;

    // Variable for the X, Y, etc. values calculated by the recipient
    BYTE calculated_X = 0x00;
    BYTE calculated_Y = 0x00;
    BYTE calculated_Q = 0x00;
    BYTE calculated_R = 0x00;
    BYTE calculated_Z = 0x00;
    BYTE calculated_Zprime = 0x00;
	
    bool root_found = false;
    
    BYTE Q = 0x00;
    BYTE R = 0x00;
    
    BYTE Z1 = 0x00;
    BYTE Z2 = 0x00;
    
    BYTE sender_Z1prime = 0x00;
    BYTE sender_Z2prime = 0x00;
    
    BYTE recipient_Zprime = 0x00;
    
    // Iterate through every possible combination of the X and Y byte values to verify
    // that it is possible to recover X and Y from Z given ANY combinations of values
    for (BYTE X = 0x00; X < 0x10; ++X)
    {
        // For each value of X iterate for each value of Y incremented by 0x01
        for (BYTE Y = 0x00; Y < 0x10; ++Y)
        {
            /**
             * Simulate the sender compressing the 2 bytes into a single byte, Z and
             * then sending Z to the recipient
             */
            D = Y;  // D MUST be same value as Y, D is not known to recipient
            Q = calcQ(&X, &Y, &C);
            R = calcR(&X, &Y, &D);
            
            // Calculate Z using both equivalent methods, Z = R XOR C = Q XOR D
            Z1 = calcZ(&R, &C);
            Z2 = calcZ(&Q, &D);
            
            // Calcualte Z' using both equivalent methods, Z' = R XNOR C = Q XNOR D
            sender_Z1prime = calcZprime(&R, &C);
            sender_Z2prime = calcZprime(&Q, &D);
            
            // Verify that Z using both methods is equivalent (This is an AXIOM OF THE
            // COMPRESSION ALGORITHM) so if it's wrong we have a major flaw
            if (Z1 != Z2)
            {
                printf("Z1 NOT EQUIVALENT TO Z2!\n");
                printf("SENDER X, Y:\t"BYTETOBINARYPATTERN", "BYTETOBINARYPATTERN"\t: %d, %d\n", BYTETOBINARY(X), BYTETOBINARY(Y), X, Y);
                printf("Q:\t\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY((0xF0 ^ Q)), 0xF0 ^ Q); 
                printf("R:\t\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY((0xF0 ^ R)), 0xF0 ^ R);
                printf("Z1:\t\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY((0xF0 ^ Z1)), 0xF0 ^ Z1);
                printf("Z2:\t\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY((0xF0 ^ Z2)), 0xF0 ^ Z2);
            }
            
            // Verify that Z' using both methods is equivalent (This is an AXIOM OF THE
            // COMPRESSION ALGORITHM) so if it's wrong we have a major flaw           
            if (sender_Z1prime != sender_Z2prime)
            {
                printf("Z1 PRIME NOT EQUIVALENT TO Z2 PRIME!\n");
                printf("SENDER X, Y:\t"BYTETOBINARYPATTERN", "BYTETOBINARYPATTERN"\t: %d, %d\n", BYTETOBINARY(X), BYTETOBINARY(Y), X, Y);
                printf("Q:\t\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY((0xF0 ^ Q)), 0xF0 ^ Q); 
                printf("R:\t\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY((0xF0 ^ R)), 0xF0 ^ R);
                printf("Z1 PRIME:\t\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY((0xF0 ^ sender_Z1prime)), 0xF0 ^ sender_Z1prime);
                printf("Z2 PRIME:\t\t"BYTETOBINARYPATTERN", %d\n\n", BYTETOBINARY((0xF0 ^ sender_Z2prime)), 0xF0 ^ sender_Z2prime);            
            }
            
               
            /**
             * Simulate the recipient receiving a single byte, Z and getting the original
             * bytes X and Y from Z, the recipient knows the constant value C, which is prime
             */
             // The user can get Z' which is ~Z, this is the UNIQUE property that makes it 
             // possible to recover X and Y by using the equations of for Z and Z' given the
             // two values Z and Z' = ~Z
             recipient_Zprime = ~(Z1);
             
             // Verify that the Zprime the recipient gets from ~Z is ACTUALLY, the same Zprime
             // that the sender calculated using the equations (This is an AXIOM OF THE
             // COMPRESSION ALGORITHM), so if it's wrong we have a major flaw
            if (recipient_Zprime != sender_Z1prime)
            {
                printf("RECIPIENT'S Z PRIME NOT EQUIVALENT TO SENDER'S Z PRIME!!!\n");
                printf("SENDER X, Y:\t"BYTETOBINARYPATTERN", "BYTETOBINARYPATTERN"\t: %d, %d\n", BYTETOBINARY(X), BYTETOBINARY(Y), X, Y);
                printf("Q:\t\t\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY((0xF0 ^ Q)), 0xF0 ^ Q); 
                printf("R:\t\t\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY((0xF0 ^ R)), 0xF0 ^ R);
                printf("SENDER Z PRIME:\t\t"BYTETOBINARYPATTERN", %d\n\n", BYTETOBINARY(sender_Z1prime), sender_Z1prime);
                printf("RECIPIENT Z PRIME:\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY(recipient_Zprime), recipient_Zprime);
            }

            // Now the user can calculate X very easily given a unique property when C is prime and D == Y
            // The user has RECEIVED Z and BOTH recipient and sender KNOW C, UNKNOWN is Y == D
            calculated_X = calcX(&Z1, &C);
            
            // Verify that the CALCULATED X IS ACTUALLY EQUAL to the Sender's X value the recipients' 
            // calculated X should ALWAYS be equivalent to the sender's X when Y == D (This is an AXIOM OF THE
            // COMPRESSION ALGORITHM), so if it's wrong we have a major flaw
            if (calculated_X != X)
            {
                printf("RECIPIENT'S CALCULATED X NOT EQUIVALENT TO SENDER'S X!!!\n");
                printf("Q:\t\t\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY((0xF0 ^ Q)), 0xF0 ^ Q); 
                printf("R:\t\t\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY((0xF0 ^ R)), 0xF0 ^ R);
                printf("SENDER X, Y:\t"BYTETOBINARYPATTERN", "BYTETOBINARYPATTERN"\t: %d, %d\n", BYTETOBINARY(X), BYTETOBINARY(Y), X, Y);
                printf("RECIPIENT X:\t"BYTETOBINARYPATTERN"\t\t: %d\n\n", BYTETOBINARY(calculated_X), calculated_X);
            }
            
            /**
            calculated_Y = 0x00;
            calculated_Q = 0x00;
            calculated_R = 0x00;
            calculated_Z = 0x00;
            calculated_Zprime = 0x00;
            root_found = false;
            
            calculated_Q = calcQ(&calculated_X, &Y, &C);
            calculated_R = calcR(&calculated_X, &Y, &Y);

            calculated_Z = calcZ(&calculated_R, &C);
            calculated_Zprime = calcZprime(&calculated_Q, &Y);
            
            calculated_Y = calcY(&calculated_Zprime, &calculated_Q);

            printf("SENDER Q:\t\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY((0xF0 ^ Q)), 0xF0 ^ Q); 
            printf("CALCULATED Q:\t\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY((0xF0 ^ calculated_Q)), 0xF0 ^ calculated_Q);
            printf("SENDER R:\t\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY((0xF0 ^ R)), 0xF0 ^ R);
            printf("CALCULATED R:\t\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY((0xF0 ^ calculated_R)), 0xF0 ^ calculated_R);
            printf("SENDER Z:\t\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY((0xF0 ^ Z1)), 0xF0 ^ Z1);
            printf("CALCULATED Z:\t\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY((0xF0 ^ calculated_Z)), 0xF0 ^ calculated_Z);
            printf("SENDER Z PRIME:\t\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY(sender_Z1prime), sender_Z1prime);
            printf("RECIPIENT Z PRIME:\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY(recipient_Zprime), recipient_Zprime);
            printf("CALCULATED Z PRIME:\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY(calculated_Zprime), calculated_Zprime);
            printf("SENDER X, Y:\t"BYTETOBINARYPATTERN", "BYTETOBINARYPATTERN"\t: %d, %d\n", BYTETOBINARY(X), BYTETOBINARY(Y), X, Y);
            printf("RECIPIENT X, Y:\t"BYTETOBINARYPATTERN", "BYTETOBINARYPATTERN"\t: %d, %d\n\n\n", BYTETOBINARY(calculated_X), 
                    BYTETOBINARY(calculated_Y), calculated_X, calculated_Y);
            */
            // Now solve for the unknown value Y, given the Z' value, X, and C, the only way to feasibly solve
            // for Y is to perform a bruteforce search given the known values and the equations for
            // Z' and Q. THERE MUST ONLY BE ONE ROOT, Y, GIVEN THE ROOT X , Z' and C (This is an AXIOM OF THE
            // COMPRESSION ALGORITHM), so if it's wrong we have a major flaw, in fact its IMPOSSIBLE to
            // reverse the compression unless Y == D is UNIQUE!!!!
            for (BYTE Y_TEST = 0x00; Y_TEST < 0x10; ++Y_TEST)
            {
                calculated_Q = calcQ(&calculated_X, &Y_TEST, &C);
                calculated_R = calcR(&calculated_X, &Y_TEST, &Y_TEST);
                
                calculated_Z = calcZ(&calculated_R, &C);
                calculated_Zprime = calcZprime(&calculated_Q, &Y_TEST);
                
                //printf("Calculated X: %d\n", calculated_X);
                //printf("Calculated Y: %d\n", Y_TEST);
                //printf("Calculated Q: %d\n", 0xF0 ^ calculated_Q);
                //printf("Calculated R: %d\n", 0xF0 ^ calculated_R);
                //printf("Calculated Z: %d\n",  0xF0 ^ calculated_Z);
                //printf("Calculated Z PRIME: %d\n\n", calculated_Zprime);
                
                /**
                printf("SENDER Q:\t\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY((0xF0 ^ Q)), 0xF0 ^ Q); 
                printf("CALCULATED Q:\t\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY((0xF0 ^ calculated_Q)), 0xF0 ^ calculated_Q);
                printf("SENDER R:\t\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY((0xF0 ^ R)), 0xF0 ^ R);
                printf("CALCULATED R:\t\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY((0xF0 ^ calculated_R)), 0xF0 ^ calculated_R);
                printf("SENDER Z:\t\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY((0xF0 ^ Z1)), 0xF0 ^ Z1);
                printf("CALCULATED Z:\t\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY((0xF0 ^ calculated_Z)), 0xF0 ^ calculated_Z);
                printf("SENDER Z PRIME:\t\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY(sender_Z1prime), sender_Z1prime);
                printf("RECIPIENT Z PRIME:\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY(recipient_Zprime), recipient_Zprime);
                printf("CALCULATED Z PRIME:\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY(calculated_Zprime), calculated_Zprime);
                printf("SENDER X, Y:\t"BYTETOBINARYPATTERN", "BYTETOBINARYPATTERN"\t: %d, %d\n", BYTETOBINARY(X), BYTETOBINARY(Y), X, Y);
                printf("RECIPIENT X, Y:\t"BYTETOBINARYPATTERN", "BYTETOBINARYPATTERN"\t: %d, %d\n\n\n", BYTETOBINARY(calculated_X), 
                    BYTETOBINARY(Y_TEST), calculated_X, Y_TEST);
                */
                
                // If the calculated Z' given the calculated root Y matches the known Z' which is 
                // Z' = ~Z, then the root Y satisfies the equations and IS THE ROOT. Remember that
                // for the sender as well as the recipient Y == D, this is the unique property that
                // makes it possible to avoid the 0000/1111 cancellations of XOR              
                if (calculated_X == calcX(&calculated_Z, &C))
                {
                    if (Y_TEST == calcY(&calculated_Zprime, &calculated_Q))
                    {
                        if (Z1 == calculated_Z && recipient_Zprime == calculated_Zprime)
                        {
                            // X XOR Y SHOULD NOT BE EQUIVALENT TO Z, ONLY THE UNIQUE ROOT IS NOT A COMBINATION
                            // OF X XOR Y = Z
                            //if (Y_TEST == Y)
                            if ((calculated_X ^ Y_TEST) != calculated_Z)
                            {
                                calculated_Y = Y_TEST;
                                root_found = true;
                                break;
                            }
                        }
                    }
                }
            }
            // If the correct root is found I will be DELIGHTED, the calculated Y MUST
            // be UNIQUE and equivalent to the initial Y value of the sender, if it does
            // not match we have a major flaw,
            if (calculated_Y == Y)
            {
                printf("RECIPIENT'S CALCULATED Y EQUIVALENT TO SENDER'S Y!!!!!!!!!!!!!!!!!!!!\n");
                printf("SENDER Q:\t\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY((0xF0 ^ Q)), 0xF0 ^ Q); 
                printf("CALCULATED Q:\t\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY((0xF0 ^ calculated_Q)), 0xF0 ^ calculated_Q);
                printf("SENDER R:\t\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY((0xF0 ^ R)), 0xF0 ^ R);
                printf("CALCULATED R:\t\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY((0xF0 ^ calculated_R)), 0xF0 ^ calculated_R);
                printf("SENDER Z:\t\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY((0xF0 ^ Z1)), 0xF0 ^ Z1);
                printf("CALCULATED Z:\t\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY((0xF0 ^ calculated_Z)), 0xF0 ^ calculated_Z);
                printf("SENDER Z PRIME:\t\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY(sender_Z1prime), sender_Z1prime);
                printf("RECIPIENT Z PRIME:\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY(recipient_Zprime), recipient_Zprime);
                printf("CALCULATED Z PRIME:\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY(calculated_Zprime), calculated_Zprime);
                printf("SENDER X, Y:\t"BYTETOBINARYPATTERN", "BYTETOBINARYPATTERN"\t: %d, %d\n", BYTETOBINARY(X), BYTETOBINARY(Y), X, Y);
                printf("RECIPIENT X, Y:\t"BYTETOBINARYPATTERN", "BYTETOBINARYPATTERN"\t: %d, %d\n\n", BYTETOBINARY(calculated_X), 
                    BYTETOBINARY(calculated_Y), calculated_X, calculated_Y);
            }
            // major flaw, back to the drawing board!
            else
            {
                printf("RECIPIENT'S CALCULATED Y NOT EQUIVALENT TO SENDER'S Y!!!!!!!!!!!!!!!!!!!!\n");
                printf("SENDER Q:\t\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY((0xF0 ^ Q)), 0xF0 ^ Q); 
                printf("CALCULATED Q:\t\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY((0xF0 ^ calculated_Q)), 0xF0 ^ calculated_Q);
                printf("SENDER R:\t\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY((0xF0 ^ R)), 0xF0 ^ R);
                printf("CALCULATED R:\t\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY((0xF0 ^ calculated_R)), 0xF0 ^ calculated_R);
                printf("SENDER Z:\t\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY((0xF0 ^ Z1)), 0xF0 ^ Z1);
                printf("CALCULATED Z:\t\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY((0xF0 ^ calculated_Z)), 0xF0 ^ calculated_Z);
                printf("SENDER Z PRIME:\t\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY(sender_Z1prime), sender_Z1prime);
                printf("RECIPIENT Z PRIME:\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY(recipient_Zprime), recipient_Zprime);
                printf("CALCULATED Z PRIME:\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY(calculated_Zprime), calculated_Zprime);
                printf("SENDER X, Y:\t"BYTETOBINARYPATTERN", "BYTETOBINARYPATTERN"\t: %d, %d\n", BYTETOBINARY(X), BYTETOBINARY(Y), X, Y);
                printf("RECIPIENT X, Y:\t"BYTETOBINARYPATTERN", "BYTETOBINARYPATTERN"\t: %d, %d\n\n", BYTETOBINARY(calculated_X), 
                    BYTETOBINARY(calculated_Y), calculated_X, calculated_Y);
            }
        }
            /**
            Zprime = calcZprime(&Q, &D);
            
            calculated_Y = calcY(&Zprime, &Q);

            if ( X == calculated_X && Y == calculated_Y)
            {
            printf("Q:\t\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY((0xF0 ^ Q)), 0xF0 ^ Q); 
            printf("R:\t\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY((0xF0 ^ R)), 0xF0 ^ R);
            printf("Z1:\t\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY((0xF0 ^ Z1)), 0xF0 ^ Z1);
            printf("Z2:\t\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY((0xF0 ^ Z2)), 0xF0 ^ Z2);

            );
            //printf("Z':\t\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY(Zprime), Zprime);

            printf("Q:\t\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY(Q), Q); 
            printf("R:\t\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY(R), R);
            printf("Z:\t\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY(Z), Z);
            printf("Z':\t\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY(Zprime), Zprime);

            printf("INITIAL X, Y:\t"BYTETOBINARYPATTERN", "BYTETOBINARYPATTERN"\t: %d, %d\n", BYTETOBINARY(X), BYTETOBINARY(Y), X, Y);
            printf("SOLVED X, Y:\t"BYTETOBINARYPATTERN", "BYTETOBINARYPATTERN"\t: %d, %d\n\n", 
            BYTETOBINARY(calculated_X), BYTETOBINARY(calculated_Y), calculated_X, calculated_Y);
            }*/
    }
	return 0;
}
