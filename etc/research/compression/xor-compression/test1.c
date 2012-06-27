/**
 * A simple program to perform binary operations for testing the compression algorithm
 * which makes use of XOR and XNOR to compress bytes A and B into one single byte
 * given KNOWN constants C and D.
**/
#include <stdio.h>

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


unsigned char calcQ(unsigned char *X, unsigned char *Y, const unsigned char *C)
{
	// (X XNOR Y) XOR C
	return (~(*X ^ *Y)) ^ *C;
}

unsigned char calcR(unsigned char *X, unsigned char *Y, const unsigned char *D)
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
unsigned char calcZ(unsigned char *R, const unsigned char *C)
{
	// R XOR C
	return  *R ^ *C;
}


/**
 * Calculate Z', R XNOR C and Q XNOR D are equivalent
 * 
 * Z = R XNOR C = Q XNOR D
 * 
**/
unsigned char calcZprime(unsigned char *Q, const unsigned char *D)
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
unsigned char calcX(unsigned char *Z, const unsigned char *C)
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
unsigned char calcY(unsigned char *Zprime, unsigned char *Q)
{
	// Z' XNOR Q
	return  ~(*Zprime ^ *Q);
}


int main(void)
{
	const unsigned char C = 0x03;
	const unsigned char D = 0x08;

    // Able to solve for ALL values of X and Y when Y and D are same, prime
	//unsigned char Y = 0x07;

    // Varialbe for the X and Y values calculated
    unsigned char calculated_X = 0x00;
    unsigned char calculated_Y = 0x00;
	
    unsigned char Q = 0x00;
    unsigned char R = 0x00;
    
    unsigned char Z = 0x00;
    unsigned char Zprime = 0x00;
    
    // Increment X by 0x01
    for (unsigned char X = 0x00; X < 0x10; ++X)
    {
        // For each value of X iterate for each value of Y incremented by 0x01
        for (unsigned char Y = 0x00; Y < 0x10; ++Y)
        {
            Q = calcQ(&X, &Y, &C);
            R = calcR(&X, &Y, &D);
            
            Z = calcZ(&R, &C);
            Zprime = calcZprime(&Q, &D);
            
            calculated_X = calcX(&Z, &C);
            calculated_Y = calcY(&Zprime, &Q);
            
            if ( X == calculated_X && Y == calculated_Y)
            {
                printf("Q:\t\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY((0xF0 ^ Q)), 0xF0 ^ Q); 
                printf("R:\t\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY((0xF0 ^ R)), 0xF0 ^ R);
                printf("Z:\t\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY((0xF0 ^ Z)), 0xF0 ^ Z);
                printf("Z':\t\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY(Zprime), Zprime);
                
                //printf("Q:\t\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY(Q), Q); 
                //printf("R:\t\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY(R), R);
                //printf("Z:\t\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY(Z), Z);
                //printf("Z':\t\t"BYTETOBINARYPATTERN", %d\n", BYTETOBINARY(Zprime), Zprime);
                
                printf("INITIAL X, Y:\t"BYTETOBINARYPATTERN", "BYTETOBINARYPATTERN"\t: %d, %d\n", BYTETOBINARY(X), BYTETOBINARY(Y), X, Y);
                printf("SOLVED X, Y:\t"BYTETOBINARYPATTERN", "BYTETOBINARYPATTERN"\t: %d, %d\n\n", 
                    BYTETOBINARY(calculated_X), BYTETOBINARY(calculated_Y), calculated_X, calculated_Y);
            }
        }
    }
	return 0;
}
