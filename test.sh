#!/bin/bash -e

function randomString()
{
  length=${1}
  #length=13
  </dev/urandom tr -dc 'A-Za-z0-9!"#$%&'\''()*+,-./:;<=>?@[\]^_`{|}~' | head -c ${length} 
}

function check()
{
  plain="${1}"
  #plain='123456789'

  crypted="$(./junos_crypt.pl "${plain}")"
  decoded="$(./junos_decrypt.pl "${crypted}")"

  if [ "${plain}" == "${decoded}" ]; then
#    echo OK
    :
  else
    echo 'Mismatch found! :('
    echo plain=${plain}
    echo crypted=${crypted}
    echo decoded=${decoded}
    false
  fi
}

for length in {1..200}; do
  echo Length of random string: $length
  echo Several random string will be generated with this length! Please wait!

  for i in {1..100}; do
    input="$(randomString "${length}")"
    check "${input}"
    echo -n .
  done

  echo
done

echo 'No mismatch found! :)'
