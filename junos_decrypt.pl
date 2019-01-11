#!/usr/bin/perl

use Crypt::Juniper;

my $hash = $ARGV[0];
my $plain = juniper_decrypt($hash);

print "$plain";

