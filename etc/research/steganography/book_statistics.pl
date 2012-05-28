#!/usr/bin/perl
use utf8;
use warnings;
use feature qw(say);
use Encode;
use Tie::File;
use Fcntl 'O_RDONLY';
use IO::File;
use File::CountLines qw(count_lines);

# Why the hell do I have to define this as a constant perl?
use constant false => 0;
use constant true  => 1;

binmode(STDIN,":utf8");
binmode(STDOUT,":utf8");

# The book that you wish to use the text from, download the text of a book
# from Project Gutenberg (http://www.gutenberg.org)
my $book_directory = "/home/jon/source/tinfoil-sms/etc/research/superbook_sources/";

# The directory to store the histogram data for each book
my $histogram_directory = "/home/jon/source/tinfoil-sms/etc/research/superbook_output/";

# Histogram containing the frequency of each word in the book
my %word_histogram = ();

# Histogram containing the frequency of the lengths of words in the book
my %length_histogram = ();

# Summary histogram containing the frequency of the lengths of words for ALL books
my %length_summary_histogram = ();


# Clean up the book, remove the project gutenberg disclaimers/legal info
# $1 - The location and name of book
sub remove_disclaimers {
    my $book = shift;
	my $book_lines = count_lines($book);
    
    # Indexes, index 2 is either 500 lines or length of book
    my $idx = 1;
    my $idx2 = 500;
    $idx2 = $book_lines if $book_lines < 500;
    
    # Open the book, to remove the disclaimers, encoded as UTF-8
    tie my @book_contents, 'Tie::File', $book, discipline => ':encoding(UTF-8)' or die "$!";
      
	# If it is a project gutenburg file remove the header and footer disclaimers
	if ($book_contents[0] =~ /Project\s*Gutenberg/)
	{
        # Lines from header to remove
		until ($book_contents[$idx] =~ /\*\*\*\s*START\s*OF.*PROJECT\s*GUTENBERG/
                || $book_contents[$idx] =~ /^.*END.*THE.*SMALL.*PRINT\!/
                || $book_contents[$idx] =~ /^\s*\*SMALL\s*PRINT\!/
                || $book_contents[$idx] =~ /^.*etext.*(produced|created|made|authored|written).*by.*$/i)
		{
            # If the end of the header hasn't been found after 500 lines 
            # or the length of the book give up 
            if ($idx == 500 || $idx == $book_lines -1)
            {
                $idx = 0;
                print "$book: END OF HEADER NOT FOUND\n";
                last;
            }
            $idx++;
		}
        $idx++;
        
        # Lines from footer to remove
        until ($book_contents[$book_lines - $idx2] =~ /End\sof.*Project\s*Gutenberg/ 
                || $book_contents[$book_lines - $idx2] =~ /\*\*\*\s*END\s*OF.*PROJECT\s*GUTENBERG/
                || $book_contents[$book_lines - $idx2] =~ /\*\*\*\s*START:\s*FULL\s*LICENSE/)
        {
            # If after 500 lines or the length of the book the start 
            # of the footer has not been found give up
            if ($idx2 == 0)
            {
                print "$book: START OF FOOTER NOT FOUND\n";
                last;
            }
            $idx2--;
        }
        
        # Remove the header and footer
        for ($i = 0; $i < $idx; $i++)
        {
            shift @book_contents;
        }
        for ($j = 0; $j < $idx2; $j++)
        {
            pop @book_contents;
        }
    }
    # Close the book
    untie @book_contents or die "$!";
}



