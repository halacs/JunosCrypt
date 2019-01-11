# Juniper Preshared Key encryption/decrtion in Java
This is an example how you can encrypt and decrypt your Juniper preshared keys (e.g. VPN keys) for a Junper device.

As you can see from the java source too, 99% of the source comes from the public internet: https://forums.juniper.net/t5/Junos/Password-encryption-algorithm-in-Junos/td-p/96208

# Example output

## Perl version used as reference:
```
root@halacs:/tmp/JunosCrypt# ./junos_crypt.pl 123
$9$OlGH1cyevWx-V
root@halacs:/tmp/JunosCrypt# ./junos_crypt.pl 123
$9$OCei1cyevWx-V
root@halacs:/tmp/JunosCrypt# ./junos_crypt.pl 123
$9$9MG3AO1EcyKWL
```

## Java version
```
root@halacs:/tmp/JunosCrypt# java JuniperPassword 123
$9$IqrEreM8X-bs
root@halacs:/tmp/JunosCrypt# java JuniperPassword 123
$9$.Pz3/CtOIE
root@halacs:/tmp/JunosCrypt# java JuniperPassword 123
$9$KRbMxNVwYoZU
```
