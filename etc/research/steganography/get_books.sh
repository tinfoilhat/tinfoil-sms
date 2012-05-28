#!/bin/bash
#
# A simple script to collect a simple random sample of books from project gutenburg
# as of 05/23/2012 the number of books on project gutenburg (population) is 39753

POPULATION=39753
SAMPLE=3
SAMPLE_COUNT=0
BASE_URL="http://www.gutenberg.org/cache/epub/"
DOWNLOAD_DIR="/home/jon/source/tinfoil-sms/etc/research/gutenburg_sample"
LOGFILE="get_books.log"



#****f* general-interface/file_exists
# NAME
#   file_exists
# DESCRIPTION
#   Simple function that checks if a file exists and is NOT empty
# ARGUMENTS
#   $1 - The location and name of the file
# RESULT
#   exitstatus - Sets $? as a zero for success, otherwise sets an error code
# EXAMPLE
#   file_exists ./template.xml
#***
file_exists()
{
    RESULT=1
    if [ -s "$1" ]
    then
        RESULT=0
    fi

    return $RESULT
}


# Download random books from project gutenburg
while :
do
    # Randomly select a book
    RANDOM_BOOK=$((${RANDOM} % ${POPULATION} +1))
    
    # Download the book
    URL="${BASE_URL}${RANDOM_BOOK}/pg${RANDOM_BOOK}.txt"
    echo $URL
    
    # If the book already exists, skip to the next random book
    if file_exists "${DOWNLOAD_DIR}/pg${RANDOM_BOOK}.txt" ; test "$?" -eq 0
    then
        continue
    else
        wget --verbose --header='Accept-Language: en-us,en;q=0.5' --header='Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8' --header='Connection: keep-alive' -U 'Mozilla/5.0 (Windows NT 5.1; rv:10.0.2) Gecko/20100101 Firefox/10.0.2' ${URL} -P "${DOWNLOAD_DIR}/" >> ${LOGFILE} 2>&1
    fi
    
    # If download succeeded, book exists and contains text increment sample counter 
    if test "$?" -eq 0 && file_exists "${DOWNLOAD_DIR}/pg${RANDOM_BOOK}.txt" ; test "$?" -eq 0
    then
        SAMPLE_COUNT=$((${SAMPLE_COUNT} + 1))
        
        # Sleep for a few seconds
        sleep 10
    
    # The file does not exist or is empty        
    else
        rm -f "${DOWNLOAD_DIR}/pg${RANDOM_BOOK}.txt"
    fi
    
    if [ "${SAMPLE_COUNT}" -eq "${SAMPLE}" ]
    then
        break
    fi
done
