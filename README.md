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
root@halacs:/tmp/JunosCrypt# ./junos_crypt.pl 123456789012345678901234567890123456789012345678901234567890
$9$8X/XdbYgoGjH2gFn9A0OdbwsJGP5Qzn/Hkp0IRSy24oJikTz3pO1CA7-dVY2fTQ3CtO1RyevIRwYoJDjO1IEevM8X7dbeKoJZUHkO1IhevNdbwgobwqmf5F3SrlMxNwYgGjH4o/CAu1IYg4oUjHqm5z3iHuO1RSy4aJD.P69ApBIzF
```

## Java version
```
root@halacs:/tmp/JunosCrypt# java JuniperPassword 123
$9$IqrEreM8X-bs
root@halacs:/tmp/JunosCrypt# java JuniperPassword 123
$9$.Pz3/CtOIE
root@halacs:/tmp/JunosCrypt# java JuniperPassword 123
$9$KRbMxNVwYoZU
root@halacs:/tmp/JunosCrypt# java JuniperPassword 123456789012345678901234567890123456789012345678901234567890
$9$vvNW7-bs2aGDwsTzn/tp7-dV4aqmf5z3DjCtOBEhwY24UjP5QCp06/Lx7Nbw.PfQ69p0BhylOBdb24ZGp0O1ylevWL7-yr24oJDjp0OIylX7-ds2-dik.mTQEcSe8XdbsaGDY236/A0ObsY2JGDikm5QUDAp0BEhYg4ZHqFn/CuO5T
```