# Generates the word frequency and word length histograms for each word in the book
# $1 - The location and name of book
# $2 - Reference to hash to populate with word frequency histogram data
# $3 - Reference to hash to populate with word length histogram data
sub gen_hists
{
    my $book = shift;
    my $ref_word_hist = shift;       # Reference to word frequency hash to populate
    my $ref_length_hist = shift;     # Reference to word length hash to populate
    
    # Open the book in read-only mode, encoded as UTF-8
    tie my @book_contents, 'Tie::File', $book, discipline => ':encoding(UTF-8)', mode => O_RDONLY or die "$!";
    #tie my @book_contents, 'Tie::File', $book, mode => O_RDONLY or die;
    
    foreach my $line (@book_contents)
    {
        my $curline = $line;
        $curline = lc($curline);
        
        # Remove all control characters and punctuation except hyphens and em/en dashes
        $curline =~ s/[\t\n\r\v\f]//g;
        $curline =~ s/[\!\"\#\$\%\&\'\(\)\*\+\,\.\/\:\;\<\=\>\?\@\[\]\^\_\`\{\|\}\~\\]//g;
        
        # Remove stand-alone numerical values
        $curline =~ s/^[\d]+\s/ /;
        $curline =~ s/\s[\d]+$/ /;
        while ($curline =~ s/\s[\d]+\s/ /) {} ;
        
        # Remove any remaining numerical values
        $curline =~ s/\d//g;
        
        # Preserve compound words, remove em/en dashes between words
        $curline =~ s/\-\-/ /g;
        $curline =~ s/\W\-\W/ /g;
        $curline =~ s/(\w)\-\W/$1 /g;
        $curline =~ s/\W\-(\w)/ $1/g;
        $curline =~ s/^\-//;
        $curline =~ s/\-$//;
        
        # Remove all words < 2 characters in length
        $curline =~ s/^[\w]{1}$//;
        $curline =~ s/^[\w]{1}\s/ /;
        $curline =~ s/\s[\w]{1}$/ /;
        while ($curline =~ s/\s[\w]{1}\s/ /) {} ;
        
        # Replace multiple non page breaking spaces with a single npbs
        #$curline =~ s/(\X{2})+(\xC2\xA0)+(\X{2})+/${1}\x00\x20${3}/g;
        $curline =~ s/(\xC2\xA0){2,}/\N{U+00A0}/g;
        
        # Remove any lines that only contain spaces
        $curline =~ s/^[\s]+$//g;
        
        # Replace multiple spaces with a single space
        $curline =~ s/[\s]{2,}/ /g;
        
        my @words = split(/[\s\N{U+00A0}]/, $curline);
        
        # For each word increment the word length and frequency counters for the histograms
        foreach (@words)
        {        
            if ($_ ne "" && $_ ne "\x{00A0}" && $_ ne "\x00")
            {   
                # Increment the word length counters
                if (exists $ref_length_hist->{length($_)})
                {
                    $ref_length_hist->{length($_)}++;
                } 
                else
                {
                    $ref_length_hist->{length($_)} = 1;
                }
                
                # Not sure about the big-oh performance of exists() has to do
                # a lookup for EVERY word > 1 character in the book so N * big-oh(exists())
                if (exists $ref_word_hist->{$_})
                {
                    $ref_word_hist->{$_}++;
                } 
                else
                {
                   $ref_word_hist->{$_} = 1;
                }
            }
        }
    }
    # Close the book
    untie @book_contents or die "$!";
}


# Adds the values from the histogram hash provided to the summary histogram
# For example, for summarizing the instances for each unique word for all books
# $1 - Reference to the summary histogram hash to populate
# $2 - Reference to histogram hash containing data to add to the summary histogram hash
sub add_to_summary_hist
{
    my $ref_summary_hist = shift;
    my $ref_hist = shift;

    foreach my $key (keys(%{$ref_hist}))
    {        
        if (exists $ref_summary_hist->{$key})
        {
            $ref_summary_hist->{$key} += $ref_hist->{$key};
        } 
        else
        {
           $ref_summary_hist->{$key} = $ref_hist->{$key};
        }
    }
}


# Saves the histogram information for the book to a file which
# has the same name as the book with the added extension specified (ie. _word_hist.txt)
# $1 - The name of book
# $2 - The location of the directory to save the histogram to
# $3 - The added extension specified (ie. _word_hist.txt)
# $4 - Reference to hash of the histogram data
sub save_hist
{
    my $book = shift;
    my $hist_file = "";
    my $directory = shift;
    my $extension = shift;
    my $ref_hist = shift;
    my $numeric_keys = true; 

    if ($book =~ /(^.*)\.txt/)
    {
        $hist_file = $1 . $extension;
    }
    
    # Open the file to write to
    my $hist_fh = IO::File->new($directory . $hist_file,'w');
    if (defined $hist_fh)
    {
        # MUST SPECIFY THAT THE OUTPUT FILE IS UTF-8
        binmode($hist_fh, ':encoding(UTF-8)');
        
        # If all of the hash keys are numeric, such as for word length, sort the output
        foreach my $key (keys(%{$ref_hist}))
        {
            # One of the keys not an integer
            if ($key =~ /^[^\d]+$/)
            {
                $numeric_keys = false;
            }
        }

        if ($numeric_keys)
        {
            foreach my $key (sort {$a <=> $b} keys(%{$ref_hist}))
            {
                print $hist_fh "$key $ref_hist->{$key}\n";
            }
        }
        # Alphabetical/Alpha-numeric, do not sort
        else
        {
            foreach my $key (keys(%{$ref_hist}))
            {
                print $hist_fh "$key $ref_hist->{$key}\n";
            }
        }
        $hist_fh->close;
    }
}


# Saves the summary histogram information for all of the books of the sample
# it contains the number of unique words contained in each book
# $1 - The directory to save the summary histogram to
# $2 - The name of the file to save the summary as
# $3 - The name of the book
# $4 - Reference to hash of the histogram containing the data
sub save_word_summay_hist
{
    my $directory = shift;
    my $summary_hist = shift;
    my $book = shift;
    my $ref_hist = shift;

    # Open the file to concatenate to
    my $summary_fh = IO::File->new(">> " . $directory . $summary_hist);
    if (defined $summary_fh)
    {
        print $summary_fh "$book ", scalar keys %{$ref_hist}, "\n";
        $summary_fh->close;
    }
}


# Saves the summary histogram information for the length of words for all of 
# the books of the sample
# $1 - The directory to save the summary histogram to
# $2 - The name of the file to save the summary as
# $3 - Reference to hash of the word length histogram containing the data
sub save_length_summay_hist
{
    my $directory = shift;
    my $summary_hist = shift;
    my $ref_hist = shift;

    # Open the file to write to
    my $summary_fh = IO::File->new(">> " . $directory . $summary_hist);
    if (defined $summary_fh)
    {
        foreach my $key (sort {$a <=> $b} keys(%{$ref_hist}))
        {
            print $summary_fh "$key $ref_hist->{$key}\n";
        }
        $summary_fh->close;
    }
}

opendir(DIR, $book_directory) or die "$!";

while (my $book = readdir(DIR)) {
    
    # Use a regular expression to ignore files that do not have the extension txt
    next unless ($book =~ m/^.*\.txt$/);
    
    %word_histogram = ();
    %length_histogram = ();
    
    print "$book\n";
    remove_disclaimers($book_directory . $book);
    
    print "Generating histogram\n";
    gen_hists($book_directory . $book, \%word_histogram, \%length_histogram);
    
    print "Saving word frequency histogram\n";
    save_hist($book, $histogram_directory, "_word_hist.txt", \%word_histogram);
 
    print "Saving word length histogram\n";
    save_hist($book, $histogram_directory, "_length_hist.txt", \%length_histogram);
    
    print "Saving number of unique words summary histogram\n";
    save_word_summay_hist("./", "summary_word_hist.txt", $book, \%word_histogram);
    
    print "Adding length of words to summary histogram\n";
    add_to_summary_hist(\%length_summary_histogram, \%length_histogram);
    
    print "$book: ", scalar keys %word_histogram, "\n";
}

print "Saving the length of words summary histogram\n";
save_length_summay_hist("./", "summary_length_hist.txt", \%length_summary_histogram);

closedir(DIR);
exit 0;
