#!/usr/bin/perl

use Crypt::Juniper;

my $hash = $ARGV[0];
my $secret = juniper_encrypt($hash);

print "$secret";

